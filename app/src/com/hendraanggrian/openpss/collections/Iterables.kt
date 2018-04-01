@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.openpss.collections

inline fun <T> Iterable<T>.isEmpty(): Boolean = count() == 0

inline fun <T> Iterable<T>.isNotEmpty(): Boolean = count() != 0