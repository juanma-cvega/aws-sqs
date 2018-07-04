package org.jusoft.aws.sqs.validation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.Validate.notNull;

public class ErrorMessage {

  private final Set<String> messages;

  private ErrorMessage() {
    this(new HashSet<>());
  }

  private ErrorMessage(String message) {
    this();
    notNull(message);
    messages.add(message);
  }

  private ErrorMessage(Set<String> messages) {
    this.messages = messages;
    notNull(this.messages);
  }

  public static ErrorMessage of(String message, Object... arguments) {
    notNull(message);
    notNull(arguments);
    return new ErrorMessage(String.format(message, arguments));
  }

  public static ErrorMessage of(Supplier<Boolean> condition, String message, Object... arguments) {
    notNull(condition);
    return condition.get()
      ? noError()
      : ErrorMessage.of(message, arguments);
  }

  public static ErrorMessage noError() {
    return new ErrorMessage();
  }

  public ErrorMessage addMessage(ErrorMessage errorMessage) {
    notNull(errorMessage);
    if (errorMessage.hasErrors()) {
      messages.addAll(errorMessage.messages);
    }
    return this;
  }

  public boolean hasErrors() {
    return !messages.isEmpty();
  }

  public String getMessage() {
    return messages.stream().collect(joining(lineSeparator()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ErrorMessage that = (ErrorMessage) o;

    return new EqualsBuilder()
      .append(messages, that.messages)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(messages)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "ErrorMessage{" +
      "messages='" + messages + '\'' +
      '}';
  }
}
