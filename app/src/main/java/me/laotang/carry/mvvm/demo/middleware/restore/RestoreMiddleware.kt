package me.laotang.carry.mvvm.demo.middleware.restore

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import me.laotang.carry.core.json.JsonConverter
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.redux.Store
import me.laotang.carry.mvvm.store.redux.middleware.Middleware
import me.laotang.carry.util.globalEntryPoint

/**
 * 记录store最后最新的state，帮助app恢复
 * 使用场景，app在后台是被杀，用户重新打开app时，app恢复到被杀前的activity以及之前所有的栈
 * 每一个activity的UI都可以使用记录的state恢复到之前的状态
 * 所以尽可能的保证数据驱动UI
 */
class RestoreMiddleware<S>(private val store: Store<S>, val tag: String) :
    Middleware<Action, Action> {

    internal var lastState: S? = null

    init {
        Restore.getInstance().addRestoreMiddleware(this)
    }

    override fun dispatch(next: Middleware.Next<Action, Action>, action: Action): Action {
        val r = next.next(action)
        lastState = store.getState()
        return r
    }

    override fun onCleared() {
        Restore.getInstance().removeRestoreMiddleware(this)
        lastState = null
    }
}

class Restore {

    private val restoreMiddlewareList: MutableList<RestoreMiddleware<*>> = mutableListOf()

    private var initialized = false

    private lateinit var applicationContext: Context

    private lateinit var jsonConverter: JsonConverter

    private var job: Job? = null

    @MainThread
    fun init(context: Context) {
        if (initialized) {
            return
        }
        applicationContext = context.applicationContext
        (applicationContext as Application)
            .registerActivityLifecycleCallbacks(RestoreActivityLifecycleCallbacks())
        this.jsonConverter = applicationContext.globalEntryPoint.jsonConverter()
        initialized = true
    }


    internal fun addRestoreMiddleware(middleware: RestoreMiddleware<*>) {
        restoreMiddlewareList.add(middleware)
    }

    internal fun removeRestoreMiddleware(middleware: RestoreMiddleware<*>) {
        restoreMiddlewareList.remove(middleware)
    }

    fun <T> getLastValue(tag: String, clazz: Class<T>): T? {
        val lastRestore = RestoreDatabase.getInstance(applicationContext)
            .restoreDao()
            .find(tag)
        val lastValue = lastRestore?.lastValue
        if (lastValue != null) {
            val result = jsonConverter.fromJson<T>(lastValue, clazz)
            RestoreDatabase.getInstance(applicationContext)
                .restoreDao()
                .delete(lastRestore)
            return result
        }
        return null
    }

    private fun start() {
        //起个延时任务，如果在时间到之前重新进入前台，取消任务
        //延迟20秒
        //如果app进入后台后一分钟后app还未被杀
        job = GlobalScope.launch(Dispatchers.IO) {
            val stateList = mutableListOf<RestoreEntity>()
            val time = System.currentTimeMillis()
            restoreMiddlewareList.forEach {
                if (it.lastState != null) {
                    val lastValue = jsonConverter.toJson(it.lastState, it.lastState!!.javaClass)
                    stateList.add(RestoreEntity(it.tag, lastValue, time))
                }
            }
            delay(1000 * 20)
            RestoreDatabase.getInstance(applicationContext)
                .restoreDao()
                .insert(*stateList.toTypedArray())
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
    }

    companion object {
        private val INSTANCE: Restore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            Restore()
        }

        fun getInstance(): Restore {
            return INSTANCE
        }
    }


    internal class RestoreActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            getInstance().stop()
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
            val isForeground = isForeground(activity)
            if (isForeground) {
                return
            }
            //app进入后台
            getInstance().start()
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {

        }

        private fun isForeground(context: Context): Boolean {
            var isForeground = false
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.runningAppProcesses?.let {
                for (processInfo in it) {
                    if (processInfo.processName == context.packageName
                        && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    ) {
                        isForeground = true
                        break
                    }
                }
            }
            isForeground = false
            return isForeground
        }
    }
}

inline fun <reified T> Restore.getValue(tag: String): T? {
    return this.getLastValue(tag, T::class.java)
}