package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {

  ExecutorService createFor(Iterable<SqsConsumer> consumerProperties);
}
