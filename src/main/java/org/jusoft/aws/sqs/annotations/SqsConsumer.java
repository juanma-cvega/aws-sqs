package org.jusoft.aws.sqs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqsConsumer {

  int DEFAULT_MAX_LONG_POLLING_IN_SECONDS = 20;
  int DEFAULT_MAX_MESSAGES_PER_POLL = 1;
  int DEFAULT_CONCURRENT_CONSUMERS = 1;

  String value();

  int longPolling() default DEFAULT_MAX_LONG_POLLING_IN_SECONDS;

  int maxMessagesPerPoll() default DEFAULT_MAX_MESSAGES_PER_POLL;

  int concurrentConsumers() default DEFAULT_CONCURRENT_CONSUMERS;

  DeletePolicy deletePolicy() default DeletePolicy.AFTER_PROCESS;


}
