package org.jusoft.aws.sqs.validation.rule;

import org.jusoft.aws.sqs.Consumer;

public interface ValidationRule {

  ConsumerValidationResult validate(Consumer consumer);

}
