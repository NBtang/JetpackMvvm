package me.laotang.carry.mvvm.domain

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.*
import me.jessyan.rxerrorhandler.core.RxErrorHandler
import me.jessyan.rxerrorhandler.handler.ErrorHandlerFactory
import me.laotang.carry.AppManager
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 根据业务抽离一个个Request，便于复用
 */
abstract class BaseRequest : Closeable {

    //全局异常回调
    protected lateinit var mHandlerFactory: ErrorHandlerFactory

    @EntryPoint
    @InstallIn(ApplicationComponent::class)
    interface BaseRequestEntryPoint {
        fun rxErrorHandler(): RxErrorHandler
    }

    private val scope: CoroutineScope

    init {
        initErrorHandler()
        val context: CoroutineContext = EmptyCoroutineContext
        val supervisorJob = SupervisorJob(context[Job])
        val coroutineContext = Dispatchers.Main.immediate + context + supervisorJob
        scope = CoroutineScope(coroutineContext)
    }


    private fun initErrorHandler() {
        val entryPoint = EntryPointAccessors.fromApplication(
            AppManager.instance.getApplicationContext(),
            BaseRequestEntryPoint::class.java
        )
        mHandlerFactory = entryPoint.rxErrorHandler().handlerFactory
    }

    //协程辅助
    protected fun launch(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit),
        enableHandleError: Boolean = true
    ) =
        scope.launch(CoroutineExceptionHandler { _, throwable ->
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
        scope.launch(CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException && enableHandleError) {
                mHandlerFactory.handleError(throwable)
            }
        }) {
            block()
        }

    override fun close() {
        scope.cancel()
    }
}
