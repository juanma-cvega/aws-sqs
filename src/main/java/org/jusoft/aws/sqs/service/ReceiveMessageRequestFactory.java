package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link ReceiveMessageRequest} based on the information contained in the consumer method {@link SqsConsumer}
 * annotation. The queue URL is fetched from the AWS account using the {@link AmazonSQS} client and the name of the
 * queue passed in the {@link SqsConsumer} annotation. <b>In case the URL cannot be fetched, a {@link System#exit(int)}
 * is invoked.</b>
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ReceiveMessageRequestFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveMessageRequestFactory.class);

  private final AmazonSQS amazonSQS;

  public ReceiveMessageRequestFactory(AmazonSQS amazonSQS) {
    this.amazonSQS = amazonSQS;
  }

  /**
   * Creates the {@link ReceiveMessageRequest} using the {@link SqsConsumer} annotation from the {@link QueueConsumer}.
   * The queue URL is fetched from the AWS account using the {@link AmazonSQS} client and the name of the queue contained
   * in the {@link SqsConsumer}. <b>In case the URL cannot be fetched, a {@link System#exit(int)} is invoked.</b>
   *
   * @param queueConsumer consumer instance and method annotated with {@link SqsConsumer}.
   */
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
