package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.StreamSupport.stream;

public class FixedExecutorFactory implements ExecutorFactory {

  @Override
  public ExecutorService createFor(Iterable<QueueConsumer> consumerProperties) {
    return Executors.newFixedThreadPool(getTotalConsumerThreadsFrom(consumerProperties));
  }

  private int getTotalConsumerThreadsFrom(Iterable<QueueConsumer> consumers) {
    return stream(consumers.spliterator(), false)
      .map(QueueConsumer::getConsumerMethod)
      .filter(method -> method.isAnnotationPresent(SqsConsumer.class))
      .map(method -> method.getAnnotation(SqsConsumer.class))
      .mapToInt(SqsConsumer::concurrentConsumers)
      .sum();
  }
}
