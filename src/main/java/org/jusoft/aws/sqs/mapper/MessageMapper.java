package org.jusoft.aws.sqs.mapper;

/**
 * Deserialises a message body from an AWS SQS queue into an instance of the type specified.
 */
public interface MessageMapper {

  /**
   * Deserialises body into an instance of the class specified.
   *
   * @param body       contains the representation of the object.
   * @param objectType type of the object to create.
   */
  <T> T deserialize(String body, Class<T> objectType);
}
