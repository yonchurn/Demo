package com.common.base.adapter

import android.view.View

enum class ItemType {
    //itemView类型 行
    VIEW,

    //itemView类型 header
    HEADER,

    //itemView类型 footer
    FOOTER,
}

/**
 * section 适配器
 */
internal interface SectionAdapter {

    //section数量
    fun numberOfSections(): Int{
        return 1
    }

    //每个section中的item数量
    fun numberOfItems(section: Int): Int

    //是否需要section的头部
    fun shouldExistSectionHeader(section: Int): Boolean{
        return false
    }

    //是否需要section的底部
    fun shouldExistSectionFooter(section: Int): Boolean{
        return false
    }

    //getItemViewType 的重写方法
    fun getItemViewType(positionInSection: Int, section: Int, type: ItemType): Int{
        return type.ordinal
    }

    //点击item
    fun onItemClick(positionInSection: Int, section: Int, item: View) {}

    //点击头部
    fun onHeaderClick(section: Int, header: View) {}

    //点击底部
    fun onFooterClick(section: Int, footer: View) {}
}