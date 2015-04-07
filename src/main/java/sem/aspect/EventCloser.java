package sem.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import sem.event.Event;
import sem.event.EventSubscriber;

@Aspect
public class EventCloser {

	@AfterReturning("execution(public void *.*(sem.event.Event)) && target(eventSubscriber) && args(event) " +
			"&& @annotation(com.google.common.eventbus.Subscribe)")
	public void notifyEventCompleted(@SuppressWarnings("unused") final JoinPoint thisJoinPoint, final EventSubscriber eventSubscriber, final Event event) {
		eventSubscriber.getEventManager().notifyEventCompleted(event);
	}
}
