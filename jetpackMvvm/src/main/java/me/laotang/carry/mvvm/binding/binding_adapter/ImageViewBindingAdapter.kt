package me.laotang.carry.mvvm.binding.binding_adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import me.laotang.carry.core.imageloader.UrlImageLoaderViewTarget
import me.laotang.carry.util.globalEntryPoint

@BindingAdapter("url")
fun ImageView.setUrl(url: String) {
    try {
        this.context.globalEntryPoint
            .imageLoader()
            .loadImage(this, UrlImageLoaderViewTarget(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}