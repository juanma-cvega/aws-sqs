package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

public class ConsumerParametersValidationRule implements ValidationRule {
  static final String MINIMUM_PARAMETERS_VALUE_ERROR = "A consumer method must have at least one argument. Queue=%s";
  private static final int MINIMUM_PARAMETERS = 0;

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    ErrorMessage errorMessage = isMinimumConcurrentConsumersRespected(consumer);
    return ConsumerValidationResult.of(errorMessage, consumer);
  }

  private ErrorMessage isMinimumConcurrentConsumersRespected(Consumer consumer) {
    return consumer.getParametersTypes().size() > MINIMUM_PARAMETERS
      ? ErrorMessage.noError()
      : ErrorMessage.of(MINIMUM_PARAMETERS_VALUE_ERROR, consumer.getAnnotation().value());
  }
}
