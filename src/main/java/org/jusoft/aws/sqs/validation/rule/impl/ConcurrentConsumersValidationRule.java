package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import static org.jusoft.aws.sqs.validation.rule.ErrorMessage.noError;

/**
 * Validates the minimum number of consumers is respected in the {@link SqsConsumer#concurrentConsumers()} field.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ConcurrentConsumersValidationRule implements ValidationRule {

  static final String MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR = "The number of concurrent consumers must be greater than 0. Queue=%s";
  private static final int MINIMUM_CONCURRENT_CONSUMERS = 0;

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return ConsumerValidationResult.of(isMinimumConcurrentConsumersRespected(queueConsumer.getAnnotation()), queueConsumer);
  }

  private ErrorMessage isMinimumConcurrentConsumersRespected(SqsConsumer annotation) {
    return annotation.concurrentConsumers() > MINIMUM_CONCURRENT_CONSUMERS
      ? noError()
      : ErrorMessage.of(MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR, annotation.value());
  }
}
