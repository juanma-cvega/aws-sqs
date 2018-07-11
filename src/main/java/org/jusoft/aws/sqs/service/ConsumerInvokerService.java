package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.mapper.ConsumerParametersMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Invokes the consumer using the {@link ReceiveMessageResult} to create its parameters.
 * The {@link QueueConsumer} contains both the instance of the consumer and the method to invoke after consuming a
 * message from the AWS SQS queue. The parameters are created by the {@link ConsumerParametersMapper} that uses
 * reflection to read the parameters type from the method and then deserialises the SQS message body and attributes to
 * match the extracted types.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ConsumerInvokerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInvokerService.class);

  private final ConsumerParametersMapper consumerParametersMapper;

  public ConsumerInvokerService(ConsumerParametersMapper consumerParametersMapper) {
    this.consumerParametersMapper = consumerParametersMapper;
  }

  /**
   * Invokes instance consumer method contained in {@link QueueConsumer}. Uses the {@link ReceiveMessageResult} to
   * create the method parameters.
   *
   * @param queueConsumer containing the instance consumer method and the instance to invoke it from.
   * @param result        AWS SQS message.
   */
  public void invoke(QueueConsumer queueConsumer, ReceiveMessageResult result) {
    try {
      Object[] consumerParameters = consumerParametersMapper.createFrom(queueConsumer.getConsumerMethod(), result);
      queueConsumer.getConsumerMethod().invoke(queueConsumer.getConsumerInstance(), consumerParameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("Error invoking method", e);
      throw new IllegalArgumentException(e);
    }
  }
}
