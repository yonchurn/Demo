package com.common.base.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.utils.AppUtils
import com.common.base.utils.StringUtils

//应用市场包名
private object Market {

    const val GOOGLE_PLAY = "com.android.vending" //google
    const val HUA_WEI = "com.huawei.appmarket" //华为应用市场
    const val XIAO_MI = "com.xiaomi.market" //小米应用商店
    const val XIAO_MI_INTER = "com.xiaomi.mipicks" //小米国际应用商店
    const val OPPO = "com.oppo.market" //OPPO应用商店
    const val OPPO_INTER = "com.heytap.market" //OPPO国际应用商店
    const val VIVO = "com.bbk.appstore" //VIVO应用商店
    const val VIVO_INTER = "com.vivo.appstore" //VIVO国际应用商店
    const val WEB = "web" //浏览器
}

/**
 * app渠道
 */
object Channel {

    const val GOOGLE_PLAY = "ANDROID_GOOGLE" //谷歌
    const val HUA_WEI = "ANDROID_HUAWEI" //华为市场
    const val WEB = "ANDROID_WEB" //浏览器
    const val XIAO_MI = "ANDROID_XIAOMI" //小米
    const val OPPO = "ANDROID_OPPO" //OPPO
    const val VIVO = "ANDROID_VIVO" //VIVO
}

/**
 * 打包渠道
 */
 object PackingChannel {

    const val GOOGLE_PLAY = "google" //谷歌
    const val HUA_WEI = "huawei" //华为市场
    const val WEB = "web" //浏览器
    const val XIAO_MI = "xiaomi" //xiaomi
    const val OPPO = "oppo" //oppo
    const val VIVO = "vivo" //vivo
}

/**
 * 更新渠道
 * */
object UpdateChannel {
    const val WEB = "UPDATE_CHANNEL_WEB"
    const val PLATFORM = "UPDATE_CHANNEL_CHANNEL"
}

//app版本管理
object AppVersionManager {

    private val context: Context
        get() = ActivityLifeCycleManager.currentContext

    //自定义渠道
    internal var customChannel: String? = null

    /**
     * 获取当前渠道
     */
    val channel: String
        get() {
            return try {
                val channel =  if (StringUtils.isEmpty(customChannel)) {
                    val packageManager = context.packageManager
                    val info = packageManager.getApplicationInfo(AppUtils.appPackageName, PackageManager.GET_META_DATA)
                    info.metaData.getString("APP_CHANNEL")
                } else customChannel!!
                when (channel) {
                    PackingChannel.GOOGLE_PLAY -> Channel.GOOGLE_PLAY
                    PackingChannel.HUA_WEI -> Channel.HUA_WEI
                    PackingChannel.XIAO_MI -> Channel.XIAO_MI
                    PackingChannel.OPPO -> Channel.OPPO
                    PackingChannel.VIVO -> Channel.VIVO
                    else -> Channel.WEB
                }
            }catch (e: Exception) {
                Channel.WEB
            }
        }

    /**
     * 打开应用市场
     * @param updateChannel 更新渠道
     * @param webDownloadUrl 网页的下载链接
     * @param channelDownloadUrl 平台的下载链接
     */
    fun openMarket(updateChannel: String, webDownloadUrl: String? = null, channelDownloadUrl: String? = null) {
        try {
            when(updateChannel){
                UpdateChannel.PLATFORM -> {
                    if (StringUtils.isEmpty(channelDownloadUrl)){
                        AppUtils.openUrl(webDownloadUrl)
                    }else if (channelDownloadUrl!!.startsWith("http")){
                        AppUtils.openUrl(channelDownloadUrl)
                    }
                    else{
                        val uri = Uri.parse(channelDownloadUrl)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
                else -> {
                    AppUtils.openUrl(webDownloadUrl)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            AppUtils.openUrl(webDownloadUrl)
        }
    }
}