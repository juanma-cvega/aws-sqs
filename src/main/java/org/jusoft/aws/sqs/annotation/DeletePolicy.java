package org.jusoft.aws.sqs.annotation;

/**
 * Specifies the type of policy to use when deleting messages from the queue.
 *
 * @author Juan Manuel Carnicero Vega
 */
public enum DeletePolicy {

  /**
   * Messages are deleted right after being fetched from the queue
   */
  AFTER_READ,

  /**
   * Messages are deleted after the consumer has finished processing them
   */
  AFTER_PROCESS
}
