package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.QueueConsumer;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {

  ExecutorService createFor(Iterable<QueueConsumer> consumerProperties);
}
