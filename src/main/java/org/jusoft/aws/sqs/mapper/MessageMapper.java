package org.jusoft.aws.sqs.mapper;

public interface MessageMapper {

  <T> T deserialize(String body, Class<T> objectType);
}
