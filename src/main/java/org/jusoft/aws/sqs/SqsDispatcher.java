package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.apache.commons.lang3.Validate;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.annotations.SqsBody;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.executor.SqsExecutorFactory;
import org.jusoft.aws.sqs.mapper.MessageMapper;
import org.jusoft.aws.sqs.provider.ConsumerInstanceProvider;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.jusoft.aws.sqs.annotations.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

public class SqsDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsDispatcher.class);

  private final AmazonSQS sqsClient;
  private final MessageMapper messageMapper;
  private final DeleteMessageService deleteMessageService;
  private final ConsumerInstanceProvider consumersProvider;
  private final SqsExecutorFactory sqsExecutorFactory;
  private final ConsumerValidator consumerValidator;

  private ExecutorService executor;
  private boolean isDispatcherRunning;


  public SqsDispatcher(AmazonSQS sqsClient,
                       MessageMapper messageMapper,
                       DeleteMessageService deleteMessageService,
                       ConsumerInstanceProvider consumersProvider,
                       SqsExecutorFactory sqsExecutorFactory,
                       ConsumerValidator consumerValidator) {
    this.sqsClient = sqsClient;
    this.messageMapper = messageMapper;
    this.deleteMessageService = deleteMessageService;
    this.consumersProvider = consumersProvider;
    this.sqsExecutorFactory = sqsExecutorFactory;
    this.consumerValidator = consumerValidator;
  }

  @PostConstruct
  public void subscribeConsumers() {
    LOGGER.info("Initializing SQS consumers");
    Iterable<Consumer> consumers = consumersProvider.getConsumers();

    consumerValidator.isValid(consumers);

    isDispatcherRunning = true;
    if (consumers.iterator().hasNext()) {
      executor = sqsExecutorFactory.createFor(consumers);
      consumers.forEach(consumer -> executor.submit(() -> start(consumer)));
    }
  }

  private void start(Consumer consumer) {
    SqsConsumer consumerProperties = consumer.getAnnotation();
    LOGGER.info("Starting consumer: queue={}", consumerProperties.value());

    String queueUrl = findQueueUrlOrFailFrom(consumerProperties);
    ReceiveMessageRequest request = createMessageRequestFrom(consumerProperties, queueUrl);
    while (isDispatcherRunning) {
      try {
        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(request);
        LOGGER.trace("Message(s) received from queue: size={}", receiveMessageResult.getMessages().size());
        if (receiveMessageResult.getMessages().size() > 0) {
          deleteMessageService.deleteMessage(
            consumerProperties.deletePolicy(),
            receiveMessageResult,
            queueUrl,
            consumeMessage(consumer));
        }
      } catch (Exception e) {
        LOGGER.error("Error while consuming messages: queueName={}", queueUrl, e);
      }
    }
    LOGGER.info("Closing consumer: queueName={}", consumerProperties.value());
  }

  private String findQueueUrlOrFailFrom(SqsConsumer consumerProperties) {
    String queueUrl = "";
    try {
      GetQueueUrlResult foundQueue = sqsClient.getQueueUrl(consumerProperties.value());
      queueUrl = foundQueue.getQueueUrl();
    } catch (QueueDoesNotExistException e) {
      LOGGER.error("Queue doesn't exist: queueName={}", consumerProperties.value(), e);
      System.exit(-1);
    }
    return queueUrl;
  }

  private ReceiveMessageRequest createMessageRequestFrom(SqsConsumer consumerProperties, String queueUrl) {
    return new ReceiveMessageRequest(queueUrl)
      .withMaxNumberOfMessages(consumerProperties.maxMessagesPerPoll())
      .withWaitTimeSeconds(consumerProperties.longPolling());
  }

  private Function<ReceiveMessageResult, Boolean> consumeMessage(Consumer consumer) {
    return receiveMessageResult -> {
      try {
        Object[] consumerParameters = createConsumerParametersFrom(consumer.getConsumerMethod(), receiveMessageResult);
        consumer.getConsumerMethod().invoke(consumer.getConsumerInstance(), consumerParameters);
        return true;
      } catch (Exception e) {
        LOGGER.error("Error invoking method", e);
        return false;
      }
    };
  }

  private Object[] createConsumerParametersFrom(Method consumer, ReceiveMessageResult receiveMessageResult) {
    Object[] result;
    if (isOnlyBodyExpected(consumer)) {
      result = new Object[]{createFirstParameterFrom(consumer, receiveMessageResult)};
    } else {
      result = Stream.of(consumer.getParameters())
        .map(parameter -> toDeserializedObject(consumer, receiveMessageResult, parameter))
        .toArray();
    }
    return result;
  }

  private Object toDeserializedObject(Method consumer, ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    return Stream.of(parameter.getAnnotations())
      .filter(isAnySqsAnnotation())
      .findFirst()
      .map(annotation -> createParameterFrom(annotation, consumer, receiveMessageResult, parameter))
      .orElse(null); //Parameter initiated to null. Not happening as long as validation rules are in place
  }

  private Predicate<Annotation> isAnySqsAnnotation() {
    return annotation -> annotation.annotationType() == SqsBody.class || annotation.annotationType() == SqsAttribute.class;
  }

  private Object createFirstParameterFrom(Method consumer, ReceiveMessageResult receiveMessageResult) {
    return createParameterFrom(consumer, receiveMessageResult, consumer.getParameters()[0]);
  }

  private Object createParameterFrom(Annotation annotation, Method consumer, ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Object result;
    if (annotation.annotationType() == SqsBody.class) {
      result = createParameterFrom(consumer, receiveMessageResult, parameter);
    } else {
      result = getAttributeFrom(receiveMessageResult, (SqsAttribute) annotation);
    }
    return result;
  }

  private Object createParameterFrom(Method consumer, ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Object result;
    Class<?> argumentType = getParameterClassTypeFrom(consumer, parameter.getType());
    if (parameter.getType().equals(List.class)) {
      result = receiveMessageResult.getMessages().stream()
        .map(message -> messageMapper.deserialize(message.getBody(), argumentType))
        .collect(toList());
    } else {
      Validate.isTrue(receiveMessageResult.getMessages().size() == 1,
        "There can only be one message when parameter is not a list");
      Message message = receiveMessageResult.getMessages().get(0);
      result = messageMapper.deserialize(message.getBody(), argumentType);
    }
    return result;
  }

  private String getAttributeFrom(ReceiveMessageResult receiveMessageResult, SqsAttribute parameterAnnotation) {
    String attributeName = parameterAnnotation.value();
    Message message = receiveMessageResult.getMessages().get(0); //Only one message is allowed when using attributes
    return message.getAttributes().get(attributeName);
  }

  private boolean isOnlyBodyExpected(Method consumer) {
    return consumer.getParameters().length == 1;
  }

  private Class<?> getParameterClassTypeFrom(Method consumer, Class<?> parameterType) {
    Class<?> argumentType;
    if (parameterType.equals(List.class)) {
      Type[] types = consumer.getGenericParameterTypes();
      ParameterizedType pType = (ParameterizedType) types[0];
      argumentType = (Class<?>) pType.getActualTypeArguments()[0];
    } else {
      argumentType = parameterType;
    }
    return argumentType;
  }

  /**
   * Method invoked when closing the application. It waits for all consumers to finish their last request before closing them
   *
   * @throws InterruptedException
   */
  public void close() throws InterruptedException {
    LOGGER.info("Closing consumers");
    isDispatcherRunning = false;
    if (executor != null) {
      executor.awaitTermination(DEFAULT_MAX_LONG_POLLING_IN_SECONDS, SECONDS);
    }
  }

}
