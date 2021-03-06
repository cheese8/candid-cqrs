package com.barsifedron.candid.cqrs.domainevent;

import com.barsifedron.candid.cqrs.domainevent.middleware.DomainEventBusDispatcher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DomainEventBusMiddlewareTest {

    @Test
    public void canDecorateADomainEventBusOrADomainEventMiddleware() {

        List<String> logs = new ArrayList<>();

        DomainEventBusMiddleware firstMiddleware = new DomainEventBusMiddleware() {
            @Override
            public void dispatch(DomainEvent domainEvent, DomainEventBus bus) {
                logs.add("First middleware");
                bus.dispatch(domainEvent);
                logs.add("First middleware");
            }
        };

        DomainEventBusMiddleware secondMiddleware = new DomainEventBusMiddleware() {
            @Override
            public void dispatch(DomainEvent domainEvent, DomainEventBus bus) {
                logs.add("\tSecond middleware");
                bus.dispatch(domainEvent);
                logs.add("\tSecond middleware");
            }
        };

        DomainEventBus baseBus = new DomainEventBus() {
            @Override
            public void dispatch(DomainEvent domainEvent) {
                logs.add("\t\tDecorated bus execution.");
                new DomainEventBusDispatcher(new NothingToDoDomainEventHandler()).dispatch(domainEvent, null);
            }
        };

        DomainEventBus domainEventBus = firstMiddleware.decorate(secondMiddleware.decorate(baseBus));
        DomainEventBus secondDomainEventBus = (firstMiddleware.decorate(secondMiddleware)).decorate(baseBus);

        domainEventBus.dispatch(new NothingToDoEvent());
        assertEquals(
                "First middleware\n" +
                        "\tSecond middleware\n" +
                        "\t\tDecorated bus execution.\n" +
                        "\tSecond middleware\n" +
                        "First middleware",
                logs.stream().collect(Collectors.joining("\n")));

        logs.clear();

        secondDomainEventBus.dispatch(new NothingToDoEvent());
        assertEquals(
                "First middleware\n" +
                        "\tSecond middleware\n" +
                        "\t\tDecorated bus execution.\n" +
                        "\tSecond middleware\n" +
                        "First middleware",
                logs.stream().collect(Collectors.joining("\n")));
    }

    @Test
    public void shouldFailToConstructEmptyMiddlewareChain() {
        assertThrows(
                RuntimeException.class,
                () -> DomainEventBusMiddleware.chainManyIntoADomainEventBus());
    }

    @Test
    public void shouldFailINoDispatcherMiddleware() {
        assertThrows(
                RuntimeException.class,
                () -> DomainEventBusMiddleware.chainManyIntoADomainEventBus(new FirstTestMiddleware()));
    }

    @Test
    public void shouldFailIfLastMiddlewareInChainIsNotTheDispatcher() {
        assertThrows(
                RuntimeException.class,
                () -> {
                    DomainEventBusMiddleware.chainManyIntoADomainEventBus(
                            new FirstTestMiddleware(),
                            new DomainEventBusDispatcher(new HashSet<>()),
                            new SecondTestMiddleware()
                    );
                });
    }

    @Test
    public void shouldFailToBuildAChainOfMiddlewareIfOneIsNull() {
        assertThrows(
                RuntimeException.class,
                () -> {
                    DomainEventBusMiddleware.chainManyIntoADomainEventBus(
                            new FirstTestMiddleware(),
                            new SecondTestMiddleware(),
                            null,
                            new DomainEventBusDispatcher(new HashSet<>()));
                });
    }

    @Test
    public void shouldProcessDomainEventsWhenRightHandler() {
        Set<NothingToDoDomainEventHandler> handlers = Stream.of(new NothingToDoDomainEventHandler()).collect(toSet());
        DomainEventBus chain = DomainEventBusMiddleware.chainManyIntoADomainEventBus(
                new FirstTestMiddleware(),
                new SecondTestMiddleware(),
                new DomainEventBusDispatcher(handlers));
        chain.dispatch(new NothingToDoEvent());
    }

    static class FirstTestMiddleware implements DomainEventBusMiddleware {

        private final static Logger LOGGER = Logger.getLogger(FirstTestMiddleware.class.getName());

        @Override
        public void dispatch(DomainEvent domainEvent, DomainEventBus bus) {
            LOGGER.info("FirstTestMiddleware : dispatching");
            bus.dispatch(domainEvent);
            LOGGER.info("FirstTestMiddleware : dispatched");
        }
    }

    static class SecondTestMiddleware implements DomainEventBusMiddleware {

        private final static Logger LOGGER = Logger.getLogger(SecondTestMiddleware.class.getName());

        @Override
        public void dispatch(DomainEvent event, DomainEventBus bus) {
            LOGGER.info("SecondTestMiddleware : dispatching");
            bus.dispatch(event);
            LOGGER.info("SecondTestMiddleware : dispatched");
        }
    }

    static class NothingToDoEvent implements DomainEvent {
    }

    static class NothingToDoDomainEventHandler implements DomainEventHandler<NothingToDoEvent> {

        private final static Logger LOGGER = Logger.getLogger(NothingToDoDomainEventHandler.class.getName());

        @Override
        public void handle(NothingToDoEvent query) {
            LOGGER.info("I do nothing.");
        }

        @Override
        public Class listenTo() {
            return NothingToDoEvent.class;
        }
    }
}