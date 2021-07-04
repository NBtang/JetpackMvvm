package me.laotang.carry.mvvm.dispatcher.thunk;

import me.laotang.carry.mvvm.dispatcher.Dispatcher;

public interface Thunk<A, R> {
    void run(Dispatcher<A, R> dispatcher);
}
