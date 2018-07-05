package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.annotation.DeletePolicy;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageConsumerServiceImplTest {

  private static final String QUEUE_URL = "queueUrl";
  private static final String RECEIPT_HANDLE_1 = "receiptHandle";
  private static final String BODY_VALUE = "anyBody";
  private static final String MESSAGE_BODY_1 = "{\"value\":\"" + BODY_VALUE + "\"}";
  private static final String MESSAGE_ID_1 = "messageId1";
  private static final Message MESSAGE_1 = new Message()
    .withReceiptHandle(RECEIPT_HANDLE_1)
    .withMessageId(MESSAGE_ID_1)
    .withBody(MESSAGE_BODY_1);
  private static final ReceiveMessageResult RECEIVE_MESSAGE_RESULT = new ReceiveMessageResult()
    .withMessages(MESSAGE_1);
  private static final DeleteMessageBatchRequestEntry DELETE_MESSAGE_BATCH_REQUEST_ENTRY = new DeleteMessageBatchRequestEntry(MESSAGE_ID_1, RECEIPT_HANDLE_1);
  private static final List<DeleteMessageBatchRequestEntry> DELETE_MESSAGE_BATCH_REQUEST_ENTRIES = singletonList(DELETE_MESSAGE_BATCH_REQUEST_ENTRY);
  private static final DeleteMessageBatchRequest DELETE_MESSAGE_BATCH_REQUEST = new DeleteMessageBatchRequest(QUEUE_URL, DELETE_MESSAGE_BATCH_REQUEST_ENTRIES);
  private static final String ERROR_CODE = "errorCode";
  private static final String ERROR_MESSAGE = "errorMessage";
  private static final DeleteMessageBatchResult DELETE_MESSAGE_BATCH_RESULT = new DeleteMessageBatchResult()
    .withFailed(singletonList(new BatchResultErrorEntry()
      .withCode(ERROR_CODE)
      .withId(MESSAGE_ID_1)
      .withMessage(ERROR_MESSAGE)));

  @Mock
  private AmazonSQS amazonSQS;
  @Mock
  private Consumer<ReceiveMessageResult> mockConsumer;

  @InjectMocks
  private MessageConsumerServiceImpl deleteMessageService;

  @Test
  public void whenInvokedWithPolicyAfterReadThenConsumerShouldBeCalledAfterDeletingMessages() {
    when(amazonSQS.deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST)).thenReturn(DELETE_MESSAGE_BATCH_RESULT);

    deleteMessageService.consumeAndDeleteMessage(DeletePolicy.AFTER_READ, RECEIVE_MESSAGE_RESULT, QUEUE_URL, mockConsumer);

    InOrder inOrder = Mockito.inOrder(amazonSQS, mockConsumer);
    inOrder.verify(amazonSQS).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
    inOrder.verify(mockConsumer).accept(RECEIVE_MESSAGE_RESULT);
  }

  @Test
  public void whenInvokedWithPolicyAfterProcessThenConsumerShouldBeCalledBeforeDeletingMessages() {
    when(amazonSQS.deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST)).thenReturn(DELETE_MESSAGE_BATCH_RESULT);

    deleteMessageService.consumeAndDeleteMessage(DeletePolicy.AFTER_PROCESS, RECEIVE_MESSAGE_RESULT, QUEUE_URL, mockConsumer);

    InOrder inOrder = Mockito.inOrder(amazonSQS, mockConsumer);
    inOrder.verify(mockConsumer).accept(RECEIVE_MESSAGE_RESULT);
    inOrder.verify(amazonSQS).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
  }

  @Test
  public void whenInvokedWithPolicyAfterProcessAndConsumerFailsThenMessagesAreNotDeleted() {
    RuntimeException exceptionThrown = new RuntimeException();
    doThrow(exceptionThrown).when(mockConsumer).accept(RECEIVE_MESSAGE_RESULT);

    assertThatThrownBy(() -> deleteMessageService.consumeAndDeleteMessage(DeletePolicy.AFTER_PROCESS, RECEIVE_MESSAGE_RESULT, QUEUE_URL, mockConsumer))
      .isEqualTo(exceptionThrown);

    verifyZeroInteractions(amazonSQS);
  }
}
