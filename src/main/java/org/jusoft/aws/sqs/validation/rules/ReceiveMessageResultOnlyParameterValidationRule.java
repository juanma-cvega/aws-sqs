package org.jusoft.aws.sqs.validation.rules;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

import java.util.List;

public class ReceiveMessageResultOnlyParameterValidationRule implements ValidationRule {

  static final String RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR =
    "Cannot map to ReceiveMessageResult body in a multi parameter consumer. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return ConsumerValidationResult.of(isOnlyParameter(consumer), consumer);
  }

  private ErrorMessage isOnlyParameter(Consumer consumer) {
    return ErrorMessage.of(() -> isValidConsumer(consumer),
      RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR, consumer.getAnnotation().value());

  }

  private boolean isValidConsumer(Consumer consumer) {
    return isReceiveMessageResultOneParameter(consumer.getParametersTypes()) && isSingleParameterConsumer(consumer.getParametersTypes());
  }

  private boolean isReceiveMessageResultOneParameter(List<Class<?>> parametersType) {
    return parametersType.stream().anyMatch(parameterType -> parameterType == ReceiveMessageResult.class);
  }

  private boolean isSingleParameterConsumer(List<Class<?>> parametersType) {
    return parametersType.size() == 1;
  }
}
