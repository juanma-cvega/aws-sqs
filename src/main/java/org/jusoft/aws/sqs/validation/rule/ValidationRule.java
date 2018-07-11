package org.jusoft.aws.sqs.validation.rule;

import org.jusoft.aws.sqs.QueueConsumer;

/**
 * Validates one or several of the requirements specified in the {@link org.jusoft.aws.sqs.validation.ConsumerValidator}
 * documentation for the given {@link QueueConsumer}
 *
 * @author Juan Manuel Carnicero Vega
 */
public interface ValidationRule {


  /**
   * Validates the {@link QueueConsumer} passed as a parameter.
   *
   * @param queueConsumer
   * @return ConsumerValidationResult representing the result of the validation and containing error messages describing
   * any problem found during validation.
   */
  ConsumerValidationResult validate(QueueConsumer queueConsumer);

}
