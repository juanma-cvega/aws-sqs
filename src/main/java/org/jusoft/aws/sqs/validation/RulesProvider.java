package org.jusoft.aws.sqs.validation;

import org.jusoft.aws.sqs.ValidationRule;

import java.util.Set;

public interface RulesProvider {

  Set<ValidationRule> find();
}
