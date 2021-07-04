package me.laotang.carry.mvvm.store.core.middleware;

public interface Middleware<A,R> {

    R dispatch(Next<A,R> next, A action);

    interface Next<A,R> {
        R next(A action);
    }
}
