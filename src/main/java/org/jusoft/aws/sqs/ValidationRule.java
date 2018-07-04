package org.jusoft.aws.sqs;

import org.jusoft.aws.sqs.validation.ConsumerValidationResult;

public interface ValidationRule {

  ConsumerValidationResult validate(Consumer consumer);

}
