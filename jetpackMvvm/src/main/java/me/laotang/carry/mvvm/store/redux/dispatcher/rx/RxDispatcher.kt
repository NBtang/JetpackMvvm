package me.laotang.carry.mvvm.store.redux.dispatcher.rx

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import me.laotang.carry.core.subscriber.RxSubscriber
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.IDispatcher

class RxDispatcher<A>(private val dispatcher: IDispatcher<A, *>) :
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

val <A, R> IDispatcher<A, R>.asRxDispatcher: RxDispatcher<A>
    get() {
        return RxDispatcher(this)
    }