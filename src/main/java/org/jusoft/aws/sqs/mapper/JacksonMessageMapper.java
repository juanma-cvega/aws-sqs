package org.jusoft.aws.sqs.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;

public class JacksonMessageMapper implements MessageMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JacksonMessageMapper.class);

  private final ObjectMapper objectMapper;

  public JacksonMessageMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    notNull(objectMapper);
  }

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
