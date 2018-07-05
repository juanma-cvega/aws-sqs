package org.jusoft.aws.sqs.provider;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class StaticConsumerInstanceProvider implements ConsumerInstanceProvider {

  private final Iterable<Consumer> consumers;

  private StaticConsumerInstanceProvider(Iterable<Consumer> consumers) {
    this.consumers = consumers;
  }

  public static StaticConsumerInstanceProvider ofConsumers(Iterable<Consumer> consumers) {
    return new StaticConsumerInstanceProvider(consumers);
  }

  public static StaticConsumerInstanceProvider ofBeans(Iterable<Object> consumers) {
    return new StaticConsumerInstanceProvider(StreamSupport.stream(consumers.spliterator(), false)
      .map(StaticConsumerInstanceProvider::toConsumerByAnnotatedMethod)
      .flatMap(List::stream)
      .collect(toList()));
  }

  private static List<Consumer> toConsumerByAnnotatedMethod(Object object) {
    return getConsumersFrom(object).stream()
      .map(method -> Consumer.of(object, method))
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

  @Override
  public Iterable<Consumer> getConsumers() {
    return StreamSupport.stream(consumers.spliterator(), false).collect(toList());
  }
}
