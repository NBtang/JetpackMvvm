package me.laotang.carry.mvvm.dispatcher

interface Effect<A> {
    fun onEffect(action: A)
}