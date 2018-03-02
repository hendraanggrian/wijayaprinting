@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package com.wijayaprinting.scene.control

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TextField
import kotfx.annotations.LayoutDsl
import kotfx.beans.binding.booleanBindingOf
import kotfx.layout.ChildManager
import kotfx.layout.ItemManager
import kotfx.listeners.listener
import org.apache.commons.validator.routines.InetAddressValidator.getInstance

/** Field that display IP address. */
open class HostField : TextField() {

    val validProperty: BooleanProperty = SimpleBooleanProperty()

    init {
        validProperty.bind(booleanBindingOf(textProperty()) { getInstance().isValidInet4Address(text) })
        focusedProperty().listener { _, _, value -> if (value && text.isNotEmpty()) selectAll() }
    }

    val isValid: Boolean get() = validProperty.get()
}

inline fun hostField(noinline init: ((@LayoutDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }
inline fun ChildManager.hostField(noinline init: ((@LayoutDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }.add()
inline fun ItemManager.hostField(noinline init: ((@LayoutDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }.add()
