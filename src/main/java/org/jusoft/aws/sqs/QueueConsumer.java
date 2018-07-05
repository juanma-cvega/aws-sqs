package org.jusoft.aws.sqs;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class QueueConsumer {

  private final Object consumerInstance;
  private final Method consumerMethod;

  private QueueConsumer(Object consumerInstance, Method consumerMethod) {
    this.consumerInstance = consumerInstance;
    this.consumerMethod = consumerMethod;
  }

  public static QueueConsumer of(Object consumerInstance, Method consumerMethod) {
    return new QueueConsumer(consumerInstance, consumerMethod);
  }

  public Object getConsumerInstance() {
    return consumerInstance;
  }

  public Method getConsumerMethod() {
    return consumerMethod;
  }

  public SqsConsumer getAnnotation() {
    return consumerMethod.getAnnotation(SqsConsumer.class);
  }

  public List<Class<?>> getParametersTypes() {
    return Arrays.asList(consumerMethod.getParameterTypes());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueueConsumer queueConsumer = (QueueConsumer) o;

    return new EqualsBuilder()
      .append(consumerInstance, queueConsumer.consumerInstance)
      .append(consumerMethod, queueConsumer.consumerMethod)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(consumerInstance)
      .append(consumerMethod)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "QueueConsumer{" +
      "consumerInstance=" + consumerInstance +
      ", consumerMethod=" + consumerMethod +
      '}';
  }
}
