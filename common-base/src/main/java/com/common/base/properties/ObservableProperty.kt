package com.common.base.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 可监听值改变，只有值不同的时候才会回调
 */
class ObservableProperty<T>(var value: T, val callback: Callback?):
    ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if(this.value != value){
            val oldValue = this.value
            this.value = value

            if(callback != null){
                callback.onPropertyValueChange(oldValue, value, property)
            }
        }
    }

    /**
     * 值变化回调
     */
    interface Callback {

        //值变化了
        fun onPropertyValueChange(oldValue: Any?, newValue: Any?, property: KProperty<*>)
    }
}