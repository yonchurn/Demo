package com.common.base.utils

import android.graphics.drawable.Drawable
import com.common.base.dialog.AlertDialogFragment
import com.common.base.dialog.AlertStyle

/**
 * 弹窗工具类
 */
object AlertUtils {

    fun alert(
        title: String? = null,
        subtitle: String? = null,
        icon: Drawable? = null,
        cancelButtonTitle: String? = null,
        buttonTitles: Array<String>?,
        destructivePosition: Int? = null,
        onItemClick: ((position: Int) -> Unit)? = null
    ): AlertDialogFragment {

        return build(
            AlertStyle.ALERT,
            title,
            subtitle,
            icon,
            cancelButtonTitle,
            buttonTitles,
            destructivePosition,
            onItemClick
        )
    }

    fun actionSheet(
        title: String? = null,
        subtitle: String? = null,
        icon: Drawable? = null,
        cancelButtonTitle: String? = null,
        buttonTitles: Array<String>?,
        destructivePosition: Int? = null,
        onItemClick: ((position: Int) -> Unit)? = null
    ): AlertDialogFragment {

        return build(
            AlertStyle.ACTION_SHEET,
            title,
            subtitle,
            icon,
            cancelButtonTitle,
            buttonTitles,
            destructivePosition,
            onItemClick
        )
    }

    private fun build(
        style: AlertStyle,
        title: String? = null,
        subtitle: String? = null,
        icon: Drawable? = null,
        cancelButtonTitle: String? = null,
        buttonTitles: Array<String>?,
        destructivePosition: Int? = null,
        onItemClick: ((position: Int) -> Unit)? = null
    ): AlertDialogFragment {
        val fragment = AlertDialogFragment(style, title, subtitle, icon, cancelButtonTitle, buttonTitles)
        fragment.onItemClick = onItemClick
        if (destructivePosition != -1) {
            fragment.adapter = object : AlertDialogFragment.AlertDialogAdapter {
                override fun shouldEnable(fragment: AlertDialogFragment, position: Int): Boolean {
                    return true
                }

                override fun shouldDestructive(
                    fragment: AlertDialogFragment,
                    position: Int
                ): Boolean {
                    return position == destructivePosition
                }
            }
        }
        return fragment
    }
}