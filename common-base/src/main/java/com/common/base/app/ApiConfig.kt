package com.common.base.app

import com.common.base.BuildConfig
import com.common.base.utils.StringUtils

/**
 * api配置
 */
object ApiConfig {

    /**
     * api地址类型
     */
    enum class Type {

        //广州ip
        IP_TEST,

        //广州域名
        DOMAIN_TEST,

        //缅甸预生产
        PRE,

        //缅甸灰度，数据库是正式的数据
        GRAY,

        //缅甸正式
        PRO,
    }

    //http分页请求第一页
    var HttpFirstPage = 1

    //当前环境
    private var mType = Type.PRO
    val type: Type
        get() = mType

    //设置debug环境
    internal fun setDebugType(index: Int) {
        mType = typeForIndex(index)
    }

    //自定义域名
    internal var customApi: String? = null

    //后台返回的域名
    var serverApi: String? = null

    //环境对应的地址
    var apis: HashMap<Type, String>? = null

    //通过下标获取api
    internal fun apiForIndex(index: Int): String {
        if (BuildConfig.DEBUG && apis.isNullOrEmpty()) {
            error("ApiConfig.apiForIndex 必须先设置才能使用")
        }
        val type = typeForIndex(index)
        return apis!![type] ?: ""
    }

    private fun typeForIndex(index: Int): Type {
        return when(index){
            Type.IP_TEST.ordinal -> Type.IP_TEST
            Type.DOMAIN_TEST.ordinal -> Type.DOMAIN_TEST
            Type.PRE.ordinal -> Type.PRE
            Type.GRAY.ordinal -> Type.GRAY
            else -> Type.PRO
        }
    }

    //当前使用的
    val api: String
        get() {
            if (BuildConfig.DEBUG && apis.isNullOrEmpty()) {
                error("ApiConfig.apis 必须先设置才能使用")
            }
            if (StringUtils.isNotEmpty(customApi)) {
                return customApi!!
            }

            if (StringUtils.isNotEmpty(serverApi)) {
                return serverApi!!
            }

            val value = apis!![type]
            if (BuildConfig.DEBUG && !StringUtils.isNotEmpty(value)) {
                error("ApiConfig 当前环境没有接口地址")
            }

            return value!!
        }
}