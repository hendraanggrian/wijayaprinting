@file:Suppress("NOTHING_TO_INLINE")

package com.wijayaprinting.util

import com.wijayaprinting.App
import javafx.scene.paint.Color
import javafx.scene.paint.Color.web
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL

private var ref: WeakReference<Class<*>> = WeakReference(App::class.java)
private val mainClass: Class<*>
    get() {
        var cls = ref.get()
        if (cls == null) {
            cls = App::class.java
            ref = WeakReference(cls)
        }
        return cls
    }

fun getResource(name: String): URL = mainClass.getResource(name)
inline fun getResourceString(name: String): String = getResource(name).toExternalForm()

fun getResourceAsStream(name: String): InputStream = mainClass.getResourceAsStream(name)

fun getColor(name: String): Color = web(name)