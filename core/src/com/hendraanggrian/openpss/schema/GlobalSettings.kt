package com.hendraanggrian.openpss.schema

import com.hendraanggrian.openpss.data.GlobalSetting
import kotlinx.nosql.mongodb.DocumentSchema
import kotlinx.nosql.string

object GlobalSettings : DocumentSchema<GlobalSetting>("$GlobalSettings", GlobalSetting::class),
    Schemed {
    val key = string("key")
    val value = string("value")

    val LANGUAGE = "language" to "en-US" // or equivalent to Language.EN_US.fullCode
    val INVOICE_HEADERS = "invoice_headers" to ""

    override fun toString(): String = "global_settings"
}