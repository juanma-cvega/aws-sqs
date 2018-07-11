package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.jusoft.aws.sqs.validation.rule.ErrorMessage.noError;

/**
 * Validates that:
 * <li>
 * <ul>Each consumer method argument has either a @{@link SqsBody} or a {@link SqsAttribute} only once.</ul>
 * <ul>The {@link SqsBody} annotation is used only once</ul>
 * <ul>The {@link SqsAttribute} is not used when there is only one parameter in the consumer method</ul>
 * </li>
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ParametersAnnotationsValidationRule implements ValidationRule {

  static final String PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR = "A consumer argument must have a unique SQS annotation. Queue=%s";
  static final String MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR = "SqsBody annotation must appear once in the definition of a consumer. Queue=%s";
  static final String SINGLE_PARAMETER_NOT_BODY_ERROR = "SqsBody annotation must be the only SQS annotation when there is a single parameter. Queue=%s";

  private static final int ALLOWED_ANNOTATIONS_PER_PARAMETER = 1;

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = noError();
    if (queueConsumer.getParametersTypes().size() == 1) {
      errorMessage.addMessage(validateOnlyParameterIsBody(queueConsumer));
    } else if (isParametersSizeValid(queueConsumer.getParametersTypes())) {
      errorMessage.addMessage(validateAllConsumerParametersHaveValidAnnotations(queueConsumer));
    }
    return ConsumerValidationResult.of(errorMessage, queueConsumer);
  }

  private ErrorMessage validateOnlyParameterIsBody(QueueConsumer queueConsumer) {
    return ErrorMessage.of(() -> isSqsAttributeNotPresentForAnyParameter(queueConsumer), SINGLE_PARAMETER_NOT_BODY_ERROR, queueConsumer.getAnnotation().value());
  }

  private boolean isSqsAttributeNotPresentForAnyParameter(QueueConsumer queueConsumer) {
    return Stream.of(queueConsumer.getConsumerMethod().getParameterAnnotations())
      .allMatch(this::isSqsAttributeNotPresent);
  }

  private boolean isSqsAttributeNotPresent(Annotation[] annotations) {
    return Stream.of(annotations)
      .noneMatch(annotation -> annotation.annotationType() == SqsAttribute.class);
  }

  private boolean isParametersSizeValid(List<Class<?>> parametersType) {
    return !parametersType.isEmpty();
  }

  private ErrorMessage validateAllConsumerParametersHaveValidAnnotations(QueueConsumer queueConsumer) {
    return ErrorMessage.of(isSqsAnnotationNotRepeatedFor(queueConsumer), PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR, queueConsumer.getAnnotation().value())
      .addMessage(ErrorMessage.of(isSqsBodyAnnotationOnlyOnceFor(queueConsumer), MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR, queueConsumer.getAnnotation().value()));
  }

  private Supplier<Boolean> isSqsAnnotationNotRepeatedFor(QueueConsumer queueConsumer) {
    return () -> Stream.of(queueConsumer.getConsumerMethod().getParameterAnnotations())
      .map(this::countSqsAnnotationOn)
      .noneMatch(sqsAnnotationsPerParameter -> sqsAnnotationsPerParameter != ALLOWED_ANNOTATIONS_PER_PARAMETER);
  }

  private Supplier<Boolean> isSqsBodyAnnotationOnlyOnceFor(QueueConsumer queueConsumer) {
    return () -> Stream.of(queueConsumer.getConsumerMethod().getParameterAnnotations())
      .filter(this::containsSqsBodyAnnotation)
      .count() == 1;
  }

  private boolean containsSqsBodyAnnotation(Annotation[] annotations) {
    return Stream.of(annotations).anyMatch(this::isSqsBodyAnnotation);
  }

  private int countSqsAnnotationOn(Annotation[] parameterAnnotations) {
    return (int) Stream.of(parameterAnnotations).filter(this::isAnySqsAnnotation).count();
  }

  private boolean isAnySqsAnnotation(Annotation parameterAnnotation) {
    return parameterAnnotation.annotationType() == SqsAttribute.class || parameterAnnotation.annotationType() == SqsBody.class;
  }

  private boolean isSqsBodyAnnotation(Annotation parameterAnnotation) {
    return parameterAnnotation.annotationType() == SqsBody.class;
  }
}
