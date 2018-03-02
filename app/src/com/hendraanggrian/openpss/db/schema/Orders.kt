package com.hendraanggrian.openpss.db.schema

import com.hendraanggrian.openpss.db.dao.OffsetOrder
import com.hendraanggrian.openpss.db.dao.PlateOrder
import kotlinx.nosql.Discriminator
import kotlinx.nosql.double
import kotlinx.nosql.id
import kotlinx.nosql.integer
import kotlinx.nosql.mongodb.DocumentSchema
import kotlinx.nosql.string
import kotlin.reflect.KClass

sealed class Orders<D : Any, S : DocumentSchema<D>>(klass: KClass<D>, discriminator: String) : DocumentSchema<D>("order", klass, Discriminator(string("type"), discriminator)) {
    val total = double<S>("total")
}

object OffsetOrders : Orders<OffsetOrder, OffsetOrders>(OffsetOrder::class, "print")

object PlateOrders : Orders<PlateOrder, PlateOrders>(PlateOrder::class, "plate") {
    val plateId = id("plate_id", Plates)
    val qty = integer("qty")
    val price = double("price")
}