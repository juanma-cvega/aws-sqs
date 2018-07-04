package org.jusoft.aws.sqs.validation.rules;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

public class LongPollingValidationRule implements ValidationRule {

  static final int LONG_POLLING_DISABLED_VALUE = 0;
  static final int DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS = 20;
  static final String MINIMUM_LONG_POLLING_VALUE_ERROR = "Minimum long polling value is 0 (Disabled). Queue=%s";
  static final String MAXIMUM_LONG_POLLING_VALUE_ERROR = "Maximum long polling value is %s. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    ErrorMessage errorMessage = ErrorMessage.noError()
      .addMessage(isMinimumLongPollingRespectedFor(consumer.getAnnotation()))
      .addMessage(isMaximumLongPollingRespectedFor(consumer.getAnnotation()));
    return ConsumerValidationResult.of(errorMessage, consumer);
  }

  private ErrorMessage isMinimumLongPollingRespectedFor(SqsConsumer annotation) {
    return annotation.longPolling() >= LONG_POLLING_DISABLED_VALUE
      ? ErrorMessage.noError()
      : ErrorMessage.of(MINIMUM_LONG_POLLING_VALUE_ERROR, annotation.value());
  }

  private ErrorMessage isMaximumLongPollingRespectedFor(SqsConsumer annotation) {
    return annotation.longPolling() <= DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS
      ? ErrorMessage.noError()
      : ErrorMessage.of(MAXIMUM_LONG_POLLING_VALUE_ERROR, DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS, annotation.value());
  }
}
