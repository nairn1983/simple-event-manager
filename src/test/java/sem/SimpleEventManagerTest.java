package sem;

import com.google.common.eventbus.Subscribe;
import org.junit.Test;
import sem.event.Event;
import sem.event.EventSubscriber;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SimpleEventManagerTest {

	@Test
	public void testCachedConstructorCreatesExecutorService() throws NoSuchFieldException, IllegalAccessException {
		testConstructorCreatesExecutorService(new SimpleEventManager());
	}

	@Test
	public void testFixedConstructorCreatesExecutorService() throws NoSuchFieldException, IllegalAccessException {
		testConstructorCreatesExecutorService(new SimpleEventManager(10));
	}

	private void testConstructorCreatesExecutorService(final SimpleEventManager eventManager) throws NoSuchFieldException, IllegalAccessException {
		assertThat(getExecutorService(eventManager), notNullValue());
	}

	private ExecutorService getExecutorService(final SimpleEventManager eventManager) {
		final ExecutorService executorService;
		try {
			final Field executorServiceField = SimpleEventManager.class.getDeclaredField("executorService");
			executorServiceField.setAccessible(true);
			executorService = (ExecutorService) executorServiceField.get(eventManager);

		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
		return executorService;
	}

	@Test
	public void testShutdownOfManagerShutsDownExecutorService() {
		testShutdownOfManagerShutsDownExecutorService(new SimpleEventManager(), ExecutorServiceCheck.SHUTDOWN);
	}

	@Test
	public void testShutdownOfManagerTerminatesExecutorService() {
		testShutdownOfManagerShutsDownExecutorService(new SimpleEventManager(), ExecutorServiceCheck.TERMINATED);
	}

	private void testShutdownOfManagerShutsDownExecutorService(final SimpleEventManager eventManager,
	                                                           final ExecutorServiceCheck executorServiceCheck) {
		eventManager.shutdown();
		waitUntilExecutorServiceCompletes(eventManager, executorServiceCheck);
	}

	private void waitUntilExecutorServiceCompletes(final SimpleEventManager eventManager, final ExecutorServiceCheck executorServiceCheck) {
		await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final ExecutorService executorService = getExecutorService(eventManager);
				final boolean result;
				switch (executorServiceCheck) {
					case SHUTDOWN:
						result = executorService.isShutdown();
						break;
					case TERMINATED:
						result = executorService.isTerminated();
						break;
					default:
						throw new IllegalStateException("Unhandled check type " + executorServiceCheck);
				}
				return result;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Set<Event> getPostedEvents(final SimpleEventManager eventManager) {
		final Set<Event> postedEvents;
		try {
			final Field postedEventsField = SimpleEventManager.class.getDeclaredField("postedEvents");
			postedEventsField.setAccessible(true);

			postedEvents = (Set<Event>) postedEventsField.get(eventManager);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
		return postedEvents;
	}

	@Test
	public void testPostingEventUpdatesPostedEventsSet() {
		testPostingEventUpdatesPostedEventsSet(new SimpleEventManager());
	}

	private void testPostingEventUpdatesPostedEventsSet(final SimpleEventManager eventManager) {
		final Set<Event> postedEvents = getPostedEvents(eventManager);
		assertThat(postedEvents, empty());

		final AtomicBoolean handlerCompleted = new AtomicBoolean(false);
		final EventSubscriber dummySubscriber = new EventSubscriber() {
			@Override
			public SimpleEventManager getEventManager() {
				return eventManager;
			}

			@Subscribe
			public void handleMockEvent(final MockEvent mockEvent) {
				await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return postedEvents.size() == 1;
					}
				});
				handlerCompleted.set(true);
			}
		};
		eventManager.register(dummySubscriber);

		final Event event = new MockEvent();
		eventManager.post(event);

		await().atMost(1, TimeUnit.MINUTES).until(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return handlerCompleted.get();
			}
		});
	}

	private enum ExecutorServiceCheck {
		SHUTDOWN, TERMINATED
	}

	private class MockEvent implements Event {
	}
}
