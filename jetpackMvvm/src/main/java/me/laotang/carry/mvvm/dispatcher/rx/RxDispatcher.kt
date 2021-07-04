package me.laotang.carry.mvvm.dispatcher.rx

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import me.laotang.carry.core.subscriber.RxSubscriber
import me.laotang.carry.mvvm.dispatcher.Action
import me.laotang.carry.mvvm.dispatcher.Dispatcher

class RxDispatcher<A : Action>(private val dispatcher: Dispatcher<Action, *>) :
    Dispatcher<Observable<A>, Unit>() {

    private val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun dispatch(action: Observable<A>) {
        val disposable = action.subscribeWith(object : RxSubscriber<A>() {
            override fun _onNext(t: A) {
                dispatcher.dispatch(t)
            }
        })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}