package org.jusoft.aws.sqs.provider;

import org.junit.Test;
import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotations.SqsConsumer;

import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class StaticConsumerInstanceProviderTest {

  private static final String QUEUE_NAME = "queueName";

  @Test
  public void whenProviderCreatedWithConsumersThenProviderShouldReturnSameConsumers() throws NoSuchMethodException {
    Consumer consumerOne = Consumer.of(new TestConsumerOne(), getConsumerFrom(TestConsumerOne.class));
    TestConsumerTwo testConsumer = new TestConsumerTwo();
    Consumer consumerTwo = Consumer.of(testConsumer, getConsumerFrom(TestConsumerTwo.class));
    Consumer consumerThree = Consumer.of(testConsumer, getConsumerFrom(TestConsumerTwo.class, "testConsumerTwo"));

    ConsumerInstanceProvider provider = StaticConsumerInstanceProvider.ofConsumers(asList(consumerOne, consumerTwo, consumerThree));

    assertThat(provider.getConsumers()).containsExactlyInAnyOrder(consumerOne, consumerTwo, consumerThree);
  }

  @Test
  public void whenProviderCreatedWithInstancesThenProviderShouldReturnConsumersFromInstances() throws NoSuchMethodException {
    TestConsumerOne testConsumerOne = new TestConsumerOne();
    TestConsumerTwo testConsumerTwo = new TestConsumerTwo();

    ConsumerInstanceProvider provider = StaticConsumerInstanceProvider.ofBeans(asList(testConsumerOne, testConsumerTwo));

    Consumer consumerOne = Consumer.of(testConsumerOne, getConsumerFrom(TestConsumerOne.class));
    Consumer consumerTwo = Consumer.of(testConsumerTwo, getConsumerFrom(TestConsumerTwo.class));
    Consumer consumerThree = Consumer.of(testConsumerTwo, getConsumerFrom(TestConsumerTwo.class, "testConsumerTwo"));
    assertThat(provider.getConsumers()).containsExactlyInAnyOrder(consumerOne, consumerTwo, consumerThree);
  }

  private Method getConsumerFrom(Class<?> clazz) throws NoSuchMethodException {
    return getConsumerFrom(clazz, "testConsumer");
  }

  private Method getConsumerFrom(Class<?> clazz, String methodName) throws NoSuchMethodException {
    return clazz.getMethod(methodName);
  }

  private static class TestConsumerOne {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer() {

    }
  }

  private static class TestConsumerTwo {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer() {

    }

    @SqsConsumer(QUEUE_NAME)
    public void testConsumerTwo() {

    }
  }

  private static class TestNoConsumer {

    public void testConsumer() {

    }
  }
}
