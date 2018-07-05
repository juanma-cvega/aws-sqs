package org.jusoft.aws.sqs.executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FixedExecutorFactoryTest {

  private final FixedExecutorFactory sqsFixedExecutorFactory = new FixedExecutorFactory();

  @Test
  public void shouldCreateExecutorBasedOnNumberOfConsumersByTheirConcurrentValue() throws NoSuchMethodException {
    QueueConsumer firstQueueConsumerTestClassOne = QueueConsumer.of(new TestClassOne(), TestClassOne.class.getMethod("test"));
    QueueConsumer secondQueueConsumerTestClassOne = QueueConsumer.of(new TestClassOne(), TestClassOne.class.getMethod("test2"));
    QueueConsumer firstQueueConsumerTestClassTwo = QueueConsumer.of(new TestClassTwo(), TestClassTwo.class.getMethod("test3"));
    QueueConsumer secondQueueConsumerTestClassTwo = QueueConsumer.of(new TestClassTwo(), TestClassTwo.class.getMethod("test4"));
    List<QueueConsumer> queueConsumers = Arrays.asList(firstQueueConsumerTestClassOne, secondQueueConsumerTestClassOne, firstQueueConsumerTestClassTwo, secondQueueConsumerTestClassTwo);

    ThreadPoolExecutor executor = (ThreadPoolExecutor) sqsFixedExecutorFactory.createFor(queueConsumers);
    assertThat(executor.getCorePoolSize()).isEqualTo(8);
  }

  private static class TestClassOne {

    @SqsConsumer(value = "test", concurrentConsumers = 2)
    public void test() {

    }

    @SqsConsumer(value = "test2")
    public void test2() {

    }
  }

  private static class TestClassTwo {

    @SqsConsumer(value = "test3", concurrentConsumers = 5)
    public void test3() {

    }

    public void test4() {

    }
  }
}
