package org.jusoft.aws.sqs.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterExceptionMethodClass;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.jusoft.aws.sqs.mapper.ConsumerParametersMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.fixture.TestFixtures.MESSAGE_DTO_1;
import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_RESULT;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerInvokeServiceTest {

  @Mock
  private ConsumerParametersMapper consumerParametersMapper;

  @InjectMocks
  private ConsumerInvokerService consumerInvokerService;

  @Test
  public void whenInvokeConsumerThenMethodShouldBeCalled() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    QueueConsumer queueConsumer = QueueConsumer.of(consumerInstance, consumerInstance.getMethod());
    Object[] parameters = new Object[]{MESSAGE_DTO_1};
    when(consumerParametersMapper.createFrom(queueConsumer.getConsumerMethod(), RECEIVE_MESSAGE_RESULT)).thenReturn(parameters);

    consumerInvokerService.invoke(queueConsumer, RECEIVE_MESSAGE_RESULT);

    assertThat(consumerInstance.testValue).isEqualTo(MESSAGE_DTO_1);
  }

  @Test
  public void whenInvocationFailsThenMethodShouldThrowException() throws NoSuchMethodException {
    SingleParameterExceptionMethodClass consumerInstance = new SingleParameterExceptionMethodClass();
    QueueConsumer queueConsumer = QueueConsumer.of(consumerInstance, consumerInstance.getMethod());
    Object[] parameters = new Object[]{MESSAGE_DTO_1};
    when(consumerParametersMapper.createFrom(queueConsumer.getConsumerMethod(), RECEIVE_MESSAGE_RESULT)).thenReturn(parameters);

    assertThatThrownBy(() -> consumerInvokerService.invoke(queueConsumer, RECEIVE_MESSAGE_RESULT))
      .isInstanceOf(IllegalArgumentException.class)
      .hasCauseInstanceOf(InvocationTargetException.class);
  }
}
