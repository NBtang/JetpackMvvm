package me.laotang.carry.mvvm.store.core.dispatcher

interface IDispatcher<A, R> {
    fun dispatch(action: A): R
    fun onCleared()
}