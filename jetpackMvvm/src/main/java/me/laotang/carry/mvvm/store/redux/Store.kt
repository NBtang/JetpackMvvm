package me.laotang.carry.mvvm.store.redux

interface Store<S> {
    fun getState(): S
    fun setState(state: S)
}