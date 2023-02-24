package com.common.base.utils

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import androidx.preference.PreferenceManager
import com.common.base.base.activity.ActivityLifeCycleManager
import java.io.*

/**
 * prefs 缓存工具
 */
object PrefsUtils {

    private val context: Context
        get() = ActivityLifeCycleManager.currentContext

    /**
     * 保存配置文件
     * @param key 要保存的key
     * @param value 保存的值
     */
    fun save(key: String, value: Any?) {

        val prefsKey = getKey(key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        when(value){
            is Boolean -> editor.putBoolean(prefsKey, value)
            is Int -> editor.putInt(prefsKey, value)
            is String -> editor.putString(prefsKey, value)
            is Long -> editor.putLong(prefsKey, value)
            is Float -> editor.putFloat(prefsKey, value)
            else -> {
                throw UnsupportedOperationException("don't support $value.toString()")
            }
        }
        editor.apply()
    }

    //获取key 加上包名 防止和第三方库的key冲突
    fun getKey(key: String): String{
        return "${AppUtils.appPackageName}.$key"
    }

    //////////////////////////////加载配置文件中的信息////////////////////////////////
    fun loadString(key: String, defValue: String? = null): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(getKey(key), defValue)
    }

    fun loadInt(key: String, defValue: Int = 0): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(getKey(key), defValue)
    }

    fun loadBoolean(key: String, defValue: Boolean = false): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(getKey(key), defValue)
    }

    fun loadLong(key: String, defValue: Long = 0): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getLong(getKey(key), defValue)
    }

    fun loadFloat(key: String, defValue: Float = 0f): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getFloat(getKey(key), defValue)
    }

    fun remove(key: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().remove(getKey(key)).apply()
    }

    // 是否包含key
    fun contains(key: String): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.contains(getKey(key))
    }

    //保存对象
    fun saveObject(key: String, obj: Serializable) {
        if (TextUtils.isEmpty(key))
            return
        val prefsKey = getKey(key)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val outputStream = ByteArrayOutputStream()
        try {
            val oos = ObjectOutputStream(outputStream)
            oos.writeObject(obj)
            val temp = String(Base64.encode(outputStream.toByteArray(), Base64.DEFAULT))
            prefs.putString(prefsKey, temp)
            prefs.apply()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inline fun <reified T> getObject(context: Context, key: String): T? {
        if (TextUtils.isEmpty(key))
            return null

        val prefsKey = getKey(key)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val temp = prefs.getString(prefsKey, "")
        val inputStream = ByteArrayInputStream(Base64.decode(temp!!.toByteArray(), Base64.DEFAULT))
        var obj: T? = null
        try {
            val ois = ObjectInputStream(inputStream)
            val result = ois.readObject()
            if(result is T){
                obj = result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }
}