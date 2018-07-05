package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.validation.rule.impl.PollMaxMessagesWithSingleParameterValidationRule.DEFAULT_MAX_MESSAGES_PER_POLL;
import static org.jusoft.aws.sqs.validation.rule.impl.PollMaxMessagesWithSingleParameterValidationRule.DEFAULT_MIN_MESSAGES_PER_POLL;
import static org.jusoft.aws.sqs.validation.rule.impl.PollMaxMessagesWithSingleParameterValidationRule.MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR;
import static org.jusoft.aws.sqs.validation.rule.impl.PollMaxMessagesWithSingleParameterValidationRule.MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR;
import static org.jusoft.aws.sqs.validation.rule.impl.PollMaxMessagesWithSingleParameterValidationRule.NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR;

public class PollMaxMessagesWithSingleParameterValidationRuleTest extends AbstractValidationRuleTest {

  private final PollMaxMessagesWithSingleParameterValidationRule rule = new PollMaxMessagesWithSingleParameterValidationRule();

  @Test
  public void whenConsumerMaxMessagePollIsDefaultThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerDefaultPollMessages()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenConsumerMaxMessagePollIsLowerThanMinimumThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerLessThanMinimumPollMessages()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(
      String.format(MINIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, DEFAULT_MIN_MESSAGES_PER_POLL, QUEUE_NAME));
  }

  @Test
  public void whenConsumerMaxMessagePollIsGreaterThanMaximumThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerGreaterThanMaximumPollMessages()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(
      String.format(MAXIMUM_NUMBER_OF_MESSAGES_PER_POLL_ERROR, DEFAULT_MAX_MESSAGES_PER_POLL, QUEUE_NAME));
  }

  @Test
  public void whenListConsumerWithNotListArgumentThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestListParameterInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(
      String.format(NO_COLLECTION_PARAMETER_WITH_MAX_MESSAGES_GREATER_THAN_ONE_ERROR, QUEUE_NAME));
  }

  private static class TestConsumerDefaultPollMessages {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(Object object) {

    }
  }

  private static class TestConsumerLessThanMinimumPollMessages {

    @SqsConsumer(value = QUEUE_NAME, maxMessagesPerPoll = DEFAULT_MIN_MESSAGES_PER_POLL - 1)
    public void testConsumer(Object object) {

    }
  }

  private static class TestConsumerGreaterThanMaximumPollMessages {

    @SqsConsumer(value = QUEUE_NAME, maxMessagesPerPoll = DEFAULT_MAX_MESSAGES_PER_POLL + 1)
    public void testConsumer(List<Object> object) {

    }
  }

  private static class TestListParameterInvalid {

    @SqsConsumer(value = QUEUE_NAME, maxMessagesPerPoll = DEFAULT_MAX_MESSAGES_PER_POLL)
    public void testConsumer(Object object) {

    }
  }
}
