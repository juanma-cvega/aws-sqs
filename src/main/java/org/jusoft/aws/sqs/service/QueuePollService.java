package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.jusoft.aws.sqs.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link ReceiveMessageRequest} using the {@link ReceiveMessageRequestFactory} and starts a loop where the
 * {@link MessageConsumerService} is called with the {@link ReceiveMessageRequest} created. The loop stops once the
 * {@link #stop()} method is invoked.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class QueuePollService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueuePollService.class);

  private final ReceiveMessageRequestFactory receiveMessageRequestFactory;
  private final MessageConsumerService messageConsumerService;

  /**
   * Controls the loop that consumes messages from AWS SQS.
   */
  private boolean isConsumerActive;

  public QueuePollService(ReceiveMessageRequestFactory receiveMessageRequestFactory,
                          MessageConsumerService messageConsumerService) {
    this.receiveMessageRequestFactory = receiveMessageRequestFactory;
    this.messageConsumerService = messageConsumerService;
  }

  /**
   * Calls the {@link ReceiveMessageRequestFactory} with the {@link QueueConsumer} passed as parameter to create a
   * {@link ReceiveMessageRequest}. It is then used to invoke the {@link MessageConsumerService} inside a loop. The
   * method ends once the loop is disabled calling the {@link #stop()} method.
   *
   * @param queueConsumer
   */
  public void start(QueueConsumer queueConsumer) {
    String queueName = queueConsumer.getAnnotation().value();
    LOGGER.info("Starting queueConsumer: queue={}", queueName);

    isConsumerActive = true;
    ReceiveMessageRequest request = receiveMessageRequestFactory.createFrom(queueConsumer);
    while (isConsumerActive) {
      try {
        messageConsumerService.consumeAndDeleteMessages(queueConsumer, request);
      } catch (Exception e) {
        LOGGER.error("Error while consuming message(s): queueName={}", request.getQueueUrl(), e);
      }
    }
    LOGGER.info("Closing queueConsumer: queueName={}", queueName);
  }

  /**
   * Disables the consumer by changing the flag controlling the consumer loop.
   */
  public void stop() {
    isConsumerActive = false;
  }
}
