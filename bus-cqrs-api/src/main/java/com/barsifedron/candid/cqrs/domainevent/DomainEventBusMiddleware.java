package com.barsifedron.candid.cqrs.domainevent;

import com.barsifedron.candid.cqrs.domainevent.middleware.DomainEventBusDispatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The middleware intercept your event on its way to or back from the event listeners.
 * Think of this as a chain of decorators, each one adding its own behaviour to the process.
 * <p>
 * This is an extremely powerful way to add common behavior to all your events processing.
 * Simple examples of middleware:
 * A middleware persisting all the events generated by your app.
 * A middleware logging the execution time taken to process your events etc...
 */
public interface DomainEventBusMiddleware {

    void dispatch(DomainEvent event, DomainEventBus next);

    /**
     * Decorates a command bus with this middleware.
     */
    default DomainEventBus decorate(DomainEventBus bus) {
        DomainEventBusMiddleware thisMiddleware = this;
        DomainEventBus decoratedDomainEventBus = (command) -> thisMiddleware.dispatch(command, bus);
        return decoratedDomainEventBus;
    }

    /**
     * Decorates an existing middleware with this middleware.
     */
    default DomainEventBusMiddleware decorate(DomainEventBusMiddleware middleware) {
        DomainEventBusMiddleware thisMiddleware = this;
        DomainEventBusMiddleware decoratedDomainEventBusMiddleware = (command, next) -> thisMiddleware
                .dispatch(command, middleware.decorate(next));
        return decoratedDomainEventBusMiddleware;
    }

    static DomainEventBus chainManyIntoADomainEventBus(DomainEventBusMiddleware... middlewares) {
        return Chain.manyIntoADomainEventBus(Arrays.asList(middlewares));
    }

    static DomainEventBusMiddleware chainManyIntoADomainEventBusMiddleware(DomainEventBusMiddleware... middlewares) {
        return Chain.manyIntoADomainEventBusMiddleware(Arrays.asList(middlewares));
    }

    class Chain {

        /**
         * Creates a domain events bus from a list of middleware, wrapping them recursively into each others.
         * The "last" middleware called should always be the dispatcher and, contrarily to the others,
         * will not forward the command but finally handle it to the command handlers.
         * Hence the last Chain built with "null".
         */
        static DomainEventBus manyIntoADomainEventBus(List<DomainEventBusMiddleware> middlewares) {

            validateMiddlewares(middlewares);
            validateLastMiddlewareIsDispatcher(middlewares);
            DomainEventBusMiddleware compositeMiddleware = manyIntoADomainEventBusMiddleware(middlewares);
            return compositeMiddleware.decorate((DomainEventBus) null);
        }

        /**
         * Wraps a list of middleware into each other to create a composite one.
         */
        static DomainEventBusMiddleware manyIntoADomainEventBusMiddleware(List<DomainEventBusMiddleware> middlewares) {

            validateMiddlewares(middlewares);
            if (middlewares.size() == 1) {
                return middlewares.get(0);
            }
            return middlewares.get(0)
                    .decorate(manyIntoADomainEventBusMiddleware(middlewares.subList(1, middlewares.size())));

        }

        private static void validateLastMiddlewareIsDispatcher(List<DomainEventBusMiddleware> middlewares) {
            DomainEventBusMiddleware lastMiddlewareInChain = middlewares.get(middlewares.size() - 1);
            if (!lastMiddlewareInChain.getClass().isAssignableFrom(DomainEventBusDispatcher.class)) {
                throw new RuntimeException(
                        "The last middleware of the chain must always be the one dispatching to handlers.");
            }
        }

        private static void validateMiddlewares(List<DomainEventBusMiddleware> middlewares) {
            if (middlewares == null) {
                throw new RuntimeException("Can not create a middleware chain from a null list of middlewares");
            }
            if (middlewares.isEmpty()) {
                throw new RuntimeException("Can not operate on an empty list of middlewares");
            }
            if (middlewares.stream().anyMatch(Objects::isNull)) {
                throw new RuntimeException("Can not accept a null middleware in the lists of middlewares");
            }
        }
    }
}

