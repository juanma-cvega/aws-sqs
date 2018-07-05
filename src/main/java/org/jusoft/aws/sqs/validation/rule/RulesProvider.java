package org.jusoft.aws.sqs.validation.rule;

import java.util.Set;

public interface RulesProvider {

  Set<ValidationRule> find();
}
