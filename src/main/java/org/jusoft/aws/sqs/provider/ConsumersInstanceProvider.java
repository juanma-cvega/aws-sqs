package org.jusoft.aws.sqs.provider;

import org.jusoft.aws.sqs.QueueConsumer;

public interface ConsumersInstanceProvider {

  Iterable<QueueConsumer> getConsumers();
}
