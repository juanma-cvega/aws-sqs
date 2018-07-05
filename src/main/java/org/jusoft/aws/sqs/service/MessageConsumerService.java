package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.annotation.DeletePolicy;

import java.util.function.Consumer;

public interface MessageConsumerService {

  void consumeAndDeleteMessage(DeletePolicy deletePolicy,
                               ReceiveMessageResult receiveMessageResult,
                               String queueUrl,
                               Consumer<ReceiveMessageResult> messageConsumer);
}
