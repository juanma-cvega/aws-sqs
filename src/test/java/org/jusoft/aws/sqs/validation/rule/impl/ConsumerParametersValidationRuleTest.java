package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.ConsumerParametersValidationRule.MINIMUM_PARAMETERS_VALUE_ERROR;

public class ConsumerParametersValidationRuleTest extends AbstractValidationRuleTest {

  private final ConsumerParametersValidationRule rule = new ConsumerParametersValidationRule();

  @Test
  public void whenConsumerHasAtLeastMinimumNumberOfParametersThenTheConsumerIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestValidConsumer()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenConsumerHasZeroParametersThenTheConsumerIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestZeroArguments()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MINIMUM_PARAMETERS_VALUE_ERROR, QUEUE_NAME));
  }

  private static class TestValidConsumer {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(Object object) {

    }
  }
}
