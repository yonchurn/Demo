package com.common.base.route

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor

//路由拦截 优先级最低
@Interceptor(priority = Int.MAX_VALUE)
class BaseRouteInterceptor: IInterceptor {

    override fun init(context: Context?) {
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        //保存路由地址
        var path = postcard.path
        if (path == null) {
            path = postcard.uri?.path
        }

        postcard.withString(RouteHelper.PATH, path)
        callback.onContinue(postcard)
    }
}