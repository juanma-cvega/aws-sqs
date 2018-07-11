package org.jusoft.aws.sqs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter to be mapped to the body content of the messages fetched. There are three options available
 * while defining the parameter used to hold the deserialized form of the body:
 * <ul>
 * <li>
 * The body is the only parameter of the consumer. In this case, the @{@link SqsBody} annotation can be omitted
 * as it's understood the parameter is the body of the message.
 * </li>
 * <li>
 * The consumer expects several parameters. In this case, the parameter expected to be mapped to the body of the
 * message <b>must</b> be annotated with the {@link SqsBody} annotation and it's mandatory that <b>one</b>
 * parameter contains it. All the other parameters should be annotated with {@link SqsAttribute},
 * otherwise they will initilised to null. The type of the body could be anything as long as the
 * {@link org.jusoft.aws.sqs.mapper.MessageMapper} can use the body to instantiate a new instance.
 * </li>
 * <li>
 * The only consumer parameter is of type {@link com.amazonaws.services.sqs.model.ReceiveMessageRequest}. In this
 * case, the whole message is passed to the consumer via the parameter and it's up to the developer to decide
 * how to handle its content.
 * </li>
 * </ul>
 *
 * @author Juan Manuel Carnicero Vega
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqsBody {
}
