package org.jusoft.aws.sqs.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Uses an {@link ObjectMapper} to deserialise the body of the AWS SQS messages.
 */
public class JacksonMessageMapper implements MessageMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JacksonMessageMapper.class);

  private final ObjectMapper objectMapper;

  public JacksonMessageMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    notNull(objectMapper);
  }

  /**
   * Deserialises the message body into an instance of the type specified.
   *
   * @param body       AWS SQS message body.
   * @param objectType type of the object to create from the body.
   * @throws IllegalArgumentException wraps any exception caused by the deserialization process.
   */
  @Override
  public <T> T deserialize(String body, Class<T> objectType) {
    try {
      return objectMapper.readValue(body, objectType);
    } catch (IOException e) {
      LOGGER.error("Unable to deserialize object: body={}", body);
      throw new IllegalArgumentException("Unable to deserialize object", e);
    }
  }
}
