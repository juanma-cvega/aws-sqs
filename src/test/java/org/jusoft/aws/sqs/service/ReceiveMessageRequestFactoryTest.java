package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.assertj.swing.security.NoExitSecurityManagerInstaller;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_URL;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveMessageRequestFactoryTest {

  private static final String SYSTEM_EXIT_MESSAGE = "System exit";

  @Mock
  private AmazonSQS amazonSQS;
  @InjectMocks
  private ReceiveMessageRequestFactory factory;

  private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

  @BeforeClass
  public static void setUpOnce() {
    noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(i -> {
      throw new RuntimeException(SYSTEM_EXIT_MESSAGE);
    });
  }

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

    assertThatThrownBy(() -> factory.createFrom(queueConsumer))
      .isInstanceOf(RuntimeException.class)
      .hasMessage(SYSTEM_EXIT_MESSAGE);
  }

  private QueueConsumer getQueueConsumer() throws NoSuchMethodException {
    SingleParameterMethodClass consumerInstance = new SingleParameterMethodClass();
    Method consumerMethod = consumerInstance.getMethod();
    return QueueConsumer.of(consumerInstance, consumerMethod);
  }
}
