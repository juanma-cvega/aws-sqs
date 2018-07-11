package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.ConcurrentConsumersValidationRule.MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR;

public class ConcurrentConsumersValidationRuleTest extends AbstractValidationRuleTest {

  private final ConcurrentConsumersValidationRule rule = new ConcurrentConsumersValidationRule();

  @Test
  public void whenConcurrentConsumersIsValidThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestValidConsumer()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenConcurrentConsumersIsLowerThanMinThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestInvalidConcurrentConsumers()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MINIMUM_CONCURRENT_CONSUMERS_VALUE_ERROR, QUEUE_NAME));
  }

  @Test
  public void whenThereAreNoParametersTheResultShouldBeValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestZeroArguments()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  private static class TestValidConsumer {
    private TestDto testValue;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(TestDto testParameter) {
      testValue = testParameter;
    }
  }

  private static class TestInvalidConcurrentConsumers {
    private TestDto testValue;

    @SqsConsumer(value = QUEUE_NAME, concurrentConsumers = 0)
    public void testConsumer(TestDto testParameter) {
      testValue = testParameter;
    }
  }

  private class TestDto {

  }
}
