package com.barsifedron.candid.cqrs.springboot.sample.writeside.domainevent;

import com.barsifedron.candid.cqrs.domainevent.DomainEventHandler;
import com.barsifedron.candid.cqrs.springboot.sample.ThingsDoneCounter;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SomethingWasDoneEventHandler_2 implements DomainEventHandler<SomethingWasDoneEvent> {

    private final static Logger LOGGER = Logger.getLogger(SomethingWasDoneEventHandler_2.class.getName());

    @Override
    public void handle(SomethingWasDoneEvent event) {

        LOGGER.info("Received event : " + event.toString());
        LOGGER.info("Will process side effect:");
        LOGGER.info("Updating counter : ");

        new ThingsDoneCounter().increment();

    }

    @Override
    public Class<SomethingWasDoneEvent> listenTo() {
        return SomethingWasDoneEvent.class;
    }

}
