package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.annotation.DeletePolicy.AFTER_PROCESS;
import static org.jusoft.aws.sqs.annotation.DeletePolicy.AFTER_READ;
import static org.jusoft.aws.sqs.fixture.TestFixtures.DELETE_MESSAGE_BATCH_REQUEST;
import static org.jusoft.aws.sqs.fixture.TestFixtures.DELETE_MESSAGE_BATCH_RESULT;
import static org.jusoft.aws.sqs.fixture.TestFixtures.DELETE_MESSAGE_BATCH_WITH_ERROR_RESULT;
import static org.jusoft.aws.sqs.fixture.TestFixtures.EMPTY_RECEIVE_MESSAGE_RESULT;
import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_REQUEST;
import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_RESULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageConsumerServiceTest {

  @Mock
  private SqsConsumer sqsConsumerAnnotation;
  @Mock
  private QueueConsumer queueConsumer;
  @Mock
  private AmazonSQS amazonSQS;
  @Mock
  private ConsumerInvokerService consumerInvokerService;

  @InjectMocks
  private MessageConsumerService messageConsumerService;

  @Test
  public void whenRequestResultDoesNotContainMessagesThenConsumerShouldNotBeCalledAndMessagesShouldNotBeDeleted() {
    when(amazonSQS.receiveMessage(RECEIVE_MESSAGE_REQUEST)).thenReturn(EMPTY_RECEIVE_MESSAGE_RESULT);

    messageConsumerService.consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);

    verify(amazonSQS, times(0)).deleteMessageBatch(any());
    verifyZeroInteractions(consumerInvokerService);
  }

  @Test
  public void whenDeletePolicyIsAfterReadThenMessagesShouldBeDeletedBeforeConsumingThem() {
    when(amazonSQS.receiveMessage(RECEIVE_MESSAGE_REQUEST)).thenReturn(RECEIVE_MESSAGE_RESULT);
    when(queueConsumer.getAnnotation()).thenReturn(sqsConsumerAnnotation);
    when(sqsConsumerAnnotation.deletePolicy()).thenReturn(AFTER_READ);
    when(amazonSQS.deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST)).thenReturn(DELETE_MESSAGE_BATCH_RESULT);

    messageConsumerService.consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);

    InOrder inOrder = Mockito.inOrder(amazonSQS, consumerInvokerService);
    inOrder.verify(amazonSQS).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
    inOrder.verify(consumerInvokerService).invoke(queueConsumer, RECEIVE_MESSAGE_RESULT);
  }

  @Test
  public void whenDeletePolicyIsAfterProcessAndConsumerInvocationFailsThenMessagesShouldNotBeDeleted() {
    when(amazonSQS.receiveMessage(RECEIVE_MESSAGE_REQUEST)).thenReturn(RECEIVE_MESSAGE_RESULT);
    when(queueConsumer.getAnnotation()).thenReturn(sqsConsumerAnnotation);
    when(sqsConsumerAnnotation.deletePolicy()).thenReturn(AFTER_PROCESS);
    RuntimeException exceptionThrown = new RuntimeException();
    doThrow(exceptionThrown).when(consumerInvokerService).invoke(queueConsumer, RECEIVE_MESSAGE_RESULT);

    assertThatThrownBy(() -> messageConsumerService.consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST))
      .isEqualTo(exceptionThrown);

    verify(amazonSQS, times(0)).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
  }

  @Test
  public void whenDeletePolicyIsAfterProcessThenMessagesShouldBeDeletedAfterConsumingThem() {
    when(amazonSQS.receiveMessage(RECEIVE_MESSAGE_REQUEST)).thenReturn(RECEIVE_MESSAGE_RESULT);
    when(queueConsumer.getAnnotation()).thenReturn(sqsConsumerAnnotation);
    when(sqsConsumerAnnotation.deletePolicy()).thenReturn(AFTER_PROCESS);
    when(amazonSQS.deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST)).thenReturn(DELETE_MESSAGE_BATCH_RESULT);

    messageConsumerService.consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);

    InOrder inOrder = Mockito.inOrder(amazonSQS, consumerInvokerService);
    inOrder.verify(consumerInvokerService).invoke(queueConsumer, RECEIVE_MESSAGE_RESULT);
    inOrder.verify(amazonSQS).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
  }

  @Test
  public void whenThereIsAnErrorWhileDeletingMessagesFromSqsThenFailedMessagesShouldBeLogged() {
    when(amazonSQS.receiveMessage(RECEIVE_MESSAGE_REQUEST)).thenReturn(RECEIVE_MESSAGE_RESULT);
    when(queueConsumer.getAnnotation()).thenReturn(sqsConsumerAnnotation);
    when(sqsConsumerAnnotation.deletePolicy()).thenReturn(AFTER_PROCESS);
    when(amazonSQS.deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST)).thenReturn(DELETE_MESSAGE_BATCH_WITH_ERROR_RESULT);

    messageConsumerService.consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);

    InOrder inOrder = Mockito.inOrder(amazonSQS, consumerInvokerService);
    inOrder.verify(consumerInvokerService).invoke(queueConsumer, RECEIVE_MESSAGE_RESULT);
    inOrder.verify(amazonSQS).deleteMessageBatch(DELETE_MESSAGE_BATCH_REQUEST);
    //FIXME add a test appender to verify logs
  }
}
