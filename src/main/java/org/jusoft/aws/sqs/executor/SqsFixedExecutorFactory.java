package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.StreamSupport.stream;

public class SqsFixedExecutorFactory implements SqsExecutorFactory {

  @Override
  public ExecutorService createFor(Iterable<Consumer> consumerProperties) {
    return Executors.newFixedThreadPool(getTotalConsumerThreadsFrom(consumerProperties));
  }

  private int getTotalConsumerThreadsFrom(Iterable<Consumer> consumers) {
    return stream(consumers.spliterator(), false)
      .map(Consumer::getConsumerMethod)
      .filter(method -> method.isAnnotationPresent(SqsConsumer.class))
      .map(method -> method.getAnnotation(SqsConsumer.class))
      .mapToInt(SqsConsumer::concurrentConsumers)
      .sum();
  }
}
