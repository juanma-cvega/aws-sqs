package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.ConsumerAccessibleValidationRule.CONSUMER_METHOD_NOT_ACCESSIBLE;

public class ConsumerAccessibleValidationRuleTest extends AbstractValidationRuleTest {

  private final ConsumerAccessibleValidationRule rule = new ConsumerAccessibleValidationRule();

  @Test
  public void whenConsumerIsAccessibleThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerAccessibleValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenConsumerIsPrivateThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerPrivateInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(CONSUMER_METHOD_NOT_ACCESSIBLE, QUEUE_NAME));
  }

  @Test
  public void whenConsumerIsProtectedThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerProtectedInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(CONSUMER_METHOD_NOT_ACCESSIBLE, QUEUE_NAME));
  }

  @Test
  public void whenConsumerIsPackageThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerPackageInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(CONSUMER_METHOD_NOT_ACCESSIBLE, QUEUE_NAME));
  }

  private static class TestConsumerAccessibleValid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer() {

    }
  }

  private static class TestConsumerPrivateInvalid {

    @SqsConsumer(QUEUE_NAME)
    private void testConsumer() {

    }
  }

  private static class TestConsumerProtectedInvalid {

    @SqsConsumer(QUEUE_NAME)
    protected void testConsumer() {

    }
  }

  private static class TestConsumerPackageInvalid {

    @SqsConsumer(QUEUE_NAME)
    void testConsumer() {

    }
  }
}
