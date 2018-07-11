package org.jusoft.aws.sqs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be used as the consumer of messages sent to the SQS queue specified in the value of the annotation.
 * The value <b>must</b> reference the name of the queue, which then will be used to find its URL. If the queue does not
 * exist for the given Amazon AWS account, a {@link System#exit(int)} will be issued, preventing the application from
 * starting until either any configuration issue is fixed or the queue is created for the given configuration.
 *
 * @author Juan Manuel Carnicero Vega
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqsConsumer {

  /**
   * Default max polling value based on the AWS SQS documentation
   */
  int DEFAULT_MAX_LONG_POLLING_IN_SECONDS = 20;

  /**
   * Value to use short polling. Anything below this is not permitted.
   */
  int SHORT_POLLING_VALUE = 0;

  int DEFAULT_MAX_MESSAGES_PER_POLL = 1;

  /**
   * Maximum number of messages allowed to be fetched from AWS SQS in one call according to the documentation
   */
  int MAX_MESSAGES_PER_POLL_ALLOWED = 10;
  int DEFAULT_CONCURRENT_CONSUMERS = 1;

  /**
   * The name of the SQS queue to consume messages from
   */
  String value();

  /**
   * Waiting time to fetch messages from the queue. By default, its value is the maximum waiting time allowed according
   * to the AWS SQS documentation. Use SHORT_POLLING_VALUE to disable long polling.
   */
  int longPolling() default DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

  /**
   * The maximum number of messages to fetch in one call to the AWS SQS queue. Defaults a single message by call.
   */
  int maxMessagesPerPoll() default DEFAULT_MAX_MESSAGES_PER_POLL;

  /**
   * Number of threads to start to consume from the AWS SQS queue specify in the annotation. Each thread will use the
   * same configuration specified in the annotation to fetch messages from the queue.
   */
  int concurrentConsumers() default DEFAULT_CONCURRENT_CONSUMERS;

  /**
   * Policy to be used when messages are to be deleted from the queue.
   */
  DeletePolicy deletePolicy() default DeletePolicy.AFTER_PROCESS;

  //TODO add visibilityTimeout option

  //TODO add attribute names

  //TODO add FIFO configuration
}
