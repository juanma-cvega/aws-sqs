package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveMessageRequestFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveMessageRequestFactory.class);

  private final AmazonSQS amazonSQS;

  ReceiveMessageRequestFactory(AmazonSQS amazonSQS) {
    this.amazonSQS = amazonSQS;
  }

  public ReceiveMessageRequest createFrom(QueueConsumer queueConsumer) {
    SqsConsumer annotation = queueConsumer.getAnnotation();
    return new ReceiveMessageRequest(findQueueUrlOrFailFrom(annotation.value()))
      .withMaxNumberOfMessages(annotation.maxMessagesPerPoll())
      .withWaitTimeSeconds(annotation.longPolling());
  }

  private String findQueueUrlOrFailFrom(String queueName) {
    String queueUrl = "";
    try {
      GetQueueUrlResult foundQueue = amazonSQS.getQueueUrl(queueName);
      queueUrl = foundQueue.getQueueUrl();
    } catch (QueueDoesNotExistException e) {
      LOGGER.error("Unable to get queue url: queueName={}", queueName, e);
      System.exit(-1);
    }
    return queueUrl;
  }
}
