package com.common.base.adapter

import com.common.base.widget.StickContainer

//悬浮固定item适配器
interface StickAdapter {

    //是否需要悬浮固定
    fun shouldStickAtPosition(position: Int): Boolean

    //根据当前第一个可见item获取当前可悬浮固定的item
    fun getCurrentStickPosition(firstVisibleItem: Int): Int

    //悬浮状态改变了
    fun onViewStickChange(stick: Boolean, view: StickContainer, position: Int) {

    }
    //悬浮偏移量
    fun getStickOffset(): Int {
        return 0
    }
}