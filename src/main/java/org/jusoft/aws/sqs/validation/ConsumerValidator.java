package org.jusoft.aws.sqs.validation;

import org.jusoft.aws.sqs.QueueConsumer;

public interface ConsumerValidator {

  void isValid(Iterable<QueueConsumer> consumers);

}
