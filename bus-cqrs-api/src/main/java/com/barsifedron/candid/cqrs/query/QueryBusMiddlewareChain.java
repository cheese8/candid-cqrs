package com.barsifedron.candid.cqrs.query;


import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Chain of responsibility pattern, aka, infinite interceptors... Will allow us
 * to dynamically create chains of middleware.
 */
public class QueryBusMiddlewareChain {

    private final QueryBusMiddleware middleware;
    private final QueryBusMiddlewareChain nextInChain;

    public QueryBusMiddlewareChain(QueryBusMiddleware middleware, QueryBusMiddlewareChain nextInChain) {
        this.middleware = middleware;
        this.nextInChain = nextInChain;
    }

    public <T> T dispatch(Query<T> query) {
        return middleware.dispatch(query, nextInChain);
    }


    <T extends QueryBusMiddleware> boolean containsInstanceOf(Class<T> middlewareClass) {
        if (middleware == null) {
            return false;
        }
        if (middleware.getClass() == middlewareClass) {
            return true;
        }
        if (nextInChain == null) {
            return false;
        }
        return nextInChain.containsInstanceOf(middlewareClass);
    }


    public static class Factory {

        public QueryBusMiddlewareChain chainOfMiddleware(QueryBusMiddleware... middlewares) {
            List<QueryBusMiddleware> list = Stream.of(middlewares).collect(toList());
            return chainOfMiddleware(list);
        }

        /**
         * From a list of middleware, creates a Chain, wrapping them recursively into each others.
         * The "last" middleware called should be the dispatcher and, contrarily to the others, will not forward the command put finally handle it to the wanted handlers.
         * Hence the last Chain built with "null". You are better than me and can probably find a more safe and elegant way to do this.
         */
        public QueryBusMiddlewareChain chainOfMiddleware(List<QueryBusMiddleware> middlewares) {
            validate(middlewares);
            if (middlewares.size() == 1) {
                return new QueryBusMiddlewareChain(middlewares.get(0), unreachableNextInChain());
            }
            return new QueryBusMiddlewareChain(
                    middlewares.get(0),
                    chainOfMiddleware(middlewares.subList(1, middlewares.size())));
        }

        private void validate(List<QueryBusMiddleware> middlewares) {
            if (middlewares == null) {
                throw new RuntimeException("Can not create a middleware chain from a null list of middlewares");
            }
            if (middlewares.isEmpty()) {
                throw new RuntimeException("Can not operate on an empty list of middlewares");
            }
            if (middlewares.stream().anyMatch(Objects::isNull)) {
                throw new RuntimeException("Can not accept a null middleware in the lists of middlewares");
            }
            QueryBusMiddleware lastMiddlewareInChain = middlewares.get(middlewares.size() - 1);
            if (!lastMiddlewareInChain.getClass().isAssignableFrom(QueryBusMiddleware.Dispatcher.class)) {
                throw new RuntimeException("The last middleware of the chain must always be the one dispatching to handlers.");
            }
        }

        private QueryBusMiddlewareChain unreachableNextInChain() {
            QueryBusMiddleware unreachableMiddleware = new QueryBusMiddleware() {
                @Override
                public <T> T dispatch(Query<T> query, QueryBusMiddlewareChain next) {
                    throw new IllegalArgumentException("This should never be called");
                }
            };
            return new QueryBusMiddlewareChain(unreachableMiddleware, null);
        }
    }


}
