package com.hendraanggrian.openpss.content

/** Taken from `android.util.Patterns`, but instead use `kotlin.Regex`. */
val REGEX_PHONE = Regex(
    "(\\+[0-9]+[\\- \\.]*)?" +
        "(\\([0-9]+\\)[\\- \\.]*)?" +
        "([0-9][0-9\\- \\.]+[0-9])"
)