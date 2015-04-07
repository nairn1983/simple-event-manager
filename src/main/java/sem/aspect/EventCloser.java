package sem.aspect;

import com.google.common.eventbus.Subscribe;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import sem.event.Event;
import sem.event.EventSubscriber;

@Aspect
public class EventCloser {

	@AfterReturning(value = "@annotation(subscribe) && target(eventSubscriber) && args(event) && execution(void *.*(*))", argNames = "joinPoint, subscribe, eventSubscriber, event")
	public void notifyEventCompleted(final JoinPoint joinPoint, final Subscribe subscribe, final EventSubscriber eventSubscriber, final Event event) {
		System.out.println("notifyEventCompleted("+joinPoint+") called");
		eventSubscriber.getEventManager().notifyEventCompleted(event);
	}
}
