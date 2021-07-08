package me.laotang.carry.mvvm.store.redux.dispatcher.thunk;

import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher;

public interface Thunk<A, R> {
    void run(Dispatcher<A, R> dispatcher);
}
