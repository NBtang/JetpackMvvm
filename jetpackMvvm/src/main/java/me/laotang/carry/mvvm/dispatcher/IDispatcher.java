package me.laotang.carry.mvvm.dispatcher;

public interface IDispatcher<A, R> {
    R dispatch(A action);

    void onCleared();
}
