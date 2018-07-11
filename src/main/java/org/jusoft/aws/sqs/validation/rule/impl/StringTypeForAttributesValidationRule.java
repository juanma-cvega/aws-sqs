package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ErrorMessage;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Validates parameters annotated with @{@link SqsAttribute} are of type {@link String}
 *
 * @author Juan Manuel Carnicero Vega
 */
public class StringTypeForAttributesValidationRule implements ValidationRule {

  static final String ATTRIBUTE_TYPE_INVALID_ERROR =
    "All parameters in a consumer defined to be mapped to message attributes must be of type String. Queue=%s";

  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    ErrorMessage errorMessage = ErrorMessage.of(areValidAttributeDefinitions(queueConsumer),
      ATTRIBUTE_TYPE_INVALID_ERROR, queueConsumer.getAnnotation().value());
    return ConsumerValidationResult.of(errorMessage, queueConsumer);
  }

  private Supplier<Boolean> areValidAttributeDefinitions(QueueConsumer queueConsumer) {
    return () -> Stream.of(queueConsumer.getConsumerMethod().getParameters())
      .filter(parameter -> parameter.getAnnotation(SqsAttribute.class) != null)
      .allMatch(parameter -> parameter.getType() == String.class);
  }
}
