package io.medicalvoice.medicalvoiceservice.services.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.joinAll
import kotlin.coroutines.CoroutineContext

/** Отменяет все дочерние корутины */
suspend fun CoroutineContext.cancelChildrenAndJoin() {
    cancelChildren()
    get(Job)?.children?.toList()?.joinAll()
}

/** Получает Job конкретной корутины */
val CoroutineScope.contextJob get() = requireNotNull(coroutineContext[Job])