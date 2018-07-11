package org.jusoft.aws.sqs.validation;

/**
 * Creates a ConsumerValidator
 *
 * @author Juan Manuel Carnicero Vega
 */
public interface ConsumerValidatorFactory {

  /**
   * Creates a {@link ConsumerValidator}
   */
  ConsumerValidator create();
}
