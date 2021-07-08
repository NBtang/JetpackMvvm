package me.laotang.carry.mvvm.store.redux

interface Effect<A> {
    fun onEffect(action: A)
}
