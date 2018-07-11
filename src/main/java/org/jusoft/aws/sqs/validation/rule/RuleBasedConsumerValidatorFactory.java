package org.jusoft.aws.sqs.validation.rule;

import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.jusoft.aws.sqs.validation.ConsumerValidatorFactory;

import java.util.HashSet;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Creates a {@link RuleBasedConsumerValidator} using a {@link RulesProvider} to fetch all the rules available.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class RuleBasedConsumerValidatorFactory implements ConsumerValidatorFactory {

  private final RulesProvider rulesProvider;

  public RuleBasedConsumerValidatorFactory(RulesProvider rulesProvider) {
    this.rulesProvider = rulesProvider;
    notNull(rulesProvider);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConsumerValidator create() {
    return new RuleBasedConsumerValidator(new HashSet<>(rulesProvider.find()));
  }
}
