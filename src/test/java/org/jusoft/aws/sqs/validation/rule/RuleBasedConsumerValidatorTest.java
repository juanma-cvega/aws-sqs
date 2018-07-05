package org.jusoft.aws.sqs.validation.rule;

import org.junit.After;
import org.junit.Test;
import org.jusoft.aws.sqs.Consumer;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jusoft.aws.sqs.validation.rule.ErrorMessage.noError;
import static org.jusoft.aws.sqs.validation.rule.RuleBasedConsumerValidator.VALIDATION_ERROR_MESSAGE;

public class RuleBasedConsumerValidatorTest {

  private static final TestConsumer CONSUMER_INSTANCE = new TestConsumer();
  private static final Method CONSUMER_METHOD = Stream.of(CONSUMER_INSTANCE.getClass().getDeclaredMethods())
    .filter(method -> method.getName().equals("incrementCounter"))
    .findFirst()
    .orElseThrow(() -> new IllegalArgumentException("Method name not valid"));
  private static final Consumer CONSUMER = Consumer.of(CONSUMER_INSTANCE, CONSUMER_METHOD);
  private static final Iterable<Consumer> CONSUMERS = singletonList(CONSUMER);
  private static final String ERROR_MESSAGE_VALUE = "Error";
  private static final String ERROR_MESSAGE_VALUE_2 = "Error2";
  private static final ErrorMessage ERROR_MESSAGE = ErrorMessage.of(ERROR_MESSAGE_VALUE);
  private static final ErrorMessage ERROR_MESSAGE_2 = ErrorMessage.of(ERROR_MESSAGE_VALUE_2);
  private static final ValidationRule IS_VALID_RULE = consumer -> {
    invokeConsumer(consumer);
    return ConsumerValidationResult.of(noError(), CONSUMER);
  };
  private static final ValidationRule IS_NOT_VALID_RULE = consumer -> {
    invokeConsumer(consumer);
    return ConsumerValidationResult.of(ERROR_MESSAGE, CONSUMER);
  };
  private static final ValidationRule IS_NOT_VALID_RULE_2 = consumer -> {
    invokeConsumer(consumer);
    return ConsumerValidationResult.of(ERROR_MESSAGE_2, CONSUMER);
  };

  private static void invokeConsumer(Consumer consumer) {
    try {
      consumer.getConsumerMethod().invoke(CONSUMER_INSTANCE);
    } catch (Exception e) {
      throw new IllegalArgumentException("Not able to run the test");
    }
  }

  private RuleBasedConsumerValidator validator;

  @After
  public void resetCounter() {
    CONSUMER_INSTANCE.resetCounter();
  }

  @Test
  public void whenAllRulesReturnValidThenTheResultShouldReturnValidAndEmptyMessage() {
    Set<ValidationRule> rules = new HashSet<>(singletonList(IS_VALID_RULE));
    validator = new RuleBasedConsumerValidator(rules);

    assertThatCode(() -> validator.isValid(CONSUMERS)).doesNotThrowAnyException();

    assertThat(CONSUMER_INSTANCE.counter).isEqualTo(1);
  }

  @Test
  public void whenOneRuleReturnInvalidThenTheResultShouldReturnInvalidAndAnErrorMessage() {
    Set<ValidationRule> rules = new HashSet<>(asList(IS_VALID_RULE, IS_NOT_VALID_RULE));
    validator = new RuleBasedConsumerValidator(rules);

    assertThatThrownBy(() -> validator.isValid(CONSUMERS))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(String.format(VALIDATION_ERROR_MESSAGE, ERROR_MESSAGE_VALUE));

    assertThat(CONSUMER_INSTANCE.counter).isEqualTo(2);
  }

  @Test
  public void whenSeveralRulesReturnInvalidThenTheResultShouldReturnInvalidAndAllErrorMessages() {
    Set<ValidationRule> rules = new HashSet<>(asList(IS_NOT_VALID_RULE, IS_NOT_VALID_RULE_2));
    validator = new RuleBasedConsumerValidator(rules);

    assertThatThrownBy(() -> validator.isValid(CONSUMERS))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(VALIDATION_ERROR_MESSAGE.replace("%s", EMPTY))
      .hasMessageContaining(ERROR_MESSAGE_VALUE)
      .hasMessageContaining(ERROR_MESSAGE_VALUE_2);

    assertThat(CONSUMER_INSTANCE.counter).isEqualTo(2);
  }

  @Test
  public void whenAddingRulesThenTheResultOfValidatingConsumersShouldReturnTheResultOfThatRule() {
    Set<ValidationRule> rules = new HashSet<>(singletonList(IS_VALID_RULE));

    validator = new RuleBasedConsumerValidator(rules);
    validator.addRule(IS_NOT_VALID_RULE);

    assertThatThrownBy(() -> validator.isValid(CONSUMERS))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(String.format(VALIDATION_ERROR_MESSAGE, ERROR_MESSAGE_VALUE));

    assertThat(CONSUMER_INSTANCE.counter).isEqualTo(2);
  }

  private static class TestConsumer {

    private int counter;

    public void incrementCounter() {
      counter++;
    }

    void resetCounter() {
      counter = 0;
    }
  }
}
