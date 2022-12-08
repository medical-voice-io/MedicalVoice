package io.medicalvoice.medicalvoiceservice.services.events

/** Интерфейс события */
sealed interface Event

/** Событие старта записи микрофона */
class StartRecordingEvent : Event

/** Событие остановки записи микрофона */
class StopRecordingEvent : Event