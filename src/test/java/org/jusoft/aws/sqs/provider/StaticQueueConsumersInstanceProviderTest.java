package org.jusoft.aws.sqs.provider;

import org.junit.Test;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class StaticQueueConsumersInstanceProviderTest {

  private static final String QUEUE_NAME = "queueName";

  @Test
  public void whenProviderCreatedWithConsumersThenProviderShouldReturnSameConsumers() throws NoSuchMethodException {
    QueueConsumer queueConsumerOne = QueueConsumer.of(new TestConsumerOne(), getConsumerFrom(TestConsumerOne.class));
    TestConsumerTwo testConsumer = new TestConsumerTwo();
    QueueConsumer queueConsumerTwo = QueueConsumer.of(testConsumer, getConsumerFrom(TestConsumerTwo.class));
    QueueConsumer queueConsumerThree = QueueConsumer.of(testConsumer, getConsumerFrom(TestConsumerTwo.class, "testConsumerTwo"));

    ConsumersInstanceProvider provider = StaticConsumersInstanceProvider.ofConsumers(asList(queueConsumerOne, queueConsumerTwo, queueConsumerThree));

    assertThat(provider.getConsumers()).containsExactlyInAnyOrder(queueConsumerOne, queueConsumerTwo, queueConsumerThree);
  }

  @Test
  public void whenProviderCreatedWithInstancesThenProviderShouldReturnConsumersFromInstances() throws NoSuchMethodException {
    TestConsumerOne testConsumerOne = new TestConsumerOne();
    TestConsumerTwo testConsumerTwo = new TestConsumerTwo();

    ConsumersInstanceProvider provider = StaticConsumersInstanceProvider.ofBeans(asList(testConsumerOne, testConsumerTwo));

    QueueConsumer queueConsumerOne = QueueConsumer.of(testConsumerOne, getConsumerFrom(TestConsumerOne.class));
    QueueConsumer queueConsumerTwo = QueueConsumer.of(testConsumerTwo, getConsumerFrom(TestConsumerTwo.class));
    QueueConsumer queueConsumerThree = QueueConsumer.of(testConsumerTwo, getConsumerFrom(TestConsumerTwo.class, "testConsumerTwo"));
    assertThat(provider.getConsumers()).containsExactlyInAnyOrder(queueConsumerOne, queueConsumerTwo, queueConsumerThree);
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
