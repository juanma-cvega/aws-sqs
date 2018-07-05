package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

public class LongPollingValidationRule implements ValidationRule {

  static final int LONG_POLLING_DISABLED_VALUE = 0;
  static final int DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS = 20;
  static final String MINIMUM_LONG_POLLING_VALUE_ERROR = "Minimum long polling value is 0 (Disabled). Queue=%s";
  static final String MAXIMUM_LONG_POLLING_VALUE_ERROR = "Maximum long polling value is %s. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = ErrorMessage.noError()
      .addMessage(isMinimumLongPollingRespectedFor(queueConsumer.getAnnotation()))
      .addMessage(isMaximumLongPollingRespectedFor(queueConsumer.getAnnotation()));
    return ConsumerValidationResult.of(errorMessage, queueConsumer);
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
