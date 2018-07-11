package org.jusoft.aws.sqs.validation;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

/**
 * Classes implementing this interface <b>must</b> iterate over the list of passed consumers and validate that they
 * comply with the following requirements:
 * <li>
 * <ul>Respect the minimum number of concurrent consumers per consumer (1) set using
 * {@link SqsConsumer#concurrentConsumers()}</ul>
 * <ul>Consumer method must be accessible (public)</ul>
 * <ul>Every consumer method must have at least one parameter, in which case it has to be either the body of the message
 * or of type {@link com.amazonaws.services.sqs.model.ReceiveMessageRequest}</ul>
 * <ul>Respect the minimum (0, disabled) and maximum (20) long polling valid values in seconds according to the AWS SQS
 * set using {@link SqsConsumer#longPolling()}
 * documentation</ul>
 * <ul>Every consumer method parameter must have either zero or one annotations of type
 * {@link org.jusoft.aws.sqs.annotation.SqsBody} or {@link org.jusoft.aws.sqs.annotation.SqsAttribute}</ul>
 * <ul>{@link org.jusoft.aws.sqs.annotation.SqsBody} must appear once when there are more than one parameters</ul>
 * <ul>{@link org.jusoft.aws.sqs.annotation.SqsBody} is the only valid annotation to choose from the ones provided
 * by the library when there is only one parameter in the consumer method but it is not mandatory to add it</ul>
 * <ul>When {@link SqsConsumer#maxMessagesPerPoll()} is greater than 1, the parameter to hold the body of messages
 * in a consumer method must be of type {@link java.util.List}</ul>
 * <ul>When {@link SqsConsumer#maxMessagesPerPoll()} is greater than 1,
 * {@link org.jusoft.aws.sqs.annotation.SqsAttribute} is not allowed</ul>
 * <ul>The maximum number of messages to poll using {@link SqsConsumer#maxMessagesPerPoll()} is 10 according to the AWS
 * documentation</ul>
 * <ul>The minimum number of messages to poll using {@link SqsConsumer#maxMessagesPerPoll()} is 1</ul>
 * <ul>When there is a parameter of type {@link com.amazonaws.services.sqs.model.ReceiveMessageRequest} in the consumer
 * method, it must be the only parameter and it cannot be annotated with any of the annotations
 * {@link org.jusoft.aws.sqs.annotation.SqsBody} or {@link org.jusoft.aws.sqs.annotation.SqsAttribute}</ul>
 * <ul>Any parameter annotated with the {@link org.jusoft.aws.sqs.annotation.SqsAttribute} annotation must be of type
 * {@link String}</ul>
 * </li>
 *
 * @author Juan Manuel Carnicero Vega
 */
public interface ConsumerValidator {

  /**
   * Validates the consumers passed in the parameter are valid according to the library rules
   */
  void isValid(Iterable<QueueConsumer> consumers);

}
