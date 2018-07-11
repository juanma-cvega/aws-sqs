package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.jusoft.aws.sqs.fixture.TestFixtures.QUEUE_NAME;

public abstract class AbstractValidationRuleTest {

  QueueConsumer getConsumerFrom(Object consumerInstance) {
    Method consumerMethod = Stream.of(consumerInstance.getClass().getDeclaredMethods())
      .filter(method -> method.getName().equals("testConsumer"))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Method name not valid"));
    return QueueConsumer.of(consumerInstance, consumerMethod);
  }

  static class TestZeroArguments {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer() {

    }
  }
}
