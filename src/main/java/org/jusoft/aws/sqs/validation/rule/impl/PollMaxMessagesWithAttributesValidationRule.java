package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.stream.Stream;

public class PollMaxMessagesWithAttributesValidationRule implements ValidationRule {

  static final String MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR =
    "Number of messages to poll can only be one when attributes are expected. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return ConsumerValidationResult.of(isNumberMessagesRespectedWhenUsingAttributes(consumer), consumer);
  }

  private ErrorMessage isNumberMessagesRespectedWhenUsingAttributes(Consumer consumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    if (isSqsAttributePresent(consumer)) {
      errorMessage.addMessage(ErrorMessage.of(
        () -> consumer.getAnnotation().maxMessagesPerPoll() == 1, MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR, consumer.getAnnotation().value()));
    }
    return errorMessage;
  }

  private boolean isSqsAttributePresent(Consumer consumer) {
    return Stream.of(consumer.getConsumerMethod().getParameterAnnotations())
      .anyMatch(annotations -> Stream.of(annotations)
        .anyMatch(annotation -> annotation.annotationType() == SqsAttribute.class));
  }
}
