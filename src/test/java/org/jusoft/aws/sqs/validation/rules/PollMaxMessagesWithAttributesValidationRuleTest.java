package org.jusoft.aws.sqs.validation.rules;

import org.junit.Test;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.validation.rules.PollMaxMessagesWithAttributesValidationRule.MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR;

public class PollMaxMessagesWithAttributesValidationRuleTest extends AbstractValidationRuleTest {

  private final PollMaxMessagesWithAttributesValidationRule rule = new PollMaxMessagesWithAttributesValidationRule();

  @Test
  public void whenThereAreSqsAttributeAnnotationsOnParametersAndDefaultMaxPollingMessagesThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerWithAttributesValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenThereAreSqsAttributeAnnotationsOnParametersAndMaxPollingIsNotDefaultThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestConsumerWithAttributesInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MESSAGES_WITH_MULTIPLE_PARAMETERS_ERROR, QUEUE_NAME));
  }

  private static class TestConsumerWithAttributesValid {

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(@SqsAttribute("attribute") Object attribute) {

    }
  }

  private static class TestConsumerWithAttributesInvalid {

    @SqsConsumer(value = QUEUE_NAME, maxMessagesPerPoll = 2)
    public void testConsumer(@SqsAttribute("attribute") Object attribute) {

    }
  }
}
