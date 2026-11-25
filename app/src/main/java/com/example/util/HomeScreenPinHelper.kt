package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

object HomeScreenPinHelper {
    const val ACTION_OPEN_SHAHGEE_CHAT = "com.example.ACTION_OPEN_SHAHGEE_CHAT"

    fun isPinSupported(context: Context): Boolean =
        ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    fun pinStudioToHomeScreen(context: Context): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val shortcut = ShortcutInfoCompat.Builder(context, "pin_aliraza_studio")
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel("ALiRaZa AI Studio V200")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun pinShahgeeChatToHomeScreen(context: Context): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }
        val chatIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_SHAHGEE_CHAT
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val shortcut = ShortcutInfoCompat.Builder(context, "pin_shahgee_chat")
            .setShortLabel("Shahgee Chat")
            .setLongLabel("Shahgee AI Chat")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher_round))
            .setIntent(chatIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
}
