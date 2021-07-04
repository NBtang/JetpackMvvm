package me.laotang.carry.mvvm.store.core.effect

interface Effect<A> {
    fun onEffect(action: A)
}