package org.jusoft.aws.sqs.executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SqsFixedExecutorFactoryTest {

  private final SqsFixedExecutorFactory sqsFixedExecutorFactory = new SqsFixedExecutorFactory();

  @Test
  public void shouldCreateExecutorBasedOnNumberOfConsumersByTheirConcurrentValue() throws NoSuchMethodException {
    Consumer firstConsumerTestClassOne = Consumer.of(new TestClassOne(), TestClassOne.class.getMethod("test"));
    Consumer secondConsumerTestClassOne = Consumer.of(new TestClassOne(), TestClassOne.class.getMethod("test2"));
    Consumer firstConsumerTestClassTwo = Consumer.of(new TestClassTwo(), TestClassTwo.class.getMethod("test3"));
    Consumer secondConsumerTestClassTwo = Consumer.of(new TestClassTwo(), TestClassTwo.class.getMethod("test4"));
    List<Consumer> consumers = Arrays.asList(firstConsumerTestClassOne, secondConsumerTestClassOne, firstConsumerTestClassTwo, secondConsumerTestClassTwo);

    ThreadPoolExecutor executor = (ThreadPoolExecutor) sqsFixedExecutorFactory.createFor(consumers);
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
