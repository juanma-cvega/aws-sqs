package org.jusoft.aws.sqs.validation.test;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.ValidationRule;
import org.jusoft.aws.sqs.validation.ConsumerValidationResult;

/**
 * Used for testing ClassLoaderRulesProvider
 */
public class TestValidationRule implements ValidationRule {
  @Override
  public ConsumerValidationResult validate(Consumer consumer) {
    return null;
  }
}
