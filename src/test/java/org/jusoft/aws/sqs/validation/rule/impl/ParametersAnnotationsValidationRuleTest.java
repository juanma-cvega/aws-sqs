package org.jusoft.aws.sqs.validation.rule.impl;

import org.junit.Test;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.validation.rule.impl.ParametersAnnotationsValidationRule.MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR;
import static org.jusoft.aws.sqs.validation.rule.impl.ParametersAnnotationsValidationRule.PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR;
import static org.jusoft.aws.sqs.validation.rule.impl.ParametersAnnotationsValidationRule.SINGLE_PARAMETER_NOT_BODY_ERROR;

public class ParametersAnnotationsValidationRuleTest extends AbstractValidationRuleTest {

  private final ParametersAnnotationsValidationRule rule = new ParametersAnnotationsValidationRule();

  @Test
  public void whenAllArgumentsHaveValidAnnotationsThenResultShouldBeValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestValidAnnotations()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenThereIsOneArgumentWithoutAnnotationThenResultShouldBeValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestNoAnnotationsValid()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenThereAreTwoSqsBodyAnnotationsThenResultShouldBeInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestInvalidTwoSqsBodyAnnotations()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR, QUEUE_NAME));
  }

  @Test
  public void whenThereAreTwoSqsAnnotationsInTheSameParameterThenResultShouldBeInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestInvalidTwoSqsAnnotationsInSameParameter()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR, QUEUE_NAME));
  }

  @Test
  public void whenThereAreNoParametersThenResultShouldBeValid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestZeroArguments()));

    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrorMessage()).isEqualTo(EMPTY);
  }

  @Test
  public void whenThereIsSingleParameterWithSqsAttributeThenResultShouldBeInvalid() {
    ConsumerValidationResult result = rule.validate(getConsumerFrom(new TestSingleParameterInvalid()));

    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo(String.format(SINGLE_PARAMETER_NOT_BODY_ERROR, QUEUE_NAME));
  }

  private static class TestValidAnnotations {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody Object body, @SqsAttribute("attribute") Object attribute, @SqsAttribute("attribute") Object attribute2) {

    }
  }

  private static class TestNoAnnotationsValid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(Object body) {

    }
  }

  private static class TestSingleParameterInvalid {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsAttribute("attribute") Object body) {

    }
  }

  private static class TestInvalidTwoSqsBodyAnnotations {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody Object body, @SqsBody Object body2) {

    }
  }

  private static class TestInvalidTwoSqsAnnotationsInSameParameter {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsAttribute("attribute") Object attribute, @SqsBody @SqsAttribute("attribute") Object body) {

    }
  }
}
