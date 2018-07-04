package org.jusoft.aws.sqs.executor;

import org.jusoft.aws.sqs.Consumer;

import java.util.concurrent.ExecutorService;

public interface SqsExecutorFactory {

  ExecutorService createFor(Iterable<Consumer> consumerProperties);
}
