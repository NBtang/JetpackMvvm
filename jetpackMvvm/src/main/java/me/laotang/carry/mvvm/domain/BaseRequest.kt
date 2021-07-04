package me.laotang.carry.mvvm.domain

import androidx.lifecycle.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.*
import me.jessyan.rxerrorhandler.core.RxErrorHandler
import me.jessyan.rxerrorhandler.handler.ErrorHandlerFactory
import me.laotang.carry.AppManager

/**
 * 根据业务抽离一个个Request，便于复用
 * 内部维护一个LifecycleRegistry，注册后可与view层的生命周期响应
 * 方便RxJava，协程等生命周期管理
 */
abstract class BaseRequest : IRequest, DefaultLifecycleObserver, LifecycleOwner {

    private var mLifecycleRegistry: LifecycleRegistry? = null

    //全局异常回调
    protected lateinit var mHandlerFactory: ErrorHandlerFactory

    @EntryPoint
    @InstallIn(ApplicationComponent::class)
    interface BaseRequestEntryPoint {
        fun rxErrorHandler(): RxErrorHandler
    }

    init {
        initLifecycle()
        initErrorHandler()
    }

    private fun initLifecycle(): LifecycleRegistry {
        if (mLifecycleRegistry == null) {
            mLifecycleRegistry = LifecycleRegistry(this)
        }
        return mLifecycleRegistry!!
    }

    private fun initErrorHandler() {
        val entryPoint = EntryPointAccessors.fromApplication(
            AppManager.instance.getApplicationContext(),
            BaseRequestEntryPoint::class.java
        )
        mHandlerFactory = entryPoint.rxErrorHandler().handlerFactory
    }

    //生命周期相关
    override fun getLifecycle(): Lifecycle {
        return initLifecycle()
    }

    override fun onCreate(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        initLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        owner.lifecycle.removeObserver(this)
        mLifecycleRegistry = null
    }

    //协程辅助
    protected fun launch(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit),
        enableHandleError: Boolean = true
    ) =
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                if (enableHandleError) {
                    mHandlerFactory.handleError(throwable)
                }
                onError.invoke(throwable)
            }
        }) {
            block()
        }

    protected fun launch(
        enableHandleError: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) =
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException && enableHandleError) {
                mHandlerFactory.handleError(throwable)
            }
        }) {
            block()
        }
}
