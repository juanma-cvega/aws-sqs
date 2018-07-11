package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;

/**
 * Created a {@link ExecutorService} based on the {@link SqsConsumer} annotations passed. It's up to the implementation
 * to decide, based on the passed list, what kind of {@link ExecutorService} to provide to run each of the consumers
 * started with the application.
 *
 * @author Juan Manuel Carnicero Vega
 */
public interface ExecutorFactory {

  /**
   * Creates a {@link ExecutorService} to be used to start the consumers of the AWS SQS queues.
   *
   * @param consumerProperties Configuration of the AWS SQS consumers defined in the application.
   */
  ExecutorService createFor(Iterable<SqsConsumer> consumerProperties);
}
