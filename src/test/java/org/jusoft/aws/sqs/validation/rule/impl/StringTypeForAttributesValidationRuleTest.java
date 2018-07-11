package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.MultipleParametersMethodClass;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import java.lang.reflect.Method;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.StringTypeForAttributesValidationRule.ATTRIBUTE_TYPE_INVALID_ERROR;

public class StringTypeForAttributesValidationRuleTest extends AbstractValidationRuleTest {

  private final StringTypeForAttributesValidationRule rule = new StringTypeForAttributesValidationRule();

  @Test
  public void whenAttributeIsAssignedToStringTypeThenValidationShouldPass() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new MultipleParametersMethodClass()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenAttributeIsAssignedToIntegerTypeThenValidationShouldFail() throws NoSuchMethodException {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new InvalidMethodDefinitions()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(ATTRIBUTE_TYPE_INVALID_ERROR, QUEUE_NAME));
  }

  private static class InvalidMethodDefinitions {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsAttribute("attribute") Integer value) {

    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", Integer.class);
    }
  }
}
