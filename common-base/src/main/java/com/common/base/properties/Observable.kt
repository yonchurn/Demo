package com.common.base.properties

import androidx.annotation.CallSuper
import kotlin.reflect.KProperty

typealias ObservableCallback = (oldValue: Any?, newValue: Any?, property: KProperty<*>) -> Unit

/**
 * 可观察的，用来监听属性值变化的，属性必须使用 ObservableProperty
 *
 * 举个🌰
 * class User: BaseObservable() {
 *   var title by ObservableProperty<String?>(null, this)
 *   var subtitle by ObservableProperty<String>("副标题", this)
 * }
 *
 *  user.addObserver(this, arrayOf("title", "subtitle"), {oldValue, newValue, property ->
 *       //doSomething
 *   })
 */
@Suppress("unchecked_cast")
interface Observable: ObservableProperty.Callback {

    //回调
    val callbacks: HashMap<Int, HashMap<String, Any>>

    /**
     * 添加观察者
     * @param byUser 为true时不会自动回调，需要调用notifyChange
     */
    fun addObserver(observer: Any, name: String, callback: ObservableCallback, byUser: Boolean = false) {
        addObserverInternal(observer.hashCode(), if(byUser) CallbackEntity(callback) else callback, name)
    }

    fun addObserver(observer: Any, names: Array<String>, callback: ObservableCallback, byUser: Boolean = false) {
        val key = observer.hashCode()
        for (name in names) {
            addObserverInternal(key, if(byUser) CallbackEntity(callback) else callback, name)
        }
    }

    private fun addObserverInternal(key: Int, callback: Any, name: String) {
        var map = callbacks[key]
        if (map == null) {
            map = HashMap()
            callbacks[key] = map
        }
        map[name] = callback
    }

    /**
     * 移除观察者
     * @param name 监听的属性名称，如果为空，则移除这个观察者的所有监听属性
     */
    fun removeObserver(observer: Any, name: String? = null) {
        val key = observer.hashCode()
        if (name != null) {
            val map = callbacks[key]
            if (map != null) {
                map.remove(name)
                if (map.size == 0) {
                    callbacks.remove(key)
                }
            }
        } else {
            callbacks.remove(key)
        }
    }

    fun removeObserver(observer: Any, names: Array<String>) {
        val key = observer.hashCode()
        val map = callbacks[key]
        if (map != null) {
            for (name in names) {
                map.remove(name)
            }
            if (map.size == 0) {
                callbacks.remove(key)
            }
        }
    }

    //手动回调，只回调byUser = true，并且值改变过的
    fun notifyChange() {
        for ((_, map) in callbacks) {
            for ((_, entity) in map) {
                if (entity is CallbackEntity && entity.hasOldValue) {
                    entity.callback(entity.oldValue, entity.newValue, entity.property!!)
                    entity.reset()
                }
            }
        }
    }

    @CallSuper
    override fun onPropertyValueChange(oldValue: Any?, newValue: Any?, property: KProperty<*>) {
        for ((_, map) in callbacks) {
            val entity = map[property.name]

            if (entity is CallbackEntity) {
                //记录下来 后面只回调一次，防止多次改变触发多次回调
                entity.oldValue = oldValue
                entity.newValue = newValue
                entity.property = property
            } else {
                val callback = entity as ObservableCallback
                callback(oldValue, newValue, property)
            }
        }
    }

    /**
     * 手动回调的实体
     */
    private class CallbackEntity(val callback: ObservableCallback) {

        //旧值
        private var _oldValue: Any? = null
        var oldValue: Any?
            get() = _oldValue
            set(value) {
                if(!hasOldValue){
                    hasOldValue = true
                    _oldValue = value
                }
            }

        //是否有旧值，旧值只设置一次
        var hasOldValue: Boolean = false

        //新值
        var newValue: Any? = null

        //关联的属性
        var property: KProperty<*>? = null

        //重置
        fun reset(){
            hasOldValue = false
            _oldValue = null
            newValue = null
            property = null
        }
    }
}