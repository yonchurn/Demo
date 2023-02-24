package com.common.base.app

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.view.ContextThemeWrapper
import com.common.base.R
import java.util.*

/**
 * 包装类，国际化用的
 */
@Suppress("deprecation")
class BaseContextWrapper(base: Context): ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            val configuration = context.resources.configuration
            configuration.fontScale = 1.0f
            val sysLocal = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                getSystemLocale(configuration)
            } else {
                getSystemLocaleLegacy(configuration)
            }
            if (sysLocal.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(configuration, locale)
                } else {
                    setSystemLocaleLegacy(configuration, locale)
                }
            }

            var resultContext = context
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resultContext = context.createConfigurationContext(configuration)
            } else {
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            }

            return BaseContextThemeWrapper(BaseContextWrapper(resultContext), androidx.appcompat.R.style.Theme_AppCompat_Empty)
        }

        fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}

/**
 * 解决androidx 1.2.0 切换语言失败的问题
 */
class BaseContextThemeWrapper(context: Context, res: Int): ContextThemeWrapper(context, res) {

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setTo(baseContext.resources.configuration)
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}