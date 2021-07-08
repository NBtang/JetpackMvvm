package me.laotang.carry.mvvm.demo.app

import android.content.Context
import android.view.View
import me.laotang.carry.app.AppLifecycleCallbacks
import me.laotang.carry.core.IConfigModule
import me.laotang.carry.core.imageloader.ImageLoader
import me.laotang.carry.core.imageloader.ImageLoaderInterceptor
import me.laotang.carry.core.imageloader.ImageLoaderViewTarget
import me.laotang.carry.di.GlobalConfigModule
import me.laotang.carry.di.ImageLoaderConfiguration
import me.laotang.carry.di.RetrofitConfiguration
import me.laotang.carry.mvvm.demo.R
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
            .responseErrorListener(ResponseErrorListenerImpl())
            .jsonConverter(JsonConverterImpl(context))
            .imageLoaderConfiguration(object : ImageLoaderConfiguration {
                override fun configImageLoader(context: Context, imageLoader: ImageLoader) {
                    imageLoader.addImageLoaderInterceptor(object :
                        ImageLoaderInterceptor<ImageLoaderViewTarget<*>>{
                        override fun intercept(
                            chain: ImageLoaderInterceptor.Chain<ImageLoaderViewTarget<*>>,
                            view: View,
                            viewTarget: ImageLoaderViewTarget<*>
                        ) {
                            viewTarget.placeholder = R.mipmap.ic_launcher_round
                            chain.proceed(view,viewTarget)
                        }
                    })
                }
            })
    }

    override fun addAppLifecycleCallback(lifecycleCallbacks: MutableList<AppLifecycleCallbacks>) {
        lifecycleCallbacks.add(AppLifecycleCallbacksImpl())
    }
}