package org.jusoft.aws.sqs.validation;

import org.jusoft.aws.sqs.Consumer;

public interface ConsumerValidator {

  void isValid(Iterable<Consumer> consumers);

}
