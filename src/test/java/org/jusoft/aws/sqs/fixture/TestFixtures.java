package org.jusoft.aws.sqs.fixture;

import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public final class TestFixtures {

  private TestFixtures() {
  }

  public static final String QUEUE_NAME = "testQueue";
  public static final String QUEUE_URL = "queueUrl";
  public static final String RECEIPT_HANDLE_1 = "receiptHandle";
  public static final String BODY_VALUE_1 = "anyBody";
  public static final String BODY_VALUE_2 = "anyBody2";
  public static final String MESSAGE_BODY_1 = "{\"value\":\"" + BODY_VALUE_1 + "\"}";
  public static final String MESSAGE_BODY_2 = "{\"value\":\"" + BODY_VALUE_2 + "\"}";
  public static final String ATTRIBUTE_VALUE_1 = "attributeOne";
  public static final String ATTRIBUTE_VALUE_2 = "attributeTwo";
  public static final String ATTRIBUTE_KEY_1 = "attributeOne";
  public static final String ATTRIBUTE_KEY_2 = "attributeTwo";
  public static final String MESSAGE_ID_1 = "messageId1";
  public static final String MESSAGE_ID_2 = "messageId2";
  public static final Message MESSAGE_1 = new Message()
    .withReceiptHandle(RECEIPT_HANDLE_1)
    .withMessageId(MESSAGE_ID_1)
    .withBody(MESSAGE_BODY_1);
  public static final Message MESSAGE_2 = new Message()
    .withReceiptHandle(RECEIPT_HANDLE_1)
    .withMessageId(MESSAGE_ID_2)
    .withBody(MESSAGE_BODY_2);
  public static final TestDto MESSAGE_DTO_1 = new TestDto(BODY_VALUE_1);
  public static final TestDto MESSAGE_DTO_2 = new TestDto(BODY_VALUE_2);
  public static final ReceiveMessageResult RECEIVE_MESSAGE_RESULT = new ReceiveMessageResult()
    .withMessages(MESSAGE_1);
  public static final ReceiveMessageResult RECEIVE_MESSAGE_RESULT_WITH_TWO_MESSAGES = new ReceiveMessageResult()
    .withMessages(MESSAGE_1, MESSAGE_2);
  public static final ReceiveMessageResult EMPTY_RECEIVE_MESSAGE_RESULT = new ReceiveMessageResult();
  public static final DeleteMessageBatchRequestEntry DELETE_MESSAGE_BATCH_REQUEST_ENTRY = new DeleteMessageBatchRequestEntry(MESSAGE_ID_1, RECEIPT_HANDLE_1);
  public static final List<DeleteMessageBatchRequestEntry> DELETE_MESSAGE_BATCH_REQUEST_ENTRIES = singletonList(DELETE_MESSAGE_BATCH_REQUEST_ENTRY);
  public static final DeleteMessageBatchRequest DELETE_MESSAGE_BATCH_REQUEST = new DeleteMessageBatchRequest(QUEUE_URL, DELETE_MESSAGE_BATCH_REQUEST_ENTRIES);
  public static final String ERROR_CODE = "errorCode";
  public static final String ERROR_MESSAGE = "errorMessage";
  public static final DeleteMessageBatchResult DELETE_MESSAGE_BATCH_RESULT = new DeleteMessageBatchResult();
  public static final DeleteMessageBatchResult DELETE_MESSAGE_BATCH_WITH_ERROR_RESULT = new DeleteMessageBatchResult()
    .withFailed(singletonList(new BatchResultErrorEntry()
      .withCode(ERROR_CODE)
      .withId(MESSAGE_ID_1)
      .withMessage(ERROR_MESSAGE)));
  public static final ReceiveMessageRequest RECEIVE_MESSAGE_REQUEST = new ReceiveMessageRequest(QUEUE_URL);


  static {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1);
    attributes.put(ATTRIBUTE_KEY_2, ATTRIBUTE_VALUE_2);
    MESSAGE_1.withAttributes(attributes);
    MESSAGE_2.withAttributes(attributes);
  }

  public static class TestDto {
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

  public static class SingleParameterMethodClass {
    public TestDto testValue;

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(TestDto testParameter) {
      testValue = testParameter;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class);
    }
  }

  public static class SingleParameterExceptionMethodClass {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(TestDto testParameter) {
      throw new RuntimeException();
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class);
    }
  }

  public static class SingleListParameterMethodClass {
    private List<TestDto> testValue;

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(List<TestDto> testParameter) {
      testValue = testParameter;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", List.class);
    }
  }

  public static class MultipleParametersMethodClass {
    private TestDto testValue;
    private String attributeOne;
    private String attributeTwo;

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody TestDto testBody,
                             @SqsAttribute(ATTRIBUTE_KEY_1) String attributeOne,
                             @SqsAttribute(ATTRIBUTE_KEY_2) String attributeTwo) {
      testValue = testBody;
      this.attributeOne = attributeOne;
      this.attributeTwo = attributeTwo;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class, String.class, String.class);
    }
  }

  public static class MultipleListParametersMethodClass {
    private List<TestDto> testValue;
    private String attributeOne;
    private String attributeTwo;

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody List<TestDto> testBody,
                             @SqsAttribute(ATTRIBUTE_KEY_1) String attributeOne,
                             @SqsAttribute(ATTRIBUTE_KEY_2) String attributeTwo) {
      testValue = testBody;
      this.attributeOne = attributeOne;
      this.attributeTwo = attributeTwo;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", List.class, String.class, String.class);
    }
  }
}
