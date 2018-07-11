package org.jusoft.aws.sqs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jusoft.aws.sqs.annotation.SqsConsumer;
import org.jusoft.aws.sqs.executor.ExecutorFactory;
import org.jusoft.aws.sqs.fixture.TestFixtures.SingleParameterMethodClass;
import org.jusoft.aws.sqs.provider.ConsumersInstanceProvider;
import org.jusoft.aws.sqs.service.QueuePollService;
import org.jusoft.aws.sqs.validation.ConsumerValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jusoft.aws.sqs.annotation.SqsConsumer.DEFAULT_MAX_LONG_POLLING_IN_SECONDS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsDispatcherTest {

  @Mock
  private ConsumersInstanceProvider consumersInstanceProvider;
  @Mock
  private ConsumerValidator consumerValidator;
  @Mock
  private QueuePollService queuePollService;
  @Mock
  private ExecutorFactory executorFactory;
  @Spy
  private SyncExecutorService executorService;

  @InjectMocks
  private SqsDispatcher sqsDispatcher;

  @Test
  public void whenConsumerInstanceProviderReturnsConsumersThenTheyAreStartedToBeUsed() throws NoSuchMethodException {
    SingleParameterMethodClass testObjectOne = new SingleParameterMethodClass();
    QueueConsumer queueConsumerOne = QueueConsumer.of(testObjectOne, testObjectOne.getMethod());
    SingleParameterMethodClass testObjectTwo = new SingleParameterMethodClass();
    QueueConsumer queueConsumerTwo = QueueConsumer.of(testObjectTwo, testObjectTwo.getMethod());
    List<QueueConsumer> queueConsumers = asList(queueConsumerOne, queueConsumerTwo);
    List<SqsConsumer> annotations = asList(queueConsumerOne.getAnnotation(), queueConsumerTwo.getAnnotation());
    when(executorFactory.createFor(annotations)).thenReturn(executorService);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);

    sqsDispatcher.subscribeConsumers();

    verify(consumerValidator).isValid(queueConsumers);
    verify(queuePollService).start(queueConsumerOne);
    verify(queuePollService).start(queueConsumerTwo);
  }

  @Test
  public void whenConsumerInstanceProviderReturnsNoConsumersThenNothingIsStarted() {
    when(consumersInstanceProvider.getConsumers()).thenReturn(new ArrayList<>());

    sqsDispatcher.subscribeConsumers();

    verifyZeroInteractions(consumerValidator, queuePollService);
  }

  @Test
  public void whenCloseSqsDispatcherButNotExecutorThenConsumersShouldNotBeStopped() throws InterruptedException {
    sqsDispatcher.close();

    verify(queuePollService).stop();
    verifyZeroInteractions(executorService);
  }

  @Test
  public void whenCloseSqsDispatcherThenConsumersShouldBeStopped() throws InterruptedException, NoSuchMethodException {
    SingleParameterMethodClass testObject = new SingleParameterMethodClass();
    QueueConsumer queueConsumerOne = QueueConsumer.of(testObject, testObject.getMethod());
    List<QueueConsumer> queueConsumers = singletonList(queueConsumerOne);
    when(consumersInstanceProvider.getConsumers()).thenReturn(queueConsumers);
    List<SqsConsumer> annotations = singletonList(queueConsumerOne.getAnnotation());
    when(executorFactory.createFor(annotations)).thenReturn(executorService);
    sqsDispatcher.subscribeConsumers();

    sqsDispatcher.close();

    verify(queuePollService).stop();
    verify(executorService).awaitTermination(DEFAULT_MAX_LONG_POLLING_IN_SECONDS, SECONDS);
  }

  private static class SyncExecutorService implements ExecutorService {
    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
      return null;
    }

    @Override
    public boolean isShutdown() {
      return false;
    }

    @Override
    public boolean isTerminated() {
      return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
      return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
      task.run();
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return null;
    }

    @Override
    public void execute(Runnable command) {

    }
  }
}
