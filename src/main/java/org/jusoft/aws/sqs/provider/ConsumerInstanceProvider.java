package org.jusoft.aws.sqs.provider;

import org.jusoft.aws.sqs.Consumer;

public interface ConsumerInstanceProvider {

  Iterable<Consumer> getConsumers();
}
