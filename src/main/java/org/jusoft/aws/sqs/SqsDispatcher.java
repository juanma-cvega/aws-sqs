package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.annotations.SqsBody;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.executor.SqsExecutorFactory;
import org.jusoft.aws.sqs.provider.ConsumerInstanceProvider;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.jusoft.aws.sqs.annotations.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

public class SqsDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsDispatcher.class);

  private final AmazonSQS sqsClient;
  private final ObjectMapper objectMapper;
  private final DeleteMessageService deleteMessageService;
  private final ConsumerInstanceProvider consumersProvider;
  private final SqsExecutorFactory sqsExecutorFactory;
  private final ConsumerValidator consumerValidator;

  private ExecutorService executor;
  private boolean isDispatcherRunning;


  public SqsDispatcher(AmazonSQS sqsClient,
                       ObjectMapper objectMapper,
                       DeleteMessageService deleteMessageService,
                       ConsumerInstanceProvider consumersProvider,
                       SqsExecutorFactory sqsExecutorFactory,
                       ConsumerValidator consumerValidator) {
    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
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
        if (receiveMessageResult.getMessages().size() > 0) {
          LOGGER.trace("Message(s) received from queue: size={}", receiveMessageResult.getMessages().size());
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
        Object methodParameter = createConsumerParameterFrom(consumer.getConsumerMethod(), receiveMessageResult);
        if (methodParameter instanceof Object[]) {
          consumer.getConsumerMethod().invoke(consumer.getConsumerInstance(), (Object[]) methodParameter);
        } else {
          consumer.getConsumerMethod().invoke(consumer.getConsumerInstance(), methodParameter);
        }
        return true;
      } catch (Exception e) {
        LOGGER.error("Error invoking method", e);
        return false;
      }
    };
  }

  private Object createConsumerParameterFrom(Method consumer, ReceiveMessageResult receiveMessageResult) throws IOException {
    if (isOnlyBodyExpected(consumer)) {
      Class<?> consumerParameter = consumer.getParameterTypes()[0];
      Class<?> argumentType = getParameterClassTypeFrom(consumer, consumerParameter);
      if (consumerParameter.equals(List.class)) {
        return receiveMessageResult.getMessages().stream()
          .map(message -> toListenerParameter(argumentType, message))
          .collect(toList());
      } else {
        Message message = receiveMessageResult.getMessages().get(0);
        return objectMapper.readValue(message.getBody(), argumentType);
      }
    } else {
      List<Object> parameters = new ArrayList<>();
      Annotation[][] parametersAnnotations = consumer.getParameterAnnotations();
      for (int parameterIndex = 0; parameterIndex < parametersAnnotations.length; parameterIndex++) {
        for (Annotation parameterAnnotation : parametersAnnotations[parameterIndex]) {
          if (parameterAnnotation.annotationType() == SqsBody.class) {
            Class<?> consumerParameter = consumer.getParameterTypes()[parameterIndex];
            Class<?> argumentType = getParameterClassTypeFrom(consumer, consumerParameter);
            if (consumerParameter.equals(List.class)) {
              parameters.add(receiveMessageResult.getMessages().stream()
                .map(message -> toListenerParameter(argumentType, message))
                .collect(toList()));
            } else {
              Message message = receiveMessageResult.getMessages().get(0);
              parameters.add(objectMapper.readValue(message.getBody(), argumentType));
            }
          } else if (parameterAnnotation.annotationType() == SqsAttribute.class) {
            String attributeName = ((SqsAttribute) parameterAnnotation).value();
            Message message = receiveMessageResult.getMessages().get(0);
            String attributeValue = message.getAttributes().get(attributeName); //TODO addMessage option to return a specific type
            parameters.add(attributeValue);
          }
        }
      }
      return parameters.toArray();
    }
  }

  private boolean isOnlyBodyExpected(Method consumer) {
    return consumer.getParameterTypes().length == 1;
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

  private Object toListenerParameter(Class<?> argumentType, Message message) {
    try {
      return objectMapper.readValue(message.getBody(), argumentType);
    } catch (IOException e) {
      throw new RuntimeException("Unable to deserialize body", e);
    }
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
