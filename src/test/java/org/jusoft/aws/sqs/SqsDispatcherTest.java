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
import org.jusoft.aws.sqs.annotations.DeletePolicy;
import org.jusoft.aws.sqs.annotations.SqsAttribute;
import org.jusoft.aws.sqs.annotations.SqsBody;
import org.jusoft.aws.sqs.annotations.SqsConsumer;
import org.jusoft.aws.sqs.executor.SqsExecutorFactory;
import org.jusoft.aws.sqs.provider.ConsumerInstanceProvider;
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
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.annotations.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;
import static org.jusoft.aws.sqs.annotations.SqsConsumer.DEFAULT_MAX_MESSAGES_PER_POLL;
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
  private ConsumerInstanceProvider consumerInstanceProvider;
  @Mock
  private ConsumerValidator consumerValidator;
  @Mock
  private AmazonSQS amazonSQS;
  @Spy
  private final SqsSyncExecutionFactory sqsExecutionFactory = new SqsSyncExecutionFactory();
  @Captor
  private ArgumentCaptor<ReceiveMessageRequest> receiveMessageCaptor;

  private DeleteMessageServiceMock deleteMessageService;
  private SqsDispatcher sqsDispatcher;

  @Before
  public void setup() {
    deleteMessageService = new DeleteMessageServiceMock();
    sqsDispatcher = new SqsDispatcher(amazonSQS, new ObjectMapper(), deleteMessageService, consumerInstanceProvider, sqsExecutionFactory, consumerValidator);
  }

  @Test
  public void whenAnyMessageIsConsumedThenTheConsumerShouldBeCalledAndTheMessageDeletedFromSqs() throws NoSuchMethodException {
    TestValidConsumer testObject = new TestValidConsumer();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
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

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isTrue();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithListArgumentThenItShouldBeCalledWithListArgumentAndTheMessageDeletedFromSqs() throws NoSuchMethodException {
    TestListArgumentConsumer testObject = new TestListArgumentConsumer();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
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

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isTrue();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithBodyAndOneAttributeThenItShouldBeCalledWithBodyAndAttributeFromMessage() throws NoSuchMethodException {
    TestAttributeConsumer testObject = new TestAttributeConsumer();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
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

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isTrue();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithManyArgumentsThenItShouldBeCalledWithBodyAndAttributesFromMessage() throws NoSuchMethodException {
    TestAttributesConsumer testObject = new TestAttributesConsumer();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
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

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isTrue();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenAnyMessageIsConsumedByConsumerWithDeletePolicyAfterReadThenMessageShouldBeDeletedBeforeProcessing() {
    //TODO
  }

  @Test
  public void whenThereIsAnExceptionWhileConsumingThenConsumerShouldContinueConsuming() throws NoSuchMethodException {
    TestValidConsumer testObject = new TestValidConsumer();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenAnswer(invocation -> {
        throw new RuntimeException();
      })
      .thenReturn(RECEIVE_MESSAGE_RESULT);

    sqsDispatcher.subscribeConsumers();

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isTrue();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenConsumersAreTerminatedThenLastExecutionIsFinishedBeforeClosing() throws NoSuchMethodException {
    CounterObject testObject = new CounterObject();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .then(invocation -> {
        sqsDispatcher.close();
        return RECEIVE_MESSAGE_RESULT;
      });

    sqsDispatcher.subscribeConsumers();

    assertThat(testObject.count).isEqualTo(2);
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenInvocationFailsThenDeleteMessageServiceShouldReceiveFailAsResultOfConsumingMessage() throws NoSuchMethodException {
    ExceptionObject testObject = new ExceptionObject();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    List<Consumer> consumers = singletonList(consumer);
    when(consumerInstanceProvider.getConsumers()).thenReturn(consumers);
    when(amazonSQS.getQueueUrl(QUEUE_NAME)).thenReturn(new GetQueueUrlResult().withQueueUrl(QUEUE_URL));
    when(amazonSQS.receiveMessage(receiveMessageCaptor.capture()))
      .thenReturn(RECEIVE_MESSAGE_RESULT)
      .thenReturn(new ReceiveMessageResult().withMessages(emptyList()));

    sqsDispatcher.subscribeConsumers();

    assertThat(deleteMessageService.isCalledAndResultConsumer).isNotNull().isFalse();
    verify(consumerValidator).isValid(consumers);
  }

  @Test
  public void whenValidationOfAnyConsumerFailsThenThereShouldBeAnException() throws NoSuchMethodException {
    ExceptionObject testObject = new ExceptionObject();
    Consumer consumer = Consumer.of(testObject, testObject.getMethod());
    when(consumerInstanceProvider.getConsumers()).thenReturn(singletonList(consumer));
    IllegalArgumentException exceptionThrown = new IllegalArgumentException();
    doThrow(exceptionThrown).when(consumerValidator).isValid(singletonList(consumer));

    assertThatThrownBy(() -> sqsDispatcher.subscribeConsumers()).isEqualTo(exceptionThrown);
    verifyZeroInteractions(amazonSQS);
  }

  private static class DeleteMessageServiceMock implements DeleteMessageService {

    private Boolean isCalledAndResultConsumer = null;

    @Override
    public void deleteMessage(DeletePolicy deletePolicy, ReceiveMessageResult receiveMessageResult, String queueUrl, Function<ReceiveMessageResult, Boolean> messageConsumer) {
      isCalledAndResultConsumer = messageConsumer.apply(receiveMessageResult);
    }
  }

  private class TestValidConsumer {
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

  private class TestListArgumentConsumer {
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

  private class TestAttributeConsumer {
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

  private class TestAttributesConsumer {
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

  private static class SqsSyncExecutionFactory implements SqsExecutorFactory {
    @Override
    public ExecutorService createFor(Iterable<Consumer> consumerProperties) {
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
