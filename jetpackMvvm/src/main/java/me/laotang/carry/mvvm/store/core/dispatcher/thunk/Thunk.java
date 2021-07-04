package me.laotang.carry.mvvm.store.core.dispatcher.thunk;

import me.laotang.carry.mvvm.store.core.dispatcher.Dispatcher;

public interface Thunk<A, R> {
    void run(Dispatcher<A, R> dispatcher);
}
