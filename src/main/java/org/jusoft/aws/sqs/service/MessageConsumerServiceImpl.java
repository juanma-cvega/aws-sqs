package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.annotation.DeletePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.jusoft.aws.sqs.annotation.DeletePolicy.AFTER_READ;

public class MessageConsumerServiceImpl implements MessageConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerServiceImpl.class);

  private final AmazonSQS amazonSQS;

  public MessageConsumerServiceImpl(AmazonSQS amazonSQS) {
    this.amazonSQS = amazonSQS;
  }

  @Override
  public void consumeAndDeleteMessage(DeletePolicy deletePolicy,
                                      ReceiveMessageResult receiveMessageResult,
                                      String queueUrl,
                                      Consumer<ReceiveMessageResult> messageConsumer) {
    if (deletePolicy.equals(AFTER_READ)) {
      deleteMessagesBeforeProcessing(receiveMessageResult, queueUrl, messageConsumer);
    } else {
      deleteMessagesAfterProcessing(receiveMessageResult, queueUrl, messageConsumer);
    }
  }

  private void deleteMessagesBeforeProcessing(ReceiveMessageResult receiveMessageResult, String queueUrl, Consumer<ReceiveMessageResult> messageConsumer) {
    deleteMessages(receiveMessageResult, queueUrl);
    messageConsumer.accept(receiveMessageResult);
  }

  private void deleteMessagesAfterProcessing(ReceiveMessageResult receiveMessageResult, String queueUrl, Consumer<ReceiveMessageResult> messageConsumer) {
    try {
      messageConsumer.accept(receiveMessageResult);
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
    LOGGER.trace("Messages deleted from SQS: queueUrl={}, messages={}", queueUrl, result.getSuccessful());
  }

  private DeleteMessageBatchRequest createDeleteMessageRequestFrom(ReceiveMessageResult receiveMessageResult, String queueUrl) {
    List<DeleteMessageBatchRequestEntry> batchEntries = receiveMessageResult.getMessages().stream()
      .map(message -> new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()))
      .collect(toList());
    return new DeleteMessageBatchRequest().withQueueUrl(queueUrl).withEntries(batchEntries);
  }
}
