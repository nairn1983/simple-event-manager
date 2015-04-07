package sem.aspect;

import com.google.common.eventbus.Subscribe;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import sem.event.Event;
import sem.event.EventSubscriber;

@Aspect
public class EventCloser {
	private final XLogger logger = XLoggerFactory.getXLogger(getClass());

	@AfterReturning(value = "@annotation(subscribe) && target(eventSubscriber) && args(event) && execution(void *.*(*))", argNames = "joinPoint, subscribe, eventSubscriber, event")
	public void notifyEventCompleted(final JoinPoint joinPoint, final Subscribe subscribe, final EventSubscriber eventSubscriber, final Event event) {
		logger.debug("Notifying completion of event {}", event);
		eventSubscriber.getEventManager().notifyEventCompleted(event);
	}
}
