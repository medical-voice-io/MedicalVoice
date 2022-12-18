package io.medicalvoice.medicalvoiceservice.logger

import java.io.File

// Класс данных для логирования
data class FileLogMessage(val logToSave: String, val directory: File, val throwable: Throwable?)
