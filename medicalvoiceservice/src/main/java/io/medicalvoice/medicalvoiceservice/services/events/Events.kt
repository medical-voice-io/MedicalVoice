package io.medicalvoice.medicalvoiceservice.services.events

/** Интерфейс события */
sealed interface Event

/** Событие старта записи микрофона */
object StartRecordingEvent : Event

/** Событие остановки записи микрофона */
object StopRecordingEvent : Event