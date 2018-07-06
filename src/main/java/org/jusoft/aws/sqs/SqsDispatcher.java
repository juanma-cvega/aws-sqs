package org.jusoft.aws.sqs;

import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.executor.ExecutorFactory;
import org.jusoft.aws.sqs.provider.ConsumersInstanceProvider;
import org.jusoft.aws.sqs.service.QueuePollService;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.StreamSupport;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.jusoft.aws.sqs.annotation.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

public class SqsDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsDispatcher.class);

  private final QueuePollService queuePollService;
  private final ConsumersInstanceProvider consumersProvider;
  private final ExecutorFactory executorFactory;
  private final ConsumerValidator consumerValidator;

  private ExecutorService executor;

  public SqsDispatcher(QueuePollService queuePollService,
                       ConsumersInstanceProvider consumersProvider,
                       ExecutorFactory executorFactory,
                       ConsumerValidator consumerValidator) {
    this.queuePollService = queuePollService;
    this.consumersProvider = consumersProvider;
    this.executorFactory = executorFactory;
    this.consumerValidator = consumerValidator;
  }

  @PostConstruct
  public void subscribeConsumers() {
    LOGGER.info("Initializing SQS consumers");
    Iterable<QueueConsumer> consumers = consumersProvider.getConsumers();

    if (consumers.iterator().hasNext()) {
      consumerValidator.isValid(consumers);
      executor = executorFactory.createFor(getAnnotationsFrom(consumers));
      consumers.forEach(consumer -> executor.submit(() -> queuePollService.start(consumer)));
    }
  }

  private List<SqsConsumer> getAnnotationsFrom(Iterable<QueueConsumer> consumers) {
    return StreamSupport.stream(consumers.spliterator(), false)
      .map(QueueConsumer::getAnnotation)
      .collect(toList());
  }

  /**
   * Method invoked when closing the application. It waits for all consumers to finish their last request before closing them
   *
   * @throws InterruptedException
   */
  public void close() throws InterruptedException {
    LOGGER.info("Closing consumers");
    queuePollService.close();
    if (executor != null) {
      executor.awaitTermination(DEFAULT_MAX_LONG_POLLING_IN_SECONDS, SECONDS);
    }
  }

}
