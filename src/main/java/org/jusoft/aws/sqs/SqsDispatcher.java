package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.executor.ExecutorFactory;
import org.jusoft.aws.sqs.mapper.ConsumerParametersMapper;
import org.jusoft.aws.sqs.provider.ConsumersInstanceProvider;
import org.jusoft.aws.sqs.service.MessageConsumerService;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jusoft.aws.sqs.annotation.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

public class SqsDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsDispatcher.class);

  private final AmazonSQS amazonSQS;
  private final ConsumerParametersMapper consumerParametersMapper;
  private final MessageConsumerService messageConsumerService;
  private final ConsumersInstanceProvider consumersProvider;
  private final ExecutorFactory executorFactory;
  private final ConsumerValidator consumerValidator;
  private final ReceiveMessageRequestFactory receiveMessageRequestFactory;

  private ExecutorService executor;
  private boolean isDispatcherRunning;


  public SqsDispatcher(AmazonSQS amazonSQS,
                       ConsumerParametersMapper consumerParametersMapper,
                       MessageConsumerService messageConsumerService,
                       ConsumersInstanceProvider consumersProvider,
                       ExecutorFactory executorFactory,
                       ConsumerValidator consumerValidator,
                       ReceiveMessageRequestFactory receiveMessageRequestFactory) {
    this.amazonSQS = amazonSQS;
    this.consumerParametersMapper = consumerParametersMapper;
    this.messageConsumerService = messageConsumerService;
    this.consumersProvider = consumersProvider;
    this.executorFactory = executorFactory;
    this.consumerValidator = consumerValidator;
    this.receiveMessageRequestFactory = receiveMessageRequestFactory;
  }

  @PostConstruct
  public void subscribeConsumers() {
    LOGGER.info("Initializing SQS consumers");
    Iterable<QueueConsumer> consumers = consumersProvider.getConsumers();

    consumerValidator.isValid(consumers);

    isDispatcherRunning = true;
    if (consumers.iterator().hasNext()) {
      executor = executorFactory.createFor(consumers);
      consumers.forEach(consumer -> executor.submit(() -> start(consumer)));
    }
  }

  private void start(QueueConsumer queueConsumer) {
    String queueName = queueConsumer.getAnnotation().value();
    LOGGER.info("Starting queueConsumer: queue={}", queueName);

    ReceiveMessageRequest request = receiveMessageRequestFactory.createFrom(queueConsumer);
    while (isDispatcherRunning) {
      try {
        getAndConsumeMessages(queueConsumer, request);
      } catch (Exception e) {
        LOGGER.error("Error while consuming message(s): queueName={}", request.getQueueUrl(), e);
      }
    }
    LOGGER.info("Closing queueConsumer: queueName={}", queueName);
  }

  private void getAndConsumeMessages(QueueConsumer queueConsumer, ReceiveMessageRequest request) {
    ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(request);
    LOGGER.trace("Message(s) received from queue: size={}", receiveMessageResult.getMessages().size());
    if (!receiveMessageResult.getMessages().isEmpty()) {
      messageConsumerService.consumeAndDeleteMessage(
        queueConsumer.getAnnotation().deletePolicy(),
        receiveMessageResult,
        request.getQueueUrl(),
        consumeMessageBy(queueConsumer));
    }
  }

  private Consumer<ReceiveMessageResult> consumeMessageBy(QueueConsumer queueConsumer) {
    return receiveMessageResult -> {
      try {
        Object[] consumerParameters = consumerParametersMapper.createFrom(queueConsumer.getConsumerMethod(), receiveMessageResult);
        queueConsumer.getConsumerMethod().invoke(queueConsumer.getConsumerInstance(), consumerParameters);
      } catch (Exception e) {
        LOGGER.error("Error invoking method", e);
        throw new RuntimeException(String.format("Unable to consume message: queue=%s", queueConsumer.getAnnotation().value()), e);
      }
    };
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
