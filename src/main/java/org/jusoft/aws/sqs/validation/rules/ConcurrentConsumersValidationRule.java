package org.jusoft.aws.sqs.validation.rules;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

import static org.jusoft.aws.sqs.validation.ErrorMessage.noError;

public class ConcurrentConsumersValidationRule implements ValidationRule {

  static final String MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR = "The number of concurrent consumers must be greater than 0. Queue=%s";
  private static final int MINIMUM_CONCURRENT_CONSUMERS = 0;

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return ConsumerValidationResult.of(isMinimumConcurrentConsumersRespected(consumer.getAnnotation()), consumer);
  }

  private ErrorMessage isMinimumConcurrentConsumersRespected(SqsConsumer annotation) {
    return annotation.concurrentConsumers() > MINIMUM_CONCURRENT_CONSUMERS
      ? noError()
      : ErrorMessage.of(MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR, annotation.value());
  }
}
