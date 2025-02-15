package com.apollographql.ijplugin.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.NotificationContent
import com.intellij.openapi.util.NlsContexts.NotificationTitle

@Suppress("UnstableApiUsage")
fun createNotification(
    @NotificationTitle title: String = "",
    @NotificationContent content: String,
    type: NotificationType,
    vararg actions: AnAction,
): Notification = NotificationGroupManager.getInstance()
    .getNotificationGroup("Apollo")
    .createNotification(title, content, type)
    .apply {
      for (action in actions) {
        addAction(action)
      }
    }

@Suppress("UnstableApiUsage")
fun showNotification(
    project: Project,
    @NotificationTitle title: String = "",
    @NotificationContent content: String,
    type: NotificationType,
    vararg actions: AnAction,
) = createNotification(
    title = title,
    content = content,
    type = type,
    actions = actions,
).notify(project)
