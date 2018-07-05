package org.jusoft.aws.sqs.validation.rule.impl;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.validation.rule.impl.ReceiveMessageResultOnlyParameterValidationRule.RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR;

public class ReceiveMessageResultOnlyParameterValidationRuleTest extends AbstractValidationRuleTest {

  private final ReceiveMessageResultOnlyParameterValidationRule rule = new ReceiveMessageResultOnlyParameterValidationRule();

  @Test
  public void whenReceiveMessageResultIsTheOnlyParameterThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultParameterValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenReceiveMessageResultIsNotTheOnlyParameterThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultParameterInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(RECEIVE_MESSAGE_RESULT_NOT_THE_ONLY_PARAMETER_ERROR, QUEUE_NAME));
  }

  private static class TestReceiveMessageResultParameterValid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(ReceiveMessageResult result) {

    }
  }

  private static class TestReceiveMessageResultParameterInvalid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(ReceiveMessageResult result, Object object) {

    }
  }
}
