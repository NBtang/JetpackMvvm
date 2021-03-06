package me.laotang.carry.mvvm.demo.app

import android.content.Context
import android.view.View
import me.laotang.carry.app.AppLifecycleCallbacks
import me.laotang.carry.core.IConfigModule
import me.laotang.carry.core.imageloader.ImageLoader
import me.laotang.carry.core.imageloader.ImageLoaderInterceptor
import me.laotang.carry.core.imageloader.ImageLoaderViewTarget
import me.laotang.carry.core.imageloader.UrlImageLoaderViewTarget
import me.laotang.carry.di.GlobalConfigModule
import me.laotang.carry.di.ImageLoaderConfiguration
import me.laotang.carry.di.RetrofitConfiguration
import me.laotang.carry.mvvm.binding.CommandHook
import me.laotang.carry.mvvm.config.commandHook
import me.laotang.carry.mvvm.demo.R
import me.laotang.carry.mvvm.demo.di.ComponentEntryPoint
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

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
                            val newViewTarget = if(viewTarget is UrlImageLoaderViewTarget){
                                viewTarget.copy(placeholder =  R.mipmap.ic_launcher_round)
                            }else{
                                viewTarget
                            }
                            chain.proceed(view,newViewTarget)
                        }
                    })
                }
            })
            .commandHook(object : CommandHook(){
                //??????command?????????UI???????????????????????????????????????????????????hook
                //???xml???dataBinding??????????????????????????????hook
                //??????????????????hook????????????????????????????????????????????????hook??????
                //??????????????????????????????hook
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

    override fun addAppLifecycleCallback(lifecycleCallbacks: MutableList<AppLifecycleCallbacks>) {
        lifecycleCallbacks.add(AppLifecycleCallbacksImpl())
    }
}