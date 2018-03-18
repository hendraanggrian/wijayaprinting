package com.hendraanggrian.openpss.ui

import com.hendraanggrian.openpss.Language
import com.hendraanggrian.openpss.db.schema.Employee
import com.hendraanggrian.openpss.io.properties.ConfigFile
import javafx.fxml.FXML
import java.util.ResourceBundle

/** Base class of all controllers. */
abstract class Controller : Resourced {

    @FXML abstract fun initialize()

    final override val language: Language = Language.find(ConfigFile.language.value)
    override val resources: ResourceBundle = language.resources

    /** Field name starts with underscore to avoid conflict with [com.hendraanggrian.ui.employee.EmployeeController]. */
    lateinit var _employee: Employee

    val employeeName: String get() = _employee.name
    val isFullAccess: Boolean get() = _employee.fullAccess

    private var extras: MutableMap<String, Any>? = null

    /** Register extra [value] with [key]. */
    fun addExtra(key: String, value: Any): Controller {
        if (extras == null) extras = mutableMapOf()
        extras!![key] = value
        return this
    }

    /** Get extra registered with [key], should be executed in platform thread. */
    fun <T : Any> getExtra(key: String): T {
        checkNotNull(extras) { "No extras added." }
        @Suppress("UNCHECKED_CAST") return checkNotNull(extras!![key] as T) { "Extra with that key is not found." }
    }
}