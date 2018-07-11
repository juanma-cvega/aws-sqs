package org.jusoft.aws.sqs.validation.rule.impl;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.List;

/**
 * Validates that {@link com.amazonaws.services.sqs.model.ReceiveMessageRequest} is the only parameter when it is present.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ReceiveMessageResultOnlyParameterValidationRule implements ValidationRule {

  static final String RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR =
    "Cannot map to ReceiveMessageResult body in a multi parameter consumer. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return ConsumerValidationResult.of(isOnlyParameter(queueConsumer), queueConsumer);
  }

  private ErrorMessage isOnlyParameter(QueueConsumer queueConsumer) {
    return ErrorMessage.of(() -> isValidConsumer(queueConsumer),
      RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR, queueConsumer.getAnnotation().value());

  }

  private boolean isValidConsumer(QueueConsumer queueConsumer) {
    return isReceiveMessageResultOneParameter(queueConsumer.getParametersTypes()) && isSingleParameterConsumer(queueConsumer.getParametersTypes());
  }

  private boolean isReceiveMessageResultOneParameter(List<Class<?>> parametersType) {
    return parametersType.stream().anyMatch(parameterType -> parameterType == ReceiveMessageResult.class);
  }

  private boolean isSingleParameterConsumer(List<Class<?>> parametersType) {
    return parametersType.size() == 1;
  }
}
