package io.medicalvoice.medicalvoiceservice.utils

import kotlinx.coroutines.CancellationException

suspend inline fun <T> retry(attempts: Int, delay: Long = 0, block: (attempt: Int) -> T): T {
    check(attempts > 0)
    for (attempt in 1..attempts) {
        try {
            return block(attempt)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            if (attempt == attempts) throw error
        }
        if (delay > 0) kotlinx.coroutines.delay(delay)
    }
    throw IllegalStateException()
}
