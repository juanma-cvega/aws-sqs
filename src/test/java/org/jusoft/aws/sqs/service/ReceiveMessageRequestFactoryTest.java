package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_URL;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveMessageRequestFactoryTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Mock
  private AmazonSQS amazonSQS;
  @InjectMocks
  private ReceiveMessageRequestFactory factory;


  @Test
  public void whenCreateFromQueueConsumerThenRequestShouldContainAnnotationConfiguration() throws NoSuchMethodException {
    QueueConsumer queueConsumer = getQueueConsumer();
    when(amazonSQS.getQueueUrl(queueConsumer.getAnnotation().value())).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));

    ReceiveMessageRequest request = factory.createFrom(queueConsumer);

    assertThat(request.getWaitTimeSeconds()).isEqualTo(queueConsumer.getAnnotation().longPolling());
    assertThat(request.getMaxNumberOfMessages()).isEqualTo(queueConsumer.getAnnotation().maxMessagesPerPoll());
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);
  }

  @Test
  public void whenCannotFindQueueUrlFromQueueNameThenThereShouldBeAnException() throws NoSuchMethodException {
    QueueConsumer queueConsumer = getQueueConsumer();
    when(amazonSQS.getQueueUrl(queueConsumer.getAnnotation().value())).thenThrow(new QueueDoesNotExistException(""));

    exit.expectSystemExit();
    factory.createFrom(queueConsumer);
  }

  private QueueConsumer getQueueConsumer() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    Method consumerMethod = consumerInstance.getMethod();
    return QueueConsumer.of(consumerInstance, consumerMethod);
  }
}
