package com.common.base.language

import com.common.base.utils.AlertUtils
import com.common.base.utils.AppUtils
import com.common.base.utils.PrefsUtils
import com.common.base.utils.StringUtils
import java.util.*

/**
 * 语言帮助类
 */
object LanguageHelper {

    private const val languageDidSelectKey = "languageDidSelect"

    //语言类型
    enum class Type {
        MY, //缅甸 mm3
        MY_MM, //缅甸zawgyi
        EN, //英文
        ZH_CN, //简体中文
    }

    const val chineseName = "中文(简体)"
    const val chinese = "zh"

    const val englishName = "English"
    const val english = "en"

    const val mm3Name = "မြန်မာ(UNICode)"
    const val mm3 = "my"

    const val zawgyiName = "ျမန္မာ(ZAWGYI)"
    const val zawgyi = "zawgyi"

    /**
     * 是否已选择语言
     */
    val isSelectLanguage: Boolean
        get() = PrefsUtils.loadBoolean(languageDidSelectKey, false)

    /**
     * app第一次运行，要选择语言
     */
    fun selectLanguage(callback: () -> Unit) {
        val titles = arrayOf(mm3Name, zawgyiName, englishName, chineseName)
        AlertUtils.actionSheet(
            title = "Select Language",
            buttonTitles = titles,
            onItemClick = {
                setCurrentLanguage(languageForName(titles[it]))
                PrefsUtils.save(languageDidSelectKey, true)
                callback()
        }).show()
    }

    private fun languageTypeForKey(key: String): Type {
        return when(key) {
            mm3 -> Type.MY
            zawgyi -> Type.MY_MM
            english -> Type.EN
            chinese -> Type.ZH_CN
            else -> Type.MY_MM
        }
    }

    fun languageForName(name: String): String {
        return when(name) {
            mm3Name -> mm3
            zawgyiName -> zawgyi
            englishName -> english
            chineseName -> chinese
            else -> zawgyi
        }
    }

    private const val currentLanguageKey = "currentLanguage"

    //当前语言
    private var mCurrentLanguage: String? = null
    internal val currentLanguage: String
        get() {
            if (mCurrentLanguage == null) {
                mCurrentLanguage = PrefsUtils.loadString(currentLanguageKey, mm3)
            }
            return mCurrentLanguage!!
        }


    private var mCurrentLanguageType: Type? = null
    private val currentLanguageType: Type
        get() {
            if (mCurrentLanguageType == null) {
                mCurrentLanguageType = languageTypeForKey(currentLanguage)
            }
            return mCurrentLanguageType!!
        }

    private var mCurrentLocal: Locale? = null
    private val currentLocal: Locale
        get() {
            if(mCurrentLocal == null) {
                mCurrentLocal = Locale(currentLanguage)
            }
            return mCurrentLocal!!
        }

    /**
     * 当前语言是否是zawgyi
     */
    val isZawgyi: Boolean
        get() = currentLanguageType == Type.MY_MM

    /**
     * 当前语言是否是mm3
     */
    val isMy: Boolean
        get() = currentLanguageType == Type.MY

    /**
     * 主要是为了 和 iOS、H5统一
     */
    val currentLanguageToCommon: String
        get() = when (currentLanguageType) {
            Type.ZH_CN -> "zh-Hans"
            Type.EN -> "en"
            Type.MY -> "my"
            else -> "my-MM"
        }

    /**
     * 设置当前语言
     */
    fun setCurrentLanguage(language: String) {
        if(language != currentLanguage){
            mCurrentLanguage = language
            mCurrentLanguageType = languageTypeForKey(currentLanguage)
            mCurrentLocal = Locale(currentLanguage)
            PrefsUtils.save(currentLanguageKey, currentLanguage)
        }
    }

    /**
     * 语言转换
     * 服务端返回的数据是 mm3
     * 如果当前语言是zawgyi，要转成zawgyi
     */
    fun convertToZawgyiIfNeeded(input: String?): String? {
        if (StringUtils.isEmpty(input)) {
            return input
        }
        return if (isZawgyi) {
            Rabbit.uni2zg(input)
        } else {
            input
        }
    }

    /**
     * 转换语言为 mm3，上传到服务器时用到
     *
     * @param input
     */
    fun covertToMm3IfNeeded(input: String?): String? {
        if (StringUtils.isEmpty(input)) {
            return input
        }
        return if (isZawgyi) {
            Rabbit.zg2uni(input)
        } else {
            input
        }
    }
}