package com.common.base.refresh


/**
 * 加载更多控制器
 */
class LoadMoreControl {
    
    //状态
    var loadingStatus = LoadMoreStatus.NORMAL
    set(value) {
        if(field != value){
            field = value
            if (field == LoadMoreStatus.NO_MORE_DATA && hideWhenNoMoreData) {
                field = LoadMoreStatus.NORMAL
            }
            onStatusChange()
        }
    }

    //所有数据加载完成后是否还显示 加载更多的视图
    var hideWhenNoMoreData: Boolean = false

    //加载更多的View
    var loadMoreFooter: LoadMoreFooter? = null
        set(value) {
            if (value != field) {
                field = value
                onStatusChange()
            }
        }

    private fun onStatusChange() {
        loadMoreFooter?.loadingStatus = loadingStatus
    }
}