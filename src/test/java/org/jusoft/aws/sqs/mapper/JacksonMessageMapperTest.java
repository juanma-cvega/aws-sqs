package org.jusoft.aws.sqs.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JacksonMessageMapperTest {

  private static final String INSTANCE_BODY = "anyBody";

  @Mock
  private ObjectMapper objectMapper;

  private JacksonMessageMapper mapper;

  @Before
  public void setup() {
    mapper = new JacksonMessageMapper(objectMapper);
  }

  @Test
  public void whenCreateMapperWithNullObjectMapperThenThereShouldBeAnExceptionThrown() {
    assertThatThrownBy(() -> new JacksonMessageMapper(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void whenDeserializeBodyThenObjectMapperShouldBeCalled() throws IOException {
    mapper.deserialize(INSTANCE_BODY, Object.class);

    verify(objectMapper).readValue(INSTANCE_BODY, Object.class);
  }

  @Test
  public void whenDeserializeBodyThrowsExceptionThenExceptionThrownShouldContainIt() throws IOException {
    IOException exceptionThrown = new IOException();
    when(objectMapper.readValue(INSTANCE_BODY, Object.class)).thenThrow(exceptionThrown);

    assertThatThrownBy(() -> mapper.deserialize(INSTANCE_BODY, Object.class))
      .isInstanceOf(IllegalArgumentException.class)
      .hasCause(exceptionThrown);
  }
}
