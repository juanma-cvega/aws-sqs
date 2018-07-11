package org.jusoft.aws.sqs.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_REQUEST;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueuePollServiceTest {

  @Mock
  private ReceiveMessageRequestFactory receiveMessageRequestFactory;
  @Mock
  private MessageConsumerService messageConsumerService;

  @InjectMocks
  private QueuePollService queuePollService;

  @Test
  public void whenStartQueueConsumerThenMessageConsumerServiceShouldBeCalled() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    QueueConsumer queueConsumer = QueueConsumer.of(consumerInstance, consumerInstance.getMethod());
    doAnswer(invocation -> {
      queuePollService.stop();
      return null;
    }).when(messageConsumerService).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
    when(receiveMessageRequestFactory.createFrom(queueConsumer)).thenReturn(RECEIVE_MESSAGE_REQUEST);

    queuePollService.start(queueConsumer);

    verify(messageConsumerService).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
  }

  @Test
  public void whenStartQueueConsumerThenMessageConsumerServiceShouldBeCalledUntilClose() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    QueueConsumer queueConsumer = QueueConsumer.of(consumerInstance, consumerInstance.getMethod());
    doAnswer(invocation -> invocation)
      .doAnswer(invocation -> {
        queuePollService.stop();
        return null;
      }).when(messageConsumerService).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
    when(receiveMessageRequestFactory.createFrom(queueConsumer)).thenReturn(RECEIVE_MESSAGE_REQUEST);

    queuePollService.start(queueConsumer);

    verify(messageConsumerService, times(2)).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
  }

  @Test
  public void whenConsumerFailsThenMessageConsumerShouldContinueUntilClose() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    QueueConsumer queueConsumer = QueueConsumer.of(consumerInstance, consumerInstance.getMethod());
    doThrow(new RuntimeException())
      .doAnswer(invocation -> {
        queuePollService.stop();
        return null;
      }).when(messageConsumerService).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
    when(receiveMessageRequestFactory.createFrom(queueConsumer)).thenReturn(RECEIVE_MESSAGE_REQUEST);

    queuePollService.start(queueConsumer);

    verify(messageConsumerService, times(2)).consumeAndDeleteMessages(queueConsumer, RECEIVE_MESSAGE_REQUEST);
  }
}
