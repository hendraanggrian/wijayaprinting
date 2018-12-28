package com.hendraanggrian.openpss.api

import com.hendraanggrian.openpss.data.Log
import com.hendraanggrian.openpss.data.Page
import com.hendraanggrian.openpss.schema.Logs
import io.ktor.client.request.get

interface LogApi : Api {

    suspend fun getLogs(page: Int, count: Int): Page<Log> = client.get {
        apiUrl("$Logs")
        parameters(
            "page" to page,
            "count" to count
        )
    }
}