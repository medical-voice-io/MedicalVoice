package io.medicalvoice.medicalvoiceservice.domain

import androidx.annotation.DrawableRes

/**
 * Данные для уведомления
 *
 * @property channelId id канала для уведомления
 * @property channelName название канала для уведомления
 * @property title заголовок уведомления
 * @property text текст уведомления
 * @property smallIconRes иконка уведомления
 */
class NotificationData(
    val channelId: String,
    val channelName: String,
    val title: String,
    val text: String,
    @DrawableRes val smallIconRes: Int
)