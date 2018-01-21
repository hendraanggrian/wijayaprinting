package com.wijayaprinting.dialogs

import com.wijayaprinting.BuildConfig
import com.wijayaprinting.R
import com.wijayaprinting.base.Listable
import com.wijayaprinting.base.Resourced
import javafx.collections.ObservableList
import javafx.event.ActionEvent.ACTION
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE
import javafx.scene.control.ButtonType
import javafx.scene.control.ButtonType.CLOSE
import javafx.scene.control.Dialog
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.text.Font.loadFont
import kotfx.*
import java.awt.Desktop.getDesktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.util.stream.Collectors.joining

class AboutDialog(val resourced: Resourced) : Dialog<Unit>(), Resourced by resourced {

    init {
        title = getString(R.string.about)
        content = hbox {
            padding = Insets(48.0)
            imageView(Image(R.png.logo_launcher)) {
                fitWidth = 172.0
                fitHeight = 172.0
            }
            vbox {
                alignment = CENTER_LEFT
                textFlow {
                    text("Wijaya ") { font = loadFont(latoBold, 24.0) }
                    text("Printing") { font = loadFont(latoLight, 24.0) }
                }
                text("${getString(R.string.version)} ${BuildConfig.VERSION}") { font = loadFont(latoRegular, 12.0) } marginTop 2
                text(getString(R.string.about_notice)) { font = loadFont(latoBold, 12.0) } marginTop 20
                textFlow {
                    text("${getString(R.string.powered_by)}  ") { font = loadFont(latoBold, 12.0) }
                    text("JavaFX, MongoDB") { font = loadFont(latoRegular, 12.0) }
                } marginTop 4
                textFlow {
                    text("${getString(R.string.author)}  ") { font = loadFont(latoBold, 12.0) }
                    text("Hendra Anggrian") { font = loadFont(latoRegular, 12.0) }
                } marginTop 4
                hbox {
                    button("GitHub") { setOnAction { browse("https://github.com/hendraanggrian/wijayaprinting") } }
                    button(getString(R.string.check_for_updates)) { setOnAction { browse("https://github.com/hendraanggrian/wijayaprinting") } } marginLeft 8
                } marginTop 20
            } marginLeft 48
        }
        lateinit var listView: ListView<License>
        expandableContent = hbox {
            listView = kotfx.listView {
                prefHeight = 256.0
                items = License.listAll()
                setCellFactory {
                    object : ListCell<License>() {
                        override fun updateItem(item: License?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = null
                            graphic = null
                            if (item != null && !empty) graphic = kotfx.vbox {
                                label(item.repo) { font = loadFont(latoRegular, 12.0) }
                                label(item.owner) { font = loadFont(latoBold, 12.0) }
                            }
                        }
                    }
                }
            }
            titledPane(getString(R.string.open_source_software), listView) { isCollapsible = false }
            titledPane(getString(R.string.license), kotfx.textArea {
                prefHeight = 256.0
                isEditable = false
                textProperty() bind stringBindingOf(listView.selectionModel.selectedIndexProperty()) { listView.selectionModel.selectedItem?.getContent(this@AboutDialog) ?: getString(R.string.select_license) }
            }) { isCollapsible = false }
        }
        button(ButtonType("Homepage", CANCEL_CLOSE)).apply {
            visibleProperty() bind (dialogPane.expandedProperty() and booleanBindingOf(listView.selectionModel.selectedIndexProperty()) { listView.selectionModel.selectedItem != null })
            addEventFilter(ACTION) {
                it.consume()
                browse(listView.selectionModel.selectedItem.homepage)
            }
        }
        button(CLOSE)
    }

    private fun browse(url: String) = try {
        getDesktop().browse(URI(url))
    } catch (e: Exception) {
        errorAlert(e.message.toString()).showAndWait()
    }

    enum class License(val owner: String, val repo: String, val homepage: String) {
        APACHE_COMMONS_LANG("Apache", "commons-lang", "https://commons.apache.org/lang"),
        APACHE_COMMONS_MATH("Apache", "commons-math", "https://commons.apache.org/math"),
        APACHE_COMMONS_VALIDATOR("Apache", "commons-validator", "https://commons.apache.org/validator"),
        APACHE_POI("Apache", "POI", "https://poi.apache.org"),
        GOOGLE_GUAVA("Google", "Guava", "https://github.com/google/guava"),
        HENDRAANGGRIAN_KOTFX("Hendra Anggrian", "kotfx", "https://github.com/hendraanggrian/kotfx"),
        JETBRAINS_KOTLIN("JetBrains", "Kotlin", "http://kotlinlang.org"),
        JODAORG_JODA_TIME("JodaOrg", "Joda-Time", "www.joda.org/joda-time"),
        REACTIVEX_RXJAVAFX("ReactiveX", "RxJavaFX", "https://github.com/ReactiveX/RxJavaFX"),
        REACTIVEX_RXKOTLIN("ReactiveX", "RxKotlin", "https://github.com/ReactiveX/RxKotlin"),
        SLF4J_LOG4J12("Slf4j", "Log4j12", "https://www.slf4j.org");

        fun getContent(resourced: Resourced): String = resourced
                .getResourceAsStream("/${name.toLowerCase()}.txt")
                .use { return BufferedReader(InputStreamReader(it)).lines().collect(joining("\n")) }

        companion object : Listable<License> {
            override fun listAll(): ObservableList<License> = observableListOf(*values())
        }
    }
}