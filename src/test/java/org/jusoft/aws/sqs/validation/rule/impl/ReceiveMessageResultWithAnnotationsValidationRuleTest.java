package org.jusoft.aws.sqs.validation.rule.impl;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.ReceiveMessageResultWithoutAnnotationsValidationRule.RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR;

public class ReceiveMessageResultWithAnnotationsValidationRuleTest extends AbstractValidationRuleTest {

  private final ReceiveMessageResultWithoutAnnotationsValidationRule rule = new ReceiveMessageResultWithoutAnnotationsValidationRule();

  @Test
  public void whenReceiveMessageResultDoesNotHaveAnnotationThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultParameterValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenReceiveMessageResultHasOtherAnnotationThenResultIsValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultAnyAnnotationValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenReceiveMessageResultHasBodyAnnotationThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultBodyParameterInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR, QUEUE_NAME));
  }

  @Test
  public void whenReceiveMessageResultHasAttributeAnnotationThenResultIsInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestReceiveMessageResultAttributeParameterInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR, QUEUE_NAME));
  }

  private static class TestReceiveMessageResultParameterValid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(ReceiveMessageResult result) {

    }
  }

  private static class TestReceiveMessageResultBodyParameterInvalid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody ReceiveMessageResult result) {

    }
  }

  private static class TestReceiveMessageResultAttributeParameterInvalid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsAttribute("attribute") ReceiveMessageResult result) {

    }
  }

  private static class TestReceiveMessageResultAnyAnnotationValid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@Deprecated ReceiveMessageResult result) {

    }
  }
}
