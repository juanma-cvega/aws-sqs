package org.jusoft.aws.sqs.provider;

import org.jusoft.aws.sqs.QueueConsumer;

/**
 * Provides the list of consumers to start in the application. How these consumers are created or passed down is up to
 * the implementation.
 */
public interface ConsumersInstanceProvider {

  /**
   * Returns the list of initialised consumers.
   */
  Iterable<QueueConsumer> getConsumers();
}
