package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.stream.Stream;

public class PollMaxMessagesWithAttributesValidationRule implements ValidationRule {

  static final String MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR =
    "Number of messages to poll can only be one when attributes are expected. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return ConsumerValidationResult.of(isNumberMessagesRespectedWhenUsingAttributes(queueConsumer), queueConsumer);
  }

  private ErrorMessage isNumberMessagesRespectedWhenUsingAttributes(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    if (isSqsAttributePresent(queueConsumer)) {
      errorMessage.addMessage(ErrorMessage.of(
        () -> queueConsumer.getAnnotation().maxMessagesPerPoll() == 1, MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR, queueConsumer.getAnnotation().value()));
    }
    return errorMessage;
  }

  private boolean isSqsAttributePresent(QueueConsumer queueConsumer) {
    return Stream.of(queueConsumer.getConsumerMethod().getParameterAnnotations())
      .anyMatch(annotations -> Stream.of(annotations)
        .anyMatch(annotation -> annotation.annotationType() == SqsAttribute.class));
  }
}
