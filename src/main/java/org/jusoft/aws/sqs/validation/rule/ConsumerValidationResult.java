package org.jusoft.aws.sqs.validation.rule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jusoft.aws.sqs.QueueConsumer;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Container that holds the result of a consumer method validation.
 *
 * @author Juan Manuel Carnicero Vega
 */
public class ConsumerValidationResult {

  private final boolean isValid;
  private final ErrorMessage errorMessage;
  private final QueueConsumer queueConsumer;

  private ConsumerValidationResult(boolean isValid, ErrorMessage errorMessage, QueueConsumer queueConsumer) {
    this.isValid = isValid;
    this.errorMessage = errorMessage;
    this.queueConsumer = queueConsumer;
    notNull(this.errorMessage);
    notNull(this.queueConsumer);
  }

  public static ConsumerValidationResult of(ErrorMessage message, QueueConsumer queueConsumer) {
    return of(!message.hasErrors(), message, queueConsumer);
  }

  public static ConsumerValidationResult of(boolean isValid, QueueConsumer queueConsumer, String message, Object... parameters) {
    return of(isValid, ErrorMessage.of(message, parameters), queueConsumer);
  }

  public static ConsumerValidationResult of(boolean isValid, ErrorMessage message, QueueConsumer queueConsumer) {
    return new ConsumerValidationResult(isValid, message, queueConsumer);
  }

  public boolean isValid() {
    return isValid;
  }

  public String getErrorMessage() {
    return errorMessage.getMessage();
  }

  public QueueConsumer getQueueConsumer() {
    return queueConsumer;
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
      .append(queueConsumer, that.queueConsumer)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(isValid)
      .append(errorMessage)
      .append(queueConsumer)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "ConsumerValidationResult{" +
      "isValid=" + isValid +
      ", errorMessage=" + errorMessage +
      ", queueConsumer=" + queueConsumer +
      '}';
  }
}
