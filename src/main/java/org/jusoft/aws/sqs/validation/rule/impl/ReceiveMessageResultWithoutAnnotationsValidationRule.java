package org.jusoft.aws.sqs.validation.rule.impl;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.stream.Stream;

public class ReceiveMessageResultWithoutAnnotationsValidationRule implements ValidationRule {

  static final String RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR =
    "A consumer expecting a ReceiveMessageResult object cannot use SQS annotation. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return ConsumerValidationResult.of(hasAnnotations(consumer), consumer);
  }

  private ErrorMessage hasAnnotations(Consumer consumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    int parameterIndex = findReceiveMessageResultParameterIndexFrom(consumer);
    if (parameterIndex != -1) {
      errorMessage.addMessage(ErrorMessage.of(() -> isSqsAnnotationPresentIn(consumer, parameterIndex),
        RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR, consumer.getAnnotation().value()));
    }
    return errorMessage;
  }

  private int findReceiveMessageResultParameterIndexFrom(Consumer consumer) {
    for (int parameterIndex = 0; parameterIndex < consumer.getParametersTypes().size(); parameterIndex++) {
      if (consumer.getParametersTypes().get(parameterIndex) == ReceiveMessageResult.class) {
        return parameterIndex;
      }
    }
    return -1;
  }

  private boolean isSqsAnnotationPresentIn(Consumer consumer, int parameterIndex) {
    return Stream.of(consumer.getConsumerMethod().getParameterAnnotations()[parameterIndex])
      .noneMatch(annotation -> annotation.annotationType() == SqsAttribute.class
        || annotation.annotationType() == SqsBody.class);
  }
}
