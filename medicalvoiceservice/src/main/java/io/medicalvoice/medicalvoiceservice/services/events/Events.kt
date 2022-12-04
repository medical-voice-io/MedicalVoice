package io.medicalvoice.medicalvoiceservice.services.events

sealed interface Event

class StartRecordingEvent : Event

class StopRecordingEvent : Event