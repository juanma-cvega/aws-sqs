package org.jusoft.aws.sqs.validation.rules;

import org.junit.Test;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.validation.rules.LongPollingValidationRule.DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS;
import static org.jusoft.aws.sqs.validation.rules.LongPollingValidationRule.LONG_POLLING_DISABLED_VALUE;
import static org.jusoft.aws.sqs.validation.rules.LongPollingValidationRule.MAXIMUM_LONG_POLLING_VALUE_ERROR;
import static org.jusoft.aws.sqs.validation.rules.LongPollingValidationRule.MINIMUM_LONG_POLLING_VALUE_ERROR;

public class LongPollingValidationRuleTest extends AbstractValidationRuleTest {

  private final LongPollingValidationRule rule = new LongPollingValidationRule();

  @Test
  public void whenLongPollingValueIsDefaultThenTheResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestMaxLongPollingConsumer()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenLongPollingValueIsDisabledThenTheResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestDisableLongPollingConsumer()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenLongPollingValueIsHigherThanMaximumThenTheResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestMaxLongPollingSurpassedConsumer()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MAXIMUM_LONG_POLLING_VALUE_ERROR,
      DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS, QUEUE_NAME));
  }

  @Test
  public void whenLongPollingValueIsLowerThanMinimumThenTheResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestMinLongPollingNotReachedConsumer()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MINIMUM_LONG_POLLING_VALUE_ERROR, QUEUE_NAME));
  }

  @Test
  public void whenThereAreNoParametersTheResultShouldBeValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestZeroArguments()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  private static class TestMaxLongPollingConsumer {
    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer() {

    }
  }

  private static class TestDisableLongPollingConsumer {
    @SqsConsumer(value = QUEUE_NAME, longPolling = LONG_POLLING_DISABLED_VALUE)
    public void testConsumer() {

    }
  }

  private static class TestMaxLongPollingSurpassedConsumer {
    @SqsConsumer(value = QUEUE_NAME, longPolling = DEFAULT_MAX_LONG_POLLING_VALUE_IN_SECONDS + 1)
    public void testConsumer() {

    }
  }

  private static class TestMinLongPollingNotReachedConsumer {
    @SqsConsumer(value = QUEUE_NAME, longPolling = -1)
    public void testConsumer() {

    }
  }
}
