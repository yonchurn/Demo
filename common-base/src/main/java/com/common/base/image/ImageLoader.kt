package com.common.base.image

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


object ImageLoader {

    val options = RequestOptions.timeoutOf(15000)

    //加载图片
    fun loadImage(view: ImageView, url: String?) {
        Glide.with(view.context).load(url).apply(options).into(view)
    }

    fun loadImage(view: ImageView, url: String?, placeholder: Int) {
        Glide.with(view.context).load(url).apply(options).placeholder(placeholder).into(view)
    }

    //加载原始大小的图片
    fun loadOriginImage(view: ImageView, url: String?, placeholder: Int) {
        Glide.with(view.context)
            .load(url)
            .placeholder(placeholder)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .apply(options)
            .into(view)
    }
}