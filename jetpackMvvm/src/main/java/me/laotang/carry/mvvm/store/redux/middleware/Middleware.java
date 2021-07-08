package me.laotang.carry.mvvm.store.redux.middleware;

public interface Middleware<A,R> {

    R dispatch(Next<A,R> next, A action);

    interface Next<A,R> {
        R next(A action);
    }
}
