package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

/**
 * Validates the consumer method has at least one argument.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ConsumerParametersValidationRule implements ValidationRule {
  static final String MINIMUM_PARAMETERS_VALUE_ERROR = "A consumer method must have at least one argument. Queue=%s";
  private static final int MINIMUM_PARAMETERS = 0;

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = isMinimumConcurrentConsumersRespected(queueConsumer);
    return ConsumerValidationResult.of(errorMessage, queueConsumer);
  }

  private ErrorMessage isMinimumConcurrentConsumersRespected(QueueConsumer queueConsumer) {
    return queueConsumer.getParametersTypes().size() > MINIMUM_PARAMETERS
      ? ErrorMessage.noError()
      : ErrorMessage.of(MINIMUM_PARAMETERS_VALUE_ERROR, queueConsumer.getAnnotation().value());
  }
}
