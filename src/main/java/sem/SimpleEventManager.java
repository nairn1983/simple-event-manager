package sem;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import sem.event.Event;
import sem.event.EventSubscriber;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleEventManager {
	private final EventBus eventBus;
	private final ExecutorService executorService;
	private final Set<Event> postedEvents = Sets.newHashSet();
	private final Multiset<Class<?>> eventSizeSet = HashMultiset.create();
	private boolean shutdownCalled = false;

	public SimpleEventManager() {
		this.executorService = Executors.newCachedThreadPool();
		this.eventBus = new AsyncEventBus(executorService);
	}

	public SimpleEventManager(final int numberOfThreads) {
		this.executorService = Executors.newFixedThreadPool(numberOfThreads);
		this.eventBus = new AsyncEventBus(executorService);
	}

	public synchronized void register(final EventSubscriber o) {
		eventBus.register(o);
		for(final Method method : o.getClass().getMethods()) {
			updateEventSizeSetFromMethod(method, EventSizeAction.ADD);
		}
	}

	public synchronized void unregister(final EventSubscriber o) {
		eventBus.unregister(o);
		for(final Method method : o.getClass().getMethods()) {
			updateEventSizeSetFromMethod(method, EventSizeAction.REMOVE);
		}
	}

	private void updateEventSizeSetFromMethod(final Method method, final EventSizeAction action) {
		if(method.getAnnotation(Subscribe.class) != null) {
			final List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
			synchronized (eventSizeSet) {
				switch(action) {
					case ADD:
						eventSizeSet.addAll(parameterTypes);
						break;
					case REMOVE:
						Multisets.removeOccurrences(eventSizeSet, parameterTypes);
						break;
					default:
						throw new UnsupportedOperationException("Unknown event size action " + action);
				}
			}
		}
	}

	public void post(final Event event) {
		postedEvents.add(event);
		eventBus.post(event);
	}

	public synchronized void shutdown() {
		shutdownCalled = true;
		attemptToShutdownEventBus();
	}

	private void attemptToShutdownEventBus() {
		if (postedEvents.isEmpty()) {
			executorService.shutdown();
		}
	}

	public void notifyEventCompleted(final Event event) {
		postedEvents.remove(event);
		if (shutdownCalled) {
			attemptToShutdownEventBus();
		}
	}

	private enum EventSizeAction {
		ADD, REMOVE
	}
}
