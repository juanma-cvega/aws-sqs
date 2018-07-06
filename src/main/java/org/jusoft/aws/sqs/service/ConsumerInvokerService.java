package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.mapper.ConsumerParametersMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class ConsumerInvokerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInvokerService.class);

  private final ConsumerParametersMapper consumerParametersMapper;

  public ConsumerInvokerService(ConsumerParametersMapper consumerParametersMapper) {
    this.consumerParametersMapper = consumerParametersMapper;
  }

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
