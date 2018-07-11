package org.jusoft.aws.sqs.validation.rule;

import java.util.Set;

/**
 * Classes implementing this interface must return a {@link Set} of valid {@link ValidationRule}s used to validate
 * that consumers comply with the requirements specified in the {@link org.jusoft.aws.sqs.validation.ConsumerValidator}
 * documentation.
 *
 * @author Juan Manuel Carnicero Vega
 */
public interface RulesProvider {

  /**
   * @return java.util.Set of {@link ValidationRule} found or configured.
   */
  Set<ValidationRule> find();
}
