package org.jusoft.aws.sqs.validation.rules;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.annotations.SqsBody;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.jusoft.aws.sqs.validation.ErrorMessage.noError;

public class ParametersAnnotationsValidationRule implements ValidationRule {

  static final String PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR = "A consumer argument must have a unique SQS annotation. Queue=%s";
  static final String MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR = "SqsBody annotation must appear once in the definition of a consumer. Queue=%s";
  static final String SINGLE_PARAMETER_NOT_BODY_ERROR = "SqsBody annotation must the only SQS annotation when there is a single parameter. Queue=%s";

  private static final int ALLOWED_ANNOTATIONS_PER_PARAMETER = 1;

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    ErrorMessage errorMessage = noError();
    if (consumer.getParametersTypes().size() == 1) {
      errorMessage.addMessage(validateOnlyParameterIsBody(consumer));
    } else if (isParametersSizeValid(consumer.getParametersTypes())) {
      errorMessage.addMessage(validateAllConsumerParametersHaveValidAnnotations(consumer));
    }
    return ConsumerValidationResult.of(errorMessage, consumer);
  }

  private ErrorMessage validateOnlyParameterIsBody(Consumer consumer) {
    return ErrorMessage.of(() -> isSqsAttributeNotPresentForAnyParameter(consumer), SINGLE_PARAMETER_NOT_BODY_ERROR, consumer.getAnnotation().value());
  }

  private boolean isSqsAttributeNotPresentForAnyParameter(Consumer consumer) {
    return Stream.of(consumer.getConsumerMethod().getParameterAnnotations())
      .allMatch(this::isSqsAttributeNotPresent);
  }

  private boolean isSqsAttributeNotPresent(Annotation[] annotations) {
    return Stream.of(annotations)
      .noneMatch(annotation -> annotation.annotationType() == SqsAttribute.class);
  }

  private boolean isParametersSizeValid(List<Class<?>> parametersType) {
    return parametersType.size() > 0;
  }

  private ErrorMessage validateAllConsumerParametersHaveValidAnnotations(Consumer consumer) {
    ErrorMessage errorMessage = noError();
    errorMessage.addMessage(ErrorMessage.of(isSqsAnnotationNotRepeatedFor(consumer), PARAMETER_ANNOTATION_NUMBER_RESTRICTION_ERROR, consumer.getAnnotation().value()));
    errorMessage.addMessage(ErrorMessage.of(isSqsBodyAnnotationOnlyOnceFor(consumer), MULTIPLE_SQS_BODY_ANNOTATIONS_ERROR, consumer.getAnnotation().value()));
    return errorMessage;
  }

  private Supplier<Boolean> isSqsAnnotationNotRepeatedFor(Consumer consumer) {
    return () -> Stream.of(consumer.getConsumerMethod().getParameterAnnotations())
      .map(this::countSqsAnnotationOn)
      .noneMatch(sqsAnnotationsPerParameter -> sqsAnnotationsPerParameter != ALLOWED_ANNOTATIONS_PER_PARAMETER);
  }

  private Supplier<Boolean> isSqsBodyAnnotationOnlyOnceFor(Consumer consumer) {
    return () -> Stream.of(consumer.getConsumerMethod().getParameterAnnotations())
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
