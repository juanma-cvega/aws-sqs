package org.jusoft.aws.sqs.validation.rule.impl;

import org.jusoft.aws.sqs.Consumer;
import org.jusoft.aws.sqs.annotation.SqsConsumer;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public abstract class AbstractValidationRuleTest {

  static final String QUEUE_NAME = "queueName";

  Consumer getConsumerFrom(Object consumerInstance) {
    Method consumerMethod = Stream.of(consumerInstance.getClass().getDeclaredMethods())
      .filter(method -> method.getName().equals("testConsumer"))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Method name not valid"));
    return Consumer.of(consumerInstance, consumerMethod);
  }

  static class TestZeroArguments {

    @SqsConsumer(QUEUE_NAME)
    public void testConsumer() {

    }
  }
}
