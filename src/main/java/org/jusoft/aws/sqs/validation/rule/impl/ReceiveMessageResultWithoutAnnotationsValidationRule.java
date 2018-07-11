package org.jusoft.aws.sqs.validation.rule.impl;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.stream.Stream;

/**
 * Validates that there are not any either {@link SqsBody} or {@link SqsAttribute} annotations when the parameter of
 * the consumer method is of type {@link com.amazonaws.services.sqs.model.ReceiveMessageRequest}
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ReceiveMessageResultWithoutAnnotationsValidationRule implements ValidationRule {

  static final String RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR =
    "A consumer expecting a ReceiveMessageResult object cannot use SQS annotation. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return ConsumerValidationResult.of(hasAnnotations(queueConsumer), queueConsumer);
  }

  private ErrorMessage hasAnnotations(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = ErrorMessage.noError();
    int parameterIndex = findReceiveMessageResultParameterIndexFrom(queueConsumer);
    if (parameterIndex != -1) {
      errorMessage.addMessage(ErrorMessage.of(() -> isSqsAnnotationPresentIn(queueConsumer, parameterIndex),
        RECEIVE_MESSAGE_RESULT_OBJECT_WITH_ANNOTATION_ERROR, queueConsumer.getAnnotation().value()));
    }
    return errorMessage;
  }

  private int findReceiveMessageResultParameterIndexFrom(QueueConsumer queueConsumer) {
    for (int parameterIndex = 0; parameterIndex < queueConsumer.getParametersTypes().size(); parameterIndex++) {
      if (queueConsumer.getParametersTypes().get(parameterIndex) == ReceiveMessageResult.class) {
        return parameterIndex;
      }
    }
    return -1;
  }

  private boolean isSqsAnnotationPresentIn(QueueConsumer queueConsumer, int parameterIndex) {
    return Stream.of(queueConsumer.getConsumerMethod().getParameterAnnotations()[parameterIndex])
      .noneMatch(annotation -> annotation.annotationType() == SqsAttribute.class
        || annotation.annotationType() == SqsBody.class);
  }
}
