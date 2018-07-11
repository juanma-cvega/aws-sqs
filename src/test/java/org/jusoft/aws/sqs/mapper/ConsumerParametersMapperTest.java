package org.jusoft.aws.sqs.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.fixture.TestFixtures.MultipleListParametersMethodClass;
import org.jusoft.aws.sqs.fixture.TestFixtures.MultipleParametersMethodClass;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleListParameterMethodClass;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.jusoft.aws.sqs.fixture.TestFixtures.TestDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusoft.aws.sqs.fixture.TestFixtures.ATTRIBUTE_KEY_1;
import static org.jusoft.aws.sqs.fixture.TestFixtures.ATTRIBUTE_VALUE_1;
import static org.jusoft.aws.sqs.fixture.TestFixtures.ATTRIBUTE_VALUE_2;
import static org.jusoft.aws.sqs.fixture.TestFixtures.MESSAGE_BODY_1;
import static org.jusoft.aws.sqs.fixture.TestFixtures.MESSAGE_BODY_2;
import static org.jusoft.aws.sqs.fixture.TestFixtures.MESSAGE_DTO_1;
import static org.jusoft.aws.sqs.fixture.TestFixtures.MESSAGE_DTO_2;
import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;
import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_RESULT;
import static org.jusoft.aws.sqs.fixture.TestFixtures.RECEIVE_MESSAGE_RESULT_WITH_TWO_MESSAGES;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerParametersMapperTest {

  private static final String NOT_VALID_ATTRIBUTE_KEY = "notValidAttributeKey";

  @Mock
  private MessageMapper messageMapper;

  @InjectMocks
  private ConsumerParametersMapper mapper;

  @Before
  public void setup() {
    when(messageMapper.deserialize(MESSAGE_BODY_1, TestDto.class)).thenReturn(MESSAGE_DTO_1);
    when(messageMapper.deserialize(MESSAGE_BODY_2, TestDto.class)).thenReturn(MESSAGE_DTO_2);
  }

  @Test
  public void whenThereIsOnlyOneParameterThenItShouldBeCreatedFromTheBodyOfTheMessage() throws NoSuchMethodException {
    SingleParameterMethodClass object = new SingleParameterMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(TestDto.class);
    assertThat(deserializedParameters[0]).isEqualTo(MESSAGE_DTO_1);
  }

  @Test
  public void whenThereIsOnlyOneListParameterAndOneMessageThenItShouldBeCreatedFromTheBodyOfTheMessage() throws NoSuchMethodException {
    SingleListParameterMethodClass object = new SingleListParameterMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(List.class);
    List<TestDto> deserializedObject = (List<TestDto>) deserializedParameters[0];
    assertThat(deserializedObject).containsExactly(MESSAGE_DTO_1);
  }

  @Test
  public void whenThereIsOnlyOneListParameterAndTwoMessagesThenItShouldBeCreatedFromTheBodyOfTheMessages() throws NoSuchMethodException {
    SingleListParameterMethodClass object = new SingleListParameterMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT_WITH_TWO_MESSAGES);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(List.class);
    List<TestDto> deserializedParameter = (List<TestDto>) deserializedParameters[0];
    assertThat(deserializedParameter).containsExactly(MESSAGE_DTO_1, MESSAGE_DTO_2);
  }

  @Test
  public void whenThereAreObjectAndAttributesWithOneMessageThenThereShouldBeABodyAndAttributesMappedFromMessage() throws NoSuchMethodException {
    MultipleParametersMethodClass object = new MultipleParametersMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(TestDto.class);
    assertThat(deserializedParameters[0]).isEqualTo(MESSAGE_DTO_1);
    assertThat(deserializedParameters[1]).isInstanceOf(String.class);
    assertThat(deserializedParameters[1]).isEqualTo(ATTRIBUTE_VALUE_1);
    assertThat(deserializedParameters[2]).isInstanceOf(String.class);
    assertThat(deserializedParameters[2]).isEqualTo(ATTRIBUTE_VALUE_2);
  }

  @Test
  public void whenThereAreListObjectAndAttributesWithOneMessageThenThereShouldBeAListBodyAndAttributesMappedFromMessage() throws NoSuchMethodException {
    MultipleListParametersMethodClass object = new MultipleListParametersMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(List.class);
    List<TestDto> deserializedParameter = (List<TestDto>) deserializedParameters[0];
    assertThat(deserializedParameter).containsExactly(MESSAGE_DTO_1);
    assertThat(deserializedParameters[1]).isInstanceOf(String.class);
    assertThat(deserializedParameters[1]).isEqualTo(ATTRIBUTE_VALUE_1);
    assertThat(deserializedParameters[2]).isInstanceOf(String.class);
    assertThat(deserializedParameters[2]).isEqualTo(ATTRIBUTE_VALUE_2);
  }

  @Test
  public void whenThereAreListObjectAndAttributesWithTwoMessagesThenThereShouldBeAListBodyAndAttributesMappedFromMessages() throws NoSuchMethodException {
    MultipleListParametersMethodClass object = new MultipleListParametersMethodClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT_WITH_TWO_MESSAGES);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(List.class);
    List<TestDto> deserializedParameter = (List<TestDto>) deserializedParameters[0];
    assertThat(deserializedParameter).containsExactly(MESSAGE_DTO_1, MESSAGE_DTO_2);
    assertThat(deserializedParameters[1]).isInstanceOf(String.class);
    assertThat(deserializedParameters[1]).isEqualTo(ATTRIBUTE_VALUE_1);
    assertThat(deserializedParameters[2]).isInstanceOf(String.class);
    assertThat(deserializedParameters[2]).isEqualTo(ATTRIBUTE_VALUE_2);
  }

  @Test
  public void whenThereIsAnAttributeNotFoundThenTheParameterShouldBeNull() throws NoSuchMethodException {
    MultipleParametersMethodWithWrongParameterClass object = new MultipleParametersMethodWithWrongParameterClass();

    Object[] deserializedParameters = mapper.createFrom(object.getMethod(), RECEIVE_MESSAGE_RESULT);

    assertThat(deserializedParameters).hasSize(object.getMethod().getParameterCount());
    assertThat(deserializedParameters[0]).isInstanceOf(TestDto.class);
    assertThat(deserializedParameters[0]).isEqualTo(MESSAGE_DTO_1);
    assertThat(deserializedParameters[1]).isInstanceOf(String.class);
    assertThat(deserializedParameters[1]).isEqualTo(ATTRIBUTE_VALUE_1);
    assertThat(deserializedParameters[2]).isNull();
  }

  private static class MultipleParametersMethodWithWrongParameterClass {
    private TestDto testValue;
    private String attributeOne;
    private String attributeTwo;

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer(@SqsBody TestDto testBody,
                             @SqsAttribute(ATTRIBUTE_KEY_1) String attributeOne,
                             @SqsAttribute(NOT_VALID_ATTRIBUTE_KEY) String attributeTwo) {
      testValue = testBody;
      this.attributeOne = attributeOne;
      this.attributeTwo = attributeTwo;
    }

    public Method getMethod() throws NoSuchMethodException {
      return getClass().getMethod("testConsumer", TestDto.class, String.class, String.class);
    }

  }
}
