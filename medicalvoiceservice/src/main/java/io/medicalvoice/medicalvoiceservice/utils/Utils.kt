package io.medicalvoice.medicalvoiceservice.utils

import kotlinx.coroutines.CancellationException

/**
 * Делает несколько попыток произвести определенное действие
 *
 * @param attempts кол-во попыток
 * @param delay задержка между попытками
 * @param block функция, которая должна выполниться
 */
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
