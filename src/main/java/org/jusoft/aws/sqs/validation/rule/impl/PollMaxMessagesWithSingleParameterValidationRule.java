package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.List;

import static org.jusoft.aws.sqs.annotation.SqsConsumer.MAX_MESSAGES_PER_POLL_ALLOWED;

/**
 * Validates that:
 * <li>
 * <ul>The minimum number of messages (1) is respected in {@link SqsConsumer#maxMessagesPerPoll()}</ul>
 * <ul>The maximum number of messages (10) is respected in {@link SqsConsumer#maxMessagesPerPoll()}</ul>
 * <ul>The body parameter in the consumer method is a {@link List} when {@link SqsConsumer#maxMessagesPerPoll()}
 * is higher than 1</ul>
 * </li>
 *
 * @author Juan Manuel Carnicero Vega
 */
public class PollMaxMessagesWithSingleParameterValidationRule implements ValidationRule {

  static final int DEFAULT_MIN_MESSAGES_PER_POLL = 1;
  static final String MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR = "Minimum number of messages per poll is %s. Queue=%s";
  static final String MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR = "Maximum number of messages per poll is %s. Queue=%s";
  static final String NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR =
    "Cannot map to single instance parameter when maxMessagesPerPoll is more than 1. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    if (isSingleParameterConsumer(queueConsumer)) {
      errorMessage = errorMessage.addMessage(isMinimumMessagesRespectedFor(queueConsumer.getAnnotation()))
        .addMessage(isMaximumMessagesRespectedFor(queueConsumer.getAnnotation()))
        .addMessage(isMessagesRespectedWhenArgumentIsAListFor(queueConsumer));
    }
    return ConsumerValidationResult.of(errorMessage, queueConsumer);
  }

  private boolean isSingleParameterConsumer(QueueConsumer queueConsumer) {
    return queueConsumer.getParametersTypes().size() == 1;
  }

  private ErrorMessage isMinimumMessagesRespectedFor(SqsConsumer annotation) {
    return annotation.maxMessagesPerPoll() >= DEFAULT_MIN_MESSAGES_PER_POLL
      ? ErrorMessage.noError()
      : ErrorMessage.of(MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, DEFAULT_MIN_MESSAGES_PER_POLL, annotation.value());
  }

  private ErrorMessage isMaximumMessagesRespectedFor(SqsConsumer annotation) {
    return annotation.maxMessagesPerPoll() <= MAX_MESSAGES_PER_POLL_ALLOWED
      ? ErrorMessage.noError()
      : ErrorMessage.of(MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, MAX_MESSAGES_PER_POLL_ALLOWED, annotation.value());
  }

  private ErrorMessage isMessagesRespectedWhenArgumentIsAListFor(QueueConsumer queueConsumer) {
    Class<?> parameterType = queueConsumer.getParametersTypes().get(0);
    return ErrorMessage.of(() -> isNotCollectionParameterWithMaxMessagesEqualToOne(parameterType, queueConsumer.getAnnotation()),
      NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR, queueConsumer.getAnnotation().value());
  }

  private boolean isNotCollectionParameterWithMaxMessagesEqualToOne(Class<?> parameterType, SqsConsumer annotation) {
    return !(parameterType != List.class && annotation.maxMessagesPerPoll() > 1);
  }
}
