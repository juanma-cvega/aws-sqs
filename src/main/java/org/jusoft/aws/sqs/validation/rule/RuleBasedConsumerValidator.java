package org.jusoft.aws.sqs.validation.rule;

import org.apache.commons.lang3.Validate;
import org.jusoft.aws.sqs.QueueConsumer;
import org.jusoft.aws.sqs.validation.ConsumerValidator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * Implementation of the {@link ConsumerValidator} that uses a {@link Set} of {@link ValidationRule}s to validate that
 * consumers comply with the requirements. {@see ConsumerValidator}
 *
 * @author Juan Manuel Carnicero Vega
 */
public class RuleBasedConsumerValidator implements ConsumerValidator {

  static final String VALIDATION_ERROR_MESSAGE = "Some consumers are not well defined: errorMessages=%s";

  private final Set<ValidationRule> rules;

  public RuleBasedConsumerValidator(Set<ValidationRule> rules) {
    Validate.notNull(rules);
    this.rules = rules;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void isValid(Iterable<QueueConsumer> consumers) {
    Set<ConsumerValidationResult> validationResults = stream(consumers.spliterator(), false)
      .map(this::isValid)
      .flatMap(Collection::stream)
      .collect(toSet());
    if (isAnyConsumerNotWellDefined(validationResults)) {
      throw new IllegalArgumentException(String.format(VALIDATION_ERROR_MESSAGE, joinErrorMessagesFrom(validationResults)));
    }
  }

  private Set<ConsumerValidationResult> isValid(QueueConsumer queueConsumer) {
    return rules.stream()
      .map(rule -> rule.validate(queueConsumer))
      .collect(Collectors.toSet());
  }

  private String joinErrorMessagesFrom(Set<ConsumerValidationResult> results) {
    return results.stream()
      .filter(result -> !result.isValid())
      .map(ConsumerValidationResult::getErrorMessage)
      .collect(joining(lineSeparator()));
  }

  private boolean isAnyConsumerNotWellDefined(Set<ConsumerValidationResult> results) {
    return results.stream().anyMatch(validationResult -> !validationResult.isValid());
  }

  public void addRule(ValidationRule rule) {
    rules.add(rule);
  }

  public Set<ValidationRule> getRules() {
    return new HashSet<>(rules);
  }
}
