package org.jusoft.aws.sqs.validation.rule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RuleBasedConsumerValidatorFactoryTest {

  @Mock
  private RulesProvider rulesProvider;

  @InjectMocks
  private RuleBasedConsumerValidatorFactory factory;

  @Test
  public void whenCreateValidatorThenResultShouldUseRulesFromRulesProvider() {
    ValidationRule ruleOne = Mockito.mock(ValidationRule.class);
    ValidationRule ruleTwo = Mockito.mock(ValidationRule.class);
    Set<ValidationRule> rules = new HashSet<>(Arrays.asList(ruleOne, ruleTwo));
    when(rulesProvider.find()).thenReturn(rules);

    ConsumerValidator validator = factory.create();

    assertThat(validator).isInstanceOf(RuleBasedConsumerValidator.class);
    assertThat(((RuleBasedConsumerValidator) validator).getRules()).containsExactlyInAnyOrder(ruleOne, ruleTwo);
  }

  @Test
  public void whenFactoryCreatedWithNullRulesProviderThenAnExceptionShouldBeThrown() {
    assertThatThrownBy(() -> new RuleBasedConsumerValidator(null)).isInstanceOf(NullPointerException.class);
  }
}
