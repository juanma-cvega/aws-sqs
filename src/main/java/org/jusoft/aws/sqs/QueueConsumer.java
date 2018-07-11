package org.jusoft.aws.sqs;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Contains a consumer. Any consumer requires an instance of the class used as a consumer and the {@link Method} to
 * invoke from the instance.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class QueueConsumer {

  private final Object consumerInstance;
  private final Method consumerMethod;

  /**
   * Constructor of the {@link QueueConsumer}. Parameters must be not null and the {@link Method} argument must be
   * annotated with @{@link SqsConsumer} annotation.
   *
   * @param consumerInstance
   * @param consumerMethod
   */
  private QueueConsumer(Object consumerInstance, Method consumerMethod) {
    this.consumerInstance = consumerInstance;
    this.consumerMethod = consumerMethod;
    Validate.notNull(this.consumerInstance);
    Validate.notNull(this.consumerMethod);
    Validate.notNull(getAnnotation());
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

  /**
   * Returns the {@link SqsConsumer} annotation used in the consumerMethod field.
   */
  public SqsConsumer getAnnotation() {
    return consumerMethod.getAnnotation(SqsConsumer.class);
  }

  /**
   * Returns a {@link List} with all the consumerMethod declared parameter types
   */
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
