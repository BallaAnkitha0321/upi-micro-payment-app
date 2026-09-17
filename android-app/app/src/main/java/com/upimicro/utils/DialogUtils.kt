package com.upimicro.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import com.upimicro.ui.auth.LoginActivity

object DialogUtils {

    fun showBlockedDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Account Temporarily Restricted")
            .setMessage(
                "Your account access has been temporarily restricted due to security reasons.\n\n" +
                        "Please contact support for assistance."
            )
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // 🔥 FORCE LOGOUT
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)

                if (context is Activity) {
                    context.finish()
                }
            }
            .show()
    }
}