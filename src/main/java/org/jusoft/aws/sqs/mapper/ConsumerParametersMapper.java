package org.jusoft.aws.sqs.mapper;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.apache.commons.lang3.Validate;
import org.jusoft.aws.sqs.annotation.SqsAttribute;
import org.jusoft.aws.sqs.annotation.SqsBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Deserialises body and attributes from AWS SQS messages into the consumer parameters, using the @{@link SqsBody} and
 * {@link SqsAttribute} annotations as hints.
 * The implementation expects the consumer has passed all validations so there is only one (or none) {@link SqsBody} and
 * all {@link SqsAttribute} annotations are added to {@link String} parameters.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ConsumerParametersMapper {

  private final MessageMapper messageMapper;

  /**
   * Creates a {@link ConsumerParametersMapper} with a {@link MessageMapper} field. The {@link MessageMapper} is used
   * to deserialize the body into the proper java object.
   */
  public ConsumerParametersMapper(MessageMapper messageMapper) {
    this.messageMapper = messageMapper;
  }

  /**
   * Creates the consumer expected parameters from the {@link com.amazonaws.services.sqs.model.ReceiveMessageResult}.
   *
   * @param consumer             the consumer of the message. It's used to find the information about its parameters.
   * @param receiveMessageResult AWS SQS message.
   */
  public Object[] createFrom(Method consumer, ReceiveMessageResult receiveMessageResult) {
    Object[] result;
    if (isOnlyBodyExpected(consumer)) {
      result = new Object[]{createBodyFrom(consumer, receiveMessageResult)};
    } else {
      result = Stream.of(consumer.getParameters())
        .map(parameter -> toInstance(receiveMessageResult, parameter))
        .toArray();
    }
    return result;
  }

  private boolean isOnlyBodyExpected(Method consumer) {
    return consumer.getParameters().length == 1;
  }

  private Object createBodyFrom(Method consumer, ReceiveMessageResult receiveMessageResult) {
    return createBodyFrom(receiveMessageResult, consumer.getParameters()[0]);
  }

  private Object toInstance(ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    return Stream.of(parameter.getAnnotations())
      .filter(isAnySqsAnnotation())
      .findFirst()
      .map(annotation -> createParameterInstanceFrom(annotation, receiveMessageResult, parameter))
      .orElse(null); //Parameter initiated to null. Not happening as long as validation rules are in place
  }

  private Predicate<Annotation> isAnySqsAnnotation() {
    return annotation -> annotation.annotationType() == SqsBody.class || annotation.annotationType() == SqsAttribute.class;
  }

  private Object createParameterInstanceFrom(Annotation annotation, ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Object result;
    if (annotation.annotationType() == SqsBody.class) {
      result = createBodyFrom(receiveMessageResult, parameter);
    } else {
      result = getAttributeFrom(receiveMessageResult, (SqsAttribute) annotation);
    }
    return result;
  }

  private Object createBodyFrom(ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Object result;
    if (isListOfMessages(parameter.getType())) {
      result = createListParameterFrom(receiveMessageResult, parameter);
    } else {
      result = createSingleParameterFrom(receiveMessageResult, parameter);
    }
    return result;
  }

  private boolean isListOfMessages(Class<?> type) {
    return type.equals(List.class);
  }

  private Object createListParameterFrom(ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Class<?> collectionType = getParameterClassTypeFrom(parameter);
    return receiveMessageResult.getMessages().stream()
      .map(message -> messageMapper.deserialize(message.getBody(), collectionType))
      .collect(toList());
  }

  private Object createSingleParameterFrom(ReceiveMessageResult receiveMessageResult, Parameter parameter) {
    Validate.isTrue(receiveMessageResult.getMessages().size() == 1,
      "There can only be one message when parameter is not a list");
    Message message = receiveMessageResult.getMessages().get(0);
    return messageMapper.deserialize(message.getBody(), parameter.getType());
  }

  private String getAttributeFrom(ReceiveMessageResult receiveMessageResult, SqsAttribute parameterAnnotation) {
    String attributeName = parameterAnnotation.value();
    Message message = receiveMessageResult.getMessages().get(0); //Only one message is allowed when using attributes
    return message.getAttributes().get(attributeName);
  }

  private Class<?> getParameterClassTypeFrom(Parameter collectionType) {
    return (Class<?>) ((ParameterizedType) collectionType.getParameterizedType()).getActualTypeArguments()[0];
  }
}
