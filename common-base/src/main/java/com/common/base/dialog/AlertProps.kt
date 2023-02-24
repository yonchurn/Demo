package com.common.base.dialog

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.common.base.R
import com.common.base.utils.SizeUtils

//弹窗属性
class AlertProps {

    //弹窗背景颜色
    @ColorInt
    var backgroundColor = 0

    //按钮点击背景颜色
    @ColorInt
    var selectedBackgroundColor = 0

    //圆角半径
    var cornerRadius = 0

    //按钮高度padding
    var buttonTopBottomPadding = 0

    //按钮左右padding
    var buttonLeftRightPadding = 0

    //内容垂直间隔
    var contentVerticalSpace = 0

    //内容上下边距
    var contentPadding = 0

    //弹窗边距
    var dialogPadding = 0

    //提示框内容（除了按钮）最低高度
    var contentMinHeight = 0

    //标题字体颜色
    @ColorInt
    var titleColor = 0

    //标题字体大小 sp
    var titleSize = 0f

    //副标题字体颜色
    @ColorInt
    var subtitleColor = 0

    //副标题字体大小 sp
    var subtitleSize = 0f

    //按钮字体大小 sp
    var buttonTextSize = 0f

    //按钮字体颜色
    @ColorInt
    var buttonTextColor = 0

    //无法点击的按钮字体颜色
    @ColorInt
    var buttonDisableTextColor = 0

    //警示按钮字体颜色 如删除
    @ColorInt
    var destructiveButtonTextColor = 0

    //警示按钮背景颜色
    @ColorInt
    var destructiveButtonBackgroundColor = 0

    //警示按钮高亮背景颜色
    @ColorInt
    var destructiveButtonSelectedBackgroundColor = 0

    //警示下标
    var destructivePosition = -1

    //分割线大小px
    var dividerHeight = 0

    //构建
    companion object{

        fun build(context: Context) : AlertProps{
            val props = AlertProps()

            props.backgroundColor = ContextCompat.getColor(context, R.color.alert_dialog_background_color)
            props.selectedBackgroundColor = ContextCompat.getColor(context, R.color.alert_high_lighted_background_color)

            props.titleColor = ContextCompat.getColor(context, R.color.alert_title_color)
            props.subtitleColor = ContextCompat.getColor(context, R.color.alert_subtitle_color)

            props.buttonTextColor = ContextCompat.getColor(context, R.color.alert_button_text_color)
            props.destructiveButtonTextColor = ContextCompat.getColor(context, R.color.alert_destructive_button_text_color)
            props.destructiveButtonBackgroundColor = ContextCompat.getColor(context, R.color.alert_destructive_button_background_color)
            props.destructiveButtonSelectedBackgroundColor = ContextCompat.getColor(context, R.color.alert_destructive_button_high_lighted_background_color)

            props.buttonDisableTextColor = ContextCompat.getColor(context, R.color.alert_disable_button_text_color)

            props.dividerHeight = context.resources.getDimensionPixelSize(R.dimen.divider_height)
            props.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.alert_corner_radius)
            props.buttonTopBottomPadding = context.resources.getDimensionPixelSize(R.dimen.alert_button_top_bottom_padding)
            props.buttonLeftRightPadding = context.resources.getDimensionPixelSize(R.dimen.alert_button_left_right_padding)

            props.contentVerticalSpace = context.resources.getDimensionPixelSize(R.dimen.alert_content_vertical_space)
            props.contentPadding = context.resources.getDimensionPixelSize(R.dimen.alert_content_padding)
            props.dialogPadding = context.resources.getDimensionPixelSize(R.dimen.alert_dialog_padding)
            props.contentMinHeight = context.resources.getDimensionPixelSize(R.dimen.alert_content_min_height)

            props.titleSize = SizeUtils.spFromPx(context.resources.getDimensionPixelSize(R.dimen.alert_title_text_size), context)
            props.subtitleSize = SizeUtils.spFromPx(context.resources.getDimensionPixelSize(R.dimen.alert_subtitle_text_size), context)
            props.buttonTextSize = SizeUtils.spFromPx(context.resources.getDimensionPixelSize(R.dimen.alert_button_text_size), context)

            return props
        }
    }
}