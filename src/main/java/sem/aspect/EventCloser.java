package sem.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import sem.event.Event;
import sem.event.EventSubscriber;

@Aspect
public class EventCloser {

	@After("execution(void sem.event.EventSubscriber.*(sem.event.Event)) && args(event) && this(eventSubscriber)")
	public void closeEvent(final EventSubscriber eventSubscriber, final Event event) {
		eventSubscriber.getEventManager().notifyEventCompleted(event);
	}
}
