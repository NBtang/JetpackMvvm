package me.laotang.carry.mvvm.demo.app

import dagger.hilt.android.HiltAndroidApp
import me.laotang.carry.app.CarryApplication
import me.laotang.carry.mvvm.binding.CommandHook
import timber.log.Timber

@HiltAndroidApp
class App : CarryApplication() {
    override fun onCreate() {
        super.onCreate()
        //通过command实现的UI事件（比如点击，滑动等）回调，支持hook
        //在xml中dataBinding实现事件绑定，也支持hook
        //设置全局默认hook回调，也可以在绑定时指定自定义的hook回调
        //简易版的点击事件埋点hook
        CommandHook.setDefaultHook(object :CommandHook(){
            override fun beforeInvoke(commandName: String, params: Any): Boolean {
                Timber.tag("CommandHook")
                Timber.d("CommandHook before commandName:${commandName},params:${params}")
                return super.beforeInvoke(commandName, params)
            }

            override fun afterInvoke(commandName: String, params: Any) {
                Timber.tag("CommandHook")
                Timber.d("CommandHook after commandName:${commandName},params:${params}")
                super.afterInvoke(commandName, params)
            }
        })
    }
}