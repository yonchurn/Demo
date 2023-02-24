package com.common.base.section

/**
 * 网格section 信息
 */
class GridSectionInfo: SectionInfo() {

    //列数
    var numberOfColumns = 0

    //item之间的间隔 px
    var itemSpace = 0

    //item和header之间的间隔 px
    var itemHeaderSpace = 0

    //item和footer之间的间隔 px
    var itemFooterSpace = 0

    //section偏移量
    var sectionInsets: EdgeInsets? = null

    //头部是否需要使用 EdgeInsets
    var headerUseSectionInsets = true
    var footerUseSectionInsets = true
}