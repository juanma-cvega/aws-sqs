package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.ReceiveMessageRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuePollService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueuePollService.class);

  private final ReceiveMessageRequestFactory receiveMessageRequestFactory;
  private final MessageConsumerService messageConsumerService;

  private boolean isDispatcherRunning;

  public QueuePollService(ReceiveMessageRequestFactory receiveMessageRequestFactory,
                          MessageConsumerService messageConsumerService) {
    this.receiveMessageRequestFactory = receiveMessageRequestFactory;
    this.messageConsumerService = messageConsumerService;
    isDispatcherRunning = true;
  }

  public void start(QueueConsumer queueConsumer) {
    String queueName = queueConsumer.getAnnotation().value();
    LOGGER.info("Starting queueConsumer: queue={}", queueName);

    ReceiveMessageRequest request = receiveMessageRequestFactory.createFrom(queueConsumer);
    while (isDispatcherRunning) {
      try {
        messageConsumerService.consumeAndDeleteMessages(queueConsumer, request);
      } catch (Exception e) {
        LOGGER.error("Error while consuming message(s): queueName={}", request.getQueueUrl(), e);
      }
    }
    LOGGER.info("Closing queueConsumer: queueName={}", queueName);
  }

  public void close() {
    isDispatcherRunning = false;
  }
}
