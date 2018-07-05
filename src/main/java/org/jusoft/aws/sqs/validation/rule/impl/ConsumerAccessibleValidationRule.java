package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.lang.reflect.Modifier;

public class ConsumerAccessibleValidationRule implements ValidationRule {

  static final String CONSUMER_METHOD_NOT_ACCESSIBLE =
    "The consumer method is not accessible. The method should be public. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return ConsumerValidationResult.of(ErrorMessage.of(() -> Modifier.isPublic(queueConsumer.getConsumerMethod().getModifiers()),
      CONSUMER_METHOD_NOT_ACCESSIBLE, queueConsumer.getAnnotation().value()), queueConsumer);
  }
}
