package org.jusoft.aws.sqs.validation.rules;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.ErrorMessage;

import java.lang.reflect.Modifier;

public class ConsumerAccessibleValidationRule implements ValidationRule {

  static final String CONSUMER_METHOD_NOT_ACCESSIBLE =
    "The consumer method is not accessible. The method should be public. Queue=%s";

  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return ConsumerValidationResult.of(ErrorMessage.of(() -> Modifier.isPublic(consumer.getConsumerMethod().getModifiers()),
      CONSUMER_METHOD_NOT_ACCESSIBLE, consumer.getAnnotation().value()), consumer);
  }
}
