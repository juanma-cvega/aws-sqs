package org.jusoft.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.annotation.DeletePolicy;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.executor.ExecutorFactory;
import org.jusoft.aws.sqs.mapper.ConsumerParametersMapper;
import org.jusoft.aws.sqs.mapper.JacksonMessageMapper;
import org.jusoft.aws.sqs.provider.ConsumersInstanceProvider;
import org.jusoft.aws.sqs.service.MessageConsumerService;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.annotation.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;
import static org.jusoft.aws.sqs.annotation.SqsConsumer.DEFAULT_MAX_MESSAGES_PER_POLL;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsDispatcherTest {

  private static final String QUEUE_NAME = "testQueue";
  private static final String QUEUE_URL = "queueUrl";
  private static final String RECEIPT_HANDLE_1 = "receiptHandle";
  private static final String BODY_VALUE = "anyBody";
  private static final String MESSAGE_BODY_1 = "{\"value\":\"" + BODY_VALUE + "\"}";
  private static final TestDto MESSAGE_TEST_DTO = new TestDto(BODY_VALUE);
  private static final String MESSAGE_ID_1 = "messageId1";
  private static final String ATTRIBUTE_KEY_1 = "attributeKey1";
  private static final String ATTRIBUTE_VALUE_1 = "attributeValue1";
  private static final String ATTRIBUTE_KEY_2 = "attributeKey2";
  private static final String ATTRIBUTE_VALUE_2 = "attributeValue2";
  private static final Map<String, String> MESSAGE_ATTRIBUTES = new HashMap<>();
  private static final Message MESSAGE_1 = new Message()
    .withReceiptHandle(RECEIPT_HANDLE_1)
    .withMessageId(MESSAGE_ID_1)
    .withBody(MESSAGE_BODY_1)
    .withAttributes(MESSAGE_ATTRIBUTES);
  private static final ReceiveMessageResult RECEIVE_MESSAGE_RESULT = new ReceiveMessageResult()
    .withMessages(MESSAGE_1);

  static {
    MESSAGE_ATTRIBUTES.put(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1);
    MESSAGE_ATTRIBUTES.put(ATTRIBUTE_KEY_2, ATTRIBUTE_VALUE_2);
  }

  @Mock
  private ConsumersInstanceProvider consumersInstanceProvider;
  @Mock
  private ConsumerValidator consumerValidator;
  private final ConsumerParametersMapper consumerParametersMapper = new ConsumerParametersMapper(new JacksonMessageMapper(new ObjectMapper()));
  @Mock
  private AmazonSQS amazonSQS;
  @Spy
  private final SyncExecutionFactory sqsExecutionFactory = new SyncExecutionFactory();
  @Captor
  private ArgumentCaptor<ReceiveMessageRequest> receiveMessageCaptor;

  private final MessageConsumerServiceMock deleteMessageService = new MessageConsumerServiceMock();
  private SqsDispatcher sqsDispatcher;

  @Before
  public void setup() {
    ReceiveMessageRequestFactory receiveMessageRequestFactory = new ReceiveMessageRequestFactory(amazonSQS);
    sqsDispatcher = new SqsDispatcher(amazonSQS, consumerParametersMapper, deleteMessageService, consumersInstanceProvider, sqsExecutionFactory, consumerValidator, receiveMessageRequestFactory);
  }

  @Test
  public void whenAnyMessageIsConsumedThenTheConsumerShouldBeCalledAndTheMessageDeletedFromSqs() throws NoSuchMethodException {
    TestValidQueueConsumer testObject = new TestValidQueueConsumer();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.testValue).isEqualTo(MESSAGE_TEST_DTO);
    ReceiveMessageRequest request = receiveMessageCaptor.getValue();
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);
    assertThat(request.getMaxNumberOfMessages()).isEqualTo(DEFAULT_MAX_MESSAGES_PER_POLL);
    assertThat(request.getWaitTimeSeconds()).isEqualTo(DEFAULT_MAX_LONG_POLLING_IN_SECONDS);
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isTrue();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithListArgumentThenItShouldBeCalledWithListArgumentAndTheMessageDeletedFromSqs() throws NoSuchMethodException {
    TestListArgumentQueueConsumer testObject = new TestListArgumentQueueConsumer();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.testValue).isEqualTo(singletonList(MESSAGE_TEST_DTO));
    ReceiveMessageRequest request = receiveMessageCaptor.getValue();
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);
    assertThat(request.getMaxNumberOfMessages()).isEqualTo(DEFAULT_MAX_MESSAGES_PER_POLL);
    assertThat(request.getWaitTimeSeconds()).isEqualTo(DEFAULT_MAX_LONG_POLLING_IN_SECONDS);
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isTrue();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithBodyAndOneAttributeThenItShouldBeCalledWithBodyAndAttributeFromMessage() throws NoSuchMethodException {
    TestAttributeQueueConsumer testObject = new TestAttributeQueueConsumer();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.testValue).isEqualTo(MESSAGE_TEST_DTO);
    assertThat(testObject.testAttribute).isEqualTo(ATTRIBUTE_VALUE_1);
    ReceiveMessageRequest request = receiveMessageCaptor.getValue();
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);
    assertThat(request.getMaxNumberOfMessages()).isEqualTo(DEFAULT_MAX_MESSAGES_PER_POLL);
    assertThat(request.getWaitTimeSeconds()).isEqualTo(DEFAULT_MAX_LONG_POLLING_IN_SECONDS);
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isTrue();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithManyArgumentsThenItShouldBeCalledWithBodyAndAttributesFromMessage() throws NoSuchMethodException {
    TestAttributesQueueConsumer testObject = new TestAttributesQueueConsumer();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.testValue).isEqualTo(MESSAGE_TEST_DTO);
    assertThat(testObject.testAttribute1).isEqualTo(ATTRIBUTE_VALUE_1);
    assertThat(testObject.testAttribute2).isEqualTo(ATTRIBUTE_VALUE_2);
    ReceiveMessageRequest request = receiveMessageCaptor.getValue();
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);
    assertThat(request.getMaxNumberOfMessages()).isEqualTo(DEFAULT_MAX_MESSAGES_PER_POLL);
    assertThat(request.getWaitTimeSeconds()).isEqualTo(DEFAULT_MAX_LONG_POLLING_IN_SECONDS);
    assertThat(request.getQueueUrl()).isEqualTo(QUEUE_URL);

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isTrue();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithDeletePolicyAfterReadThenMessageShouldBeDeletedBeforeProcessing() {
    //TODO
  }

  @Test
  public void whenThereIsAnExceptionWhileConsumingThenConsumerShouldContinueConsuming() throws NoSuchMethodException {
    TestValidQueueConsumer testObject = new TestValidQueueConsumer();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenAnswer(invocation -> {
        throw new RuntimeException();
      })
      .thenReturn(RECEIVE_MESSAGE_RESULT);

    sqsDispatcher.subscribeConsumers();

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isTrue();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenConsumersAreTerminatedThenLastExecutionIsFinishedBeforeClosing() throws NoSuchMethodException {
    CounterObject testObject = new CounterObject();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .then(invocation -> {
        sqsDispatcher.close();
        return RECEIVE_MESSAGE_RESULT;
      });

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.count).isEqualTo(2);
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenInvocationFailsThenDeleteMessageServiceShouldNotFinishExecution() throws NoSuchMethodException {
    ExceptionObject testObject = new ExceptionObject();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumer);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(deleteMessageService.isCalledConsumerAndResultOk).isNotNull().isFalse();
    verify(consumerValidator).isValid(queueConsumers);
  }

  @Test
  public void whenValidationOfAnyConsumerFailsThenThereShouldBeAnException() throws NoSuchMethodException {
    ExceptionObject testObject = new ExceptionObject();
    QueueConsumer queueConsumer = QueueConsumer.of(testObject, testObject.getMethod());
    when(consumersInstanceProvider.getConsumers()).thenReturn(singletonList(queueConsumer));
    IllegalArgumentException exceptionThrown = new IllegalArgumentException();
    doThrow(exceptionThrown).when(consumerValidator).isValid(singletonList(queueConsumer));

    assertThatThrownBy(() -> sqsDispatcher.subscribeConsumers()).isEqualTo(exceptionThrown);
    verifyZeroInteractions(amazonSQS);
  }

  private static class MessageConsumerServiceMock implements MessageConsumerService {

    private Boolean isCalledConsumerAndResultOk = null;

    @Override
    public void consumeAndDeleteMessage(DeletePolicy deletePolicy, ReceiveMessageResult receiveMessageResult, String queueUrl, Consumer<ReceiveMessageResult> messageConsumer) {
      try {
        messageConsumer.accept(receiveMessageResult);
        isCalledConsumerAndResultOk = true;
      } catch (Exception e) {
        isCalledConsumerAndResultOk = false;
      }
    }
  }

  private class TestValidQueueConsumer {
    private TestDto testValue;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(TestDto testParameter) throws InterruptedException {
      testValue = testParameter;
      sqsDispatcher.close();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class);
    }
  }

  private class TestListArgumentQueueConsumer {
    private List<TestDto> testValue;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(List<TestDto> testParameter) throws InterruptedException {
      testValue = testParameter;
      sqsDispatcher.close();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", List.class);
    }
  }

  private class TestAttributeQueueConsumer {
    private TestDto testValue;
    private String testAttribute;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(@SqsBody TestDto testParameter, @SqsAttribute(ATTRIBUTE_KEY_1) String attribute) throws InterruptedException {
      testValue = testParameter;
      testAttribute = attribute;
      sqsDispatcher.close();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class, String.class);
    }
  }

  private class TestAttributesQueueConsumer {
    private TestDto testValue;
    private String testAttribute1;
    private String testAttribute2;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(@SqsAttribute(ATTRIBUTE_KEY_1) String attribute1, @SqsBody TestDto testParameter, @SqsAttribute(ATTRIBUTE_KEY_2) String attribute2) throws InterruptedException {
      testValue = testParameter;
      testAttribute1 = attribute1;
      testAttribute2 = attribute2;
      sqsDispatcher.close();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", String.class, TestDto.class, String.class);
    }
  }

  private class ExceptionObject {

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(TestDto testParameter) throws InterruptedException {
      sqsDispatcher.close();
      throw new RuntimeException();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class);
    }
  }

  private class CounterObject {
    private int count = 0;

    @SqsConsumer(value = QUEUE_NAME)
    public void testConsumer(TestDto testParameter) {
      count++;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class);
    }
  }

  private static class TestDto {
    private final String value;

    private TestDto() {
      value = null;
    }

    private TestDto(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestDto testDto = (TestDto) o;

      return new EqualsBuilder()
        .append(value, testDto.value)
        .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37)
        .append(value)
        .toHashCode();
    }
  }

  private static class SyncExecutionFactory implements ExecutorFactory {
    @Override
    public ExecutorService createFor(Iterable<QueueConsumer> consumerProperties) {
      return new ExecutorService() {
        @Override
        public void shutdown() {

        }

        @Override
        public List<Runnable> shutdownNow() {
          return null;
        }

        @Override
        public boolean isShutdown() {
          return false;
        }

        @Override
        public boolean isTerminated() {
          return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
          return false;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
          return null;
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
          return null;
        }

        @Override
        public Future<?> submit(Runnable task) {
          task.run();
          return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
          return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
          return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
          return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
          return null;
        }

        @Override
        public void execute(Runnable command) {

        }
      };
    }
  }
}
