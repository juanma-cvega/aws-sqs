package org.jusoft.aws.sqs.validation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jusoft.aws.sqs.Consumer;

import static org.apache.commons.lang3.Validate.notNull;

public class ConsumerValidationResult {

  private final boolean isValid;
  private final ErrorMessage errorMessage;
  private final Consumer consumer;

  private ConsumerValidationResult(boolean isValid, ErrorMessage errorMessage, Consumer consumer) {
    this.isValid = isValid;
    this.errorMessage = errorMessage;
    this.consumer = consumer;
    notNull(this.errorMessage);
    notNull(this.consumer);
  }

  public static ConsumerValidationResult of(ErrorMessage message, Consumer consumer) {
    return of(!message.hasErrors(), message, consumer);
  }

  public static ConsumerValidationResult of(boolean isValid, Consumer consumer, String message, Object... parameters) {
    return of(isValid, ErrorMessage.of(message, parameters), consumer);
  }

  public static ConsumerValidationResult of(boolean isValid, ErrorMessage message, Consumer consumer) {
    return new ConsumerValidationResult(isValid, message, consumer);
  }

  public boolean isValid() {
    return isValid;
  }

  public String getErrorMessage() {
    return errorMessage.getMessage();
  }

  public Consumer getConsumer() {
    return consumer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConsumerValidationResult that = (ConsumerValidationResult) o;

    return new EqualsBuilder()
      .append(isValid, that.isValid)
      .append(errorMessage, that.errorMessage)
      .append(consumer, that.consumer)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(isValid)
      .append(errorMessage)
      .append(consumer)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "ConsumerValidationResult{" +
      "isValid=" + isValid +
      ", errorMessage=" + errorMessage +
      ", consumer=" + consumer +
      '}';
  }
}
