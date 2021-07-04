package me.laotang.carry.mvvm.demo.app

import android.content.Context
import me.laotang.carry.app.AppLifecycleCallbacks
import me.laotang.carry.core.IConfigModule
import me.laotang.carry.di.GlobalConfigModule
import me.laotang.carry.di.RetrofitConfiguration
import me.laotang.carry.mvvm.demo.di.ComponentEntryPoint
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppConfigModule : IConfigModule {
    override fun applyOptions(context: Context, builder: GlobalConfigModule.Builder) {
        builder.baseUrl("https://api.github.com/")
            .retrofitConfiguration(object : RetrofitConfiguration {
                override fun configRetrofit(context: Context, builder: Retrofit.Builder) {
                    val moshi = ComponentEntryPoint.getEntryPoint(context).moshi()
                    builder.addConverterFactory(MoshiConverterFactory.create(moshi))
                }
            })
            .jsonConverter(JsonConverterImpl(context))
    }

    override fun addAppLifecycleCallback(lifecycleCallbacks: MutableList<AppLifecycleCallbacks>) {
        lifecycleCallbacks.add(AppLifecycleCallbacksImpl())
    }
}