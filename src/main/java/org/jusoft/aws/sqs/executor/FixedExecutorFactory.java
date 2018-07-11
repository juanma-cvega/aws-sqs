package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.StreamSupport.stream;

/**
 * Creates an {@link ExecutorService} with a fixed number of threads. The number is calculated by summing up the
 * concurrent consumers property from each of the AWS SQS queue consumer configuration.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class FixedExecutorFactory implements ExecutorFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  public ExecutorService createFor(Iterable<SqsConsumer> consumerProperties) {
    return Executors.newFixedThreadPool(getTotalConsumerThreadsFrom(consumerProperties));
  }

  private int getTotalConsumerThreadsFrom(Iterable<SqsConsumer> consumers) {
    return stream(consumers.spliterator(), false)
      .mapToInt(SqsConsumer::concurrentConsumers)
      .sum();
  }
}
