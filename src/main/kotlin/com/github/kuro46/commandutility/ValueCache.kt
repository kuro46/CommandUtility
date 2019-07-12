package com.github.kuro46.commandutility

import java.lang.ref.SoftReference

class ValueCache<T>(initial: T? = null) {

    var softReference = SoftReference(initial)

    fun get(): T? {
        return softReference.get()
    }

    fun set(value: T) {
        softReference = SoftReference(value)
    }

    fun getOrSet(valueSupplier: () -> T): T {
        return softReference.get() ?: run {
            val value = valueSupplier()
            set(value)
            value
        }
    }
}
