package com.common.base.section

/**
 * 用于 listView RecyclerView 分成多块
 */
open class SectionInfo {

    //section下标 section中行数 该section的起点
    var section = 0
    var numberItems = 0
    var sectionBegin = 0

    //是否存在 footer 和 header
    var isExistHeader = false
    var isExistFooter = false

    //获取头部位置
    fun getHeaderPosition(): Int {
        return sectionBegin
    }

    //获取底部位置
    fun getFooterPosition(): Int {
        var footerPosition = sectionBegin
        if (isExistHeader) footerPosition++

        return footerPosition + numberItems
    }

    //获取item的起始位置
    fun getItemStartPosition(): Int {
        return if (isExistHeader) {
            sectionBegin + 1
        } else {
            sectionBegin
        }
    }

    //获取item在section的位置
    fun getItemPosition(position: Int): Int {
        return position - getItemStartPosition()
    }

    //判断position是否是头部
    fun isHeaderForPosition(position: Int): Boolean {
        return isExistHeader && position == getHeaderPosition()
    }

    //判断position是否是底部
    fun isFooterForPosition(position: Int): Boolean {
        return isExistFooter && position == getFooterPosition()
    }
}