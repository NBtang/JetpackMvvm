package me.laotang.carry.mvvm.demo.app

import android.app.Application
import android.content.Context
import com.orhanobut.logger.*
import me.laotang.carry.app.DummyAppLifecycleCallbacks
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import timber.log.Timber.DebugTree

class AppLifecycleCallbacksImpl :
    DummyAppLifecycleCallbacks() {

    override fun attachBaseContext(base: Context?, application: Application) {
    }

    override fun onCreate(application: Application) {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
            .methodCount(0) // (Optional) How many method line to show. Default 2
            .methodOffset(7) // (Optional) Hides internal method calls up to offset. Default 5
            .logStrategy(LogcatLogStrategy()) // (Optional) Changes the log strategy to print out. Default LogCat
            .tag("txf") // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        Timber.plant(object : DebugTree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                try {
                    if (message.startsWith("{") && message.endsWith("}")) {
                        JSONObject(message)
                        Logger.json(message)
                    } else if (message.startsWith("[") && message.endsWith("]")) {
                        JSONArray(message)
                        Logger.json(message)
                    } else {
                        Logger.log(priority, tag, message, t)
                    }
                } catch (e: Exception) {
                    Logger.log(priority, tag, message, t)
                }
            }

            override fun isLoggable(tag: String?, priority: Int): Boolean {
                return true
            }
        })
    }
}