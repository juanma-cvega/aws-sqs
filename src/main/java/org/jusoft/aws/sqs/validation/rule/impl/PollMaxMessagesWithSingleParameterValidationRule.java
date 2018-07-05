package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.List;

public class PollMaxMessagesWithSingleParameterValidationRule implements ValidationRule {

  static final int DEFAULT_MAX_MESSAGES_PER_POLL = 10;
  static final int DEFAULT_MIN_MESSAGES_PER_POLL = 1;
  static final String MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR = "Minimum number of messages per poll is %s. Queue=%s";
  static final String MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR = "Maximum number of messages per poll is %s. Queue=%s";
  static final String NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR =
    "Cannot map to single instance parameter when maxMessagesPerPoll is more than 1. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    if (isSingleParameterConsumer(consumer)) {
      errorMessage = errorMessage.addMessage(isMinimumMessagesRespectedFor(consumer.getAnnotation()))
        .addMessage(isMaximumMessagesRespectedFor(consumer.getAnnotation()))
        .addMessage(isMessagesRespectedWhenArgumentIsAListFor(consumer));
    }
    return ConsumerValidationResult.of(errorMessage, consumer);
  }

  private boolean isSingleParameterConsumer(Consumer consumer) {
    return consumer.getParametersTypes().size() == 1;
  }

  private ErrorMessage isMinimumMessagesRespectedFor(SqsConsumer annotation) {
    return annotation.maxMessagesPerPoll() >= DEFAULT_MIN_MESSAGES_PER_POLL
      ? ErrorMessage.noError()
      : ErrorMessage.of(MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, DEFAULT_MIN_MESSAGES_PER_POLL, annotation.value());
  }

  private ErrorMessage isMaximumMessagesRespectedFor(SqsConsumer annotation) {
    return annotation.maxMessagesPerPoll() <= DEFAULT_MAX_MESSAGES_PER_POLL
      ? ErrorMessage.noError()
      : ErrorMessage.of(MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, DEFAULT_MAX_MESSAGES_PER_POLL, annotation.value());
  }

  private ErrorMessage isMessagesRespectedWhenArgumentIsAListFor(Consumer consumer) {
    Class<?> parameterType = consumer.getParametersTypes().get(0);
    return ErrorMessage.of(() -> isNotCollectionParameterWithMaxMessagesEqualToOne(parameterType, consumer.getAnnotation()),
      NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR, consumer.getAnnotation().value());
  }

  private boolean isNotCollectionParameterWithMaxMessagesEqualToOne(Class<?> parameterType, SqsConsumer annotation) {
    return !(parameterType != List.class && annotation.maxMessagesPerPoll() > 1);
  }
}
