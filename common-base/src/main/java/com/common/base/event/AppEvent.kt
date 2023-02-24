package com.common.base.event

/**
 * app事件
 */
class AppEvent(type: Any?, sender: Any?): BaseEvent(type, sender) {

    //事件类型
    enum class Type {
        //进入前台
        ENTER_FOREGROUND,

        //进入后台
        ENTER_BACKGROUND
    }
}