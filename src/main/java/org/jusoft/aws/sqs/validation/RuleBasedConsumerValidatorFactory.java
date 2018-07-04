package org.jusoft.aws.sqs.validation;

import java.util.HashSet;

import static org.apache.commons.lang3.Validate.notNull;

public class RuleBasedConsumerValidatorFactory implements ConsumerValidatorFactory {

  private final RulesProvider rulesProvider;

  public RuleBasedConsumerValidatorFactory(RulesProvider rulesProvider) {
    this.rulesProvider = rulesProvider;
    notNull(rulesProvider);
  }

  @Override
  public ConsumerValidator create() {
    return new RuleBasedConsumerValidator(new HashSet<>(rulesProvider.find()));
  }
}
