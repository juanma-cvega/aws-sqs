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

public class MessageConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerService.class);

  private final AmazonSQS amazonSQS;
  private final ConsumerInvokerService consumerInvokerService;

  public MessageConsumerService(AmazonSQS amazonSQS,
                                ConsumerInvokerService consumerInvokerService) {
    this.amazonSQS = amazonSQS;
    this.consumerInvokerService = consumerInvokerService;
  }

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
