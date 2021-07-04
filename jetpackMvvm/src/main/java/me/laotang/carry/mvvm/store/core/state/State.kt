package me.laotang.carry.mvvm.store.core.state

interface State<S> {
    fun setState(state: S)
}