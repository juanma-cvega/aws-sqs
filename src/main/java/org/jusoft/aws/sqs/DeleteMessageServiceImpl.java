package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.annotations.DeletePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class DeleteMessageServiceImpl implements DeleteMessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMessageServiceImpl.class);

  private final AmazonSQS amazonSQS;

  public DeleteMessageServiceImpl(AmazonSQS amazonSQS) {
    this.amazonSQS = amazonSQS;
  }

  @Override
  public void deleteMessage(DeletePolicy deletePolicy,
                            ReceiveMessageResult receiveMessageResult,
                            String queueUrl,
                            Function<ReceiveMessageResult, Boolean> messageConsumer) {
    try {
      if (deletePolicy.equals(DeletePolicy.AFTER_READ)) {
        deleteMessages(receiveMessageResult, queueUrl);
        messageConsumer.apply(receiveMessageResult);
      } else {
        if (messageConsumer.apply(receiveMessageResult)) {
          deleteMessages(receiveMessageResult, queueUrl);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Unable to delete message", e);
    }
  }

  private void deleteMessages(ReceiveMessageResult receiveMessageResult, String queueUrl) {
    List<DeleteMessageBatchRequestEntry> deleteRequests = receiveMessageResult.getMessages().stream()
      .map(message -> new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()))
      .collect(toList());
    DeleteMessageBatchResult result = amazonSQS.deleteMessageBatch(new DeleteMessageBatchRequest().withQueueUrl(queueUrl).withEntries(deleteRequests));
    if (result.getFailed().size() > 0) {
      LOGGER.error("Error deleting messages from SQS: messages={}", result.getFailed());
    }
  }
}
