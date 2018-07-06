package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.StreamSupport.stream;

public class FixedExecutorFactory implements ExecutorFactory {

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
