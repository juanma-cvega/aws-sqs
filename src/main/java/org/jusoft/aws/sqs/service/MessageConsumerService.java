package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jusoft.aws.sqs.annotation.DeletePolicy.AFTER_READ;

/**
 * Polls messages from AWS SQS using a {@link ReceiveMessageRequest}. The messages are used to invoke the consumer
 * method contained in a {@link QueueConsumer} using the {@link ConsumerInvokerService}. Depending on the
 * {@link org.jusoft.aws.sqs.annotation.DeletePolicy} found in the consumer method
 * {@link org.jusoft.aws.sqs.annotation.SqsConsumer} annotation, messages are deleted from the SQS queue either after
 * being read from the queue or after the consumer has successfully processed the message.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class MessageConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerService.class);

  /**
   * AWS SQS client used to consume and delete messages from the queue
   */
  private final AmazonSQS amazonSQS;
  private final ConsumerInvokerService consumerInvokerService;

  public MessageConsumerService(AmazonSQS amazonSQS,
                                ConsumerInvokerService consumerInvokerService) {
    this.amazonSQS = amazonSQS;
    this.consumerInvokerService = consumerInvokerService;
  }

  /**
   * Uses the AWS SQS client to consume messages using the {@link ReceiveMessageRequest} passed as a parameter. The
   * message(s) is then processed by invoking {@link ConsumerInvokerService} with the {@link QueueConsumer} received as
   * parameter. Based on the {@link org.jusoft.aws.sqs.annotation.DeletePolicy} contained in the
   * {@link org.jusoft.aws.sqs.annotation.SqsConsumer} of the consumer, the message(s) is deleted from the queue either
   * after being read or after being successfully processed.
   *
   * @param queueConsumer consumer instance and method to invoke that contains the
   *                      {@link org.jusoft.aws.sqs.annotation.SqsConsumer} annotation.
   * @param request       AWS {@link ReceiveMessageRequest}
   */
  public void consumeAndDeleteMessages(QueueConsumer queueConsumer,
                                       ReceiveMessageRequest request) {
    ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(request);
    LOGGER.trace("Message(s) received from queue: size={}", receiveMessageResult.getMessages().size());
    if (!receiveMessageResult.getMessages().isEmpty()) {
      if (isMessagesToBeDeletedBeforeProcessingFor(queueConsumer)) {
        deleteMessagesBeforeProcessing(receiveMessageResult, request.getQueueUrl(), queueConsumer);
      } else {
        deleteMessagesAfterProcessing(receiveMessageResult, request.getQueueUrl(), queueConsumer);
      }
    }
  }

  private boolean isMessagesToBeDeletedBeforeProcessingFor(QueueConsumer queueConsumer) {
    return queueConsumer.getAnnotation().deletePolicy().equals(AFTER_READ);
  }

  private void deleteMessagesBeforeProcessing(ReceiveMessageResult receiveMessageResult, String queueUrl, QueueConsumer consumer) {
    LOGGER.debug("Deleting messages before processing them: queueUrl={}", queueUrl);
    deleteMessages(receiveMessageResult, queueUrl);
    consumerInvokerService.invoke(consumer, receiveMessageResult);
  }

  private void deleteMessagesAfterProcessing(ReceiveMessageResult receiveMessageResult, String queueUrl, QueueConsumer consumer) {
    LOGGER.debug("Deleting messages after processing them: queueUrl={}", queueUrl);
    try {
      consumerInvokerService.invoke(consumer, receiveMessageResult);
    } catch (Exception e) {
      LOGGER.warn("Failed to consume message(s). Message(s) will not be deleted: queueUrl={}", queueUrl);
      throw e;
    }
    deleteMessages(receiveMessageResult, queueUrl);
  }

  private void deleteMessages(ReceiveMessageResult receiveMessageResult, String queueUrl) {
    DeleteMessageBatchResult result = amazonSQS.deleteMessageBatch(createDeleteMessageRequestFrom(receiveMessageResult, queueUrl));
    if (!result.getFailed().isEmpty()) {
      LOGGER.error("Error deleting messages from SQS: queueUrl={}, messages={}", receiveMessageResult, result.getFailed());
    }
    LOGGER.debug("Messages deleted from SQS: queueUrl={}, messages={}", queueUrl, result.getSuccessful());
  }

  private DeleteMessageBatchRequest createDeleteMessageRequestFrom(ReceiveMessageResult receiveMessageResult, String queueUrl) {
    List<DeleteMessageBatchRequestEntry> batchEntries = receiveMessageResult.getMessages().stream()
      .map(message -> new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()))
      .collect(toList());
    return new DeleteMessageBatchRequest().withQueueUrl(queueUrl).withEntries(batchEntries);
  }
}
