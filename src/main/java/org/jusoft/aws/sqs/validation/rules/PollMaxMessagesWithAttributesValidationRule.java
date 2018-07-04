package org.jusoft.aws.sqs.validation.rules;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

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
