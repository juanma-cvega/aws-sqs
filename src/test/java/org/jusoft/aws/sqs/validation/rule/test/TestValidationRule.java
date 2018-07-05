package org.jusoft.aws.sqs.validation.rule.test;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.rule.ConsumerValidationResult;
import org.jusoft.aws.sqs.validation.rule.ValidationRule;

/**
 * Used for testing ClassLoaderRulesProvider
 */
public class TestValidationRule implements ValidationRule {
  @Override
  public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
    return null;
  }
}
