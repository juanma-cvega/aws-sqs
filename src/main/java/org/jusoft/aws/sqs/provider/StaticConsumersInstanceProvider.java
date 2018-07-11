package org.jusoft.aws.sqs.provider;

import org.apache.commons.lang3.Validate;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the {@link ConsumersInstanceProvider} that takes an already created {@link Iterable} of either
 * {@link QueueConsumer}s or of objects annotated with {@link SqsConsumer} methods
 */
public class StaticConsumersInstanceProvider implements ConsumersInstanceProvider {

  private final Iterable<QueueConsumer> consumers;

  private StaticConsumersInstanceProvider(Iterable<QueueConsumer> consumers) {
    this.consumers = consumers;
    Validate.notNull(this.consumers);
  }

  /**
   * The {@link Iterable} contains the {@link QueueConsumer}s to be used to consume AWS SQS messages.
   */
  public static StaticConsumersInstanceProvider ofConsumers(Iterable<QueueConsumer> consumers) {
    return new StaticConsumersInstanceProvider(consumers);
  }

  /**
   * The {@link Iterable} contains objects with methods annotated with {@link SqsConsumer}. All other methods
   * are filtered out when creating the final collection.
   */
  public static StaticConsumersInstanceProvider ofBeans(Iterable<Object> consumers) {
    return new StaticConsumersInstanceProvider(StreamSupport.stream(consumers.spliterator(), false)
      .map(StaticConsumersInstanceProvider::toConsumerByAnnotatedMethod)
      .flatMap(List::stream)
      .collect(toList()));
  }

  private static List<QueueConsumer> toConsumerByAnnotatedMethod(Object object) {
    return getConsumersFrom(object).stream()
      .map(method -> QueueConsumer.of(object, method))
      .collect(toList());
  }

  private static List<Method> getConsumersFrom(Object object) {
    return stream(object.getClass().getDeclaredMethods())
      .filter(isAnnotatedConsumer())
      .collect(toList());
  }

  private static Predicate<Method> isAnnotatedConsumer() {
    return method -> method.getAnnotation(SqsConsumer.class) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<QueueConsumer> getConsumers() {
    return StreamSupport.stream(consumers.spliterator(), false).collect(toList());
  }
}
