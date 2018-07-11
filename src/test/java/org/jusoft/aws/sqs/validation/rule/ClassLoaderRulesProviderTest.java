package org.jusoft.aws.sqs.validation.rule;

import org.junit.Test;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.rule.test.TestValidationRule;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class ClassLoaderRulesProviderTest {

  private static final int CURRENT_DEFAULT_VALIDATION_RULES = 10;

  private ClassLoaderRulesProvider provider;

  @Test
  public void whenPackageContainsValidationRulesThenResultShouldContainOnlyPublicNotAbstractInstances() {
    provider = new ClassLoaderRulesProvider(getClass().getPackage().getName());

    Set<ValidationRule> validationRules = provider.find();

    assertThat(validationRules).extracting("class")
      .containsExactlyInAnyOrder(TestValidationRuleOne.class, TestValidationRuleTwo.class);
  }

  @Test
  public void whenPackageDoesNotContainValidationRulesThenResultShouldNotContainAnyInstance() {
    provider = new ClassLoaderRulesProvider(EMPTY);

    Set<ValidationRule> validationRules = provider.find();

    assertThat(validationRules).isEmpty();
  }

  @Test
  public void whenPackageIsDefaultThenResultShouldContainAllDefaultValidationRules() {
    provider = new ClassLoaderRulesProvider();

    Set<ValidationRule> validationRules = provider.find();

    assertThat(validationRules).hasSize(CURRENT_DEFAULT_VALIDATION_RULES);
  }

  @Test
  public void whenPackageIsDifferentFromCurrentClassThenResultShouldContainValidationRules() {
    provider = new ClassLoaderRulesProvider("org.jusoft.aws.sqs.validation.rule.test");

    Set<ValidationRule> validationRules = provider.find();

    assertThat(validationRules).extracting("class")
      .containsExactlyInAnyOrder(TestValidationRule.class);
  }

  public static class TestValidationRuleOne implements ValidationRule {

    @Override
    public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
      return null;
    }
  }

  public static class TestValidationRuleTwo implements ValidationRule {

    @Override
    public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
      return null;
    }
  }

  public static abstract class TestAbstractValidation implements ValidationRule {

  }

  public static class TestNotValidationRule {

  }

  private static class TestPrivateValidation implements ValidationRule {

    @Override
    public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
      return null;
    }
  }

  protected static class TestProtectedValidation implements ValidationRule {

    @Override
    public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
      return null;
    }
  }

  static class TestPackageValidation implements ValidationRule {

    @Override
    public ConsumerValidationResult validate(QueueConsumer queueConsumer) {
      return null;
    }
  }
}
