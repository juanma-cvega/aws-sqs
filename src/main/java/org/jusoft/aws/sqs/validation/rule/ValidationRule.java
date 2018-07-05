package org.jusoft.aws.sqs.validation.rule;

import org.jusoft.aws.sqs.QueueConsumer;

public interface ValidationRule {

  ConsumerValidationResult validate(QueueConsumer queueConsumer);

}
