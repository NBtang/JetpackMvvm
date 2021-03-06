package me.laotang.carry.mvvm.store.redux.dispatcher;

import java.util.Arrays;
import java.util.Iterator;

import me.laotang.carry.mvvm.store.redux.Effect;
import me.laotang.carry.mvvm.store.redux.middleware.Middleware;

public abstract class Dispatcher<A, R> implements IDispatcher<A, R> {

    public static <T extends Effect<A>, A> Dispatcher<A, A> create(final T effect) {
        return new Dispatcher<A, A>() {

            @Override
            public A dispatch(A action) {
                effect.onEffect(action);
                return action;
            }
        };
    }

    @SafeVarargs
    public static <T extends Effect<A>, A> Dispatcher<A, A> create(final T effect, final IDispatcher<A, A>... dispatcher) {
        if (dispatcher == null) {
            return create(effect);
        }
        final Middleware<A, A> middleware = new Middleware<A, A>() {
            @Override
            public A dispatch(Next<A, A> next, A action) {
                for (IDispatcher<A, A> iDispatcher : dispatcher) {
                    iDispatcher.dispatch(action);
                }
                return next.next(action);
            }

            @Override
            public void onCleared() {

            }
        };
        return new Dispatcher<A, A>() {
            @Override
            public A dispatch(A action) {
                effect.onEffect(action);
                return action;
            }
        }.chain(middleware);
    }

    public final Dispatcher<A, R> chain(final Middleware<A, R> middleware) {
        if (middleware == null) {
            throw new NullPointerException("middleware==null");
        }
        return new Dispatcher<A, R>() {
            @Override
            public R dispatch(A action) {
                return middleware.dispatch(new Middleware.Next<A, R>() {
                    @Override
                    public R next(A action) {
                        return Dispatcher.this.dispatch(action);
                    }
                }, action);
            }

            @Override
            public void onCleared() {
                middleware.onCleared();
                super.onCleared();
            }
        };
    }

    public final Dispatcher<A, R> chain(Iterable<Middleware<A, R>> middleware) {
        return chain(middleware.iterator());
    }

    public final Dispatcher<A, R> chain(Middleware<A, R>... middleware) {
        return chain(Arrays.asList(middleware));
    }

    private Dispatcher<A, R> chain(Iterator<Middleware<A, R>> itr) {
        if (!itr.hasNext()) {
            return this;
        }
        return chain(itr.next()).chain(itr);
    }

    @Override
    public void onCleared() {
    }
}
