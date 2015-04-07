package sem.event;

import com.google.common.eventbus.Subscribe;
import org.junit.Before;
import org.junit.Test;
import sem.SimpleEventManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventSubscriberTest {
	private SimpleEventManager eventManager;
	private MockEventSubscriber eventSubscriber;

	@Before
	public void setup() {
		eventManager = mock(SimpleEventManager.class);
		eventSubscriber = new MockEventSubscriber(eventManager);
	}

	@Test
	public void testEventHandlerNotifiesAfterCompletion() {
		final Event testEvent = new Event() {
		};

		eventSubscriber.handleTestEvent(testEvent);
		verify(eventManager).notifyEventCompleted(testEvent);
	}

	@Test
	public void testNonAnnotatdeMethodDoesNotNotify() {
		final Event testEvent = new Event() {
		};

		eventSubscriber.doNothing(testEvent);
		verify(eventManager, never()).notifyEventCompleted(any(Event.class));
	}

	@Test
	public void testHandlingImplementedEventNotifiesOnCompletion() {
		final MockEvent testEvent = new MockEvent();

		eventSubscriber.handleMockEvent(testEvent);
		verify(eventManager).notifyEventCompleted(testEvent);
	}

	private class MockEventSubscriber implements EventSubscriber {
		private SimpleEventManager eventManager;

		public MockEventSubscriber(final SimpleEventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public SimpleEventManager getEventManager() {
			return eventManager;
		}

		@Subscribe
		public void handleTestEvent(final Event event) {
		}

		public void doNothing(final Event event) {
		}

		@Subscribe
		public void handleMockEvent(final MockEvent event) {
		}
	}
}
