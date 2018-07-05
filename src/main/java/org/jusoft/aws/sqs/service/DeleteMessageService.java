package org.jusoft.aws.sqs.service;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.jusoft.aws.sqs.annotation.DeletePolicy;

import java.util.function.Function;

public interface DeleteMessageService {

  void deleteMessage(DeletePolicy deletePolicy,
                     ReceiveMessageResult receiveMessageResult,
                     String queueUrl,
                     Function<ReceiveMessageResult, Boolean> messageConsumer);
}
