package com.example.financetracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.financetracker.MainActivity
import com.example.financetracker.R

/**
 * Helper class for managing notifications
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_BUDGET = "budget_notifications"
        private const val CHANNEL_ID_REMINDER = "reminder_notifications"
        private const val NOTIFICATION_ID_BUDGET = 1001
        private const val NOTIFICATION_ID_REMINDER = 1002
        private const val TAG = "NotificationHelper"
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Creates notification channels for Android 8.0 (API level 26) and higher
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget alerts channel
            val budgetChannel = NotificationChannel(
                CHANNEL_ID_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about budget status"
            }
            
            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to add expenses"
            }
            
            // Register the channels
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }
    
    /**
     * Checks if notification permission is granted
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Older versions don't need runtime permission for notifications
        }
    }
    
    /**
     * Shows a budget alert notification
     * @return True if notification was sent, false if permission denied
     */
    fun showBudgetNotification(percentage: Int, isExceeded: Boolean): Boolean {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification permission denied for budget alert")
            return false
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = if (isExceeded) {
            context.getString(R.string.budget_exceeded)
        } else {
            context.getString(R.string.budget_alert)
        }
        
        val message = if (isExceeded) {
            "You have exceeded your monthly budget."
        } else {
            context.getString(R.string.budget_warning, percentage)
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_BUDGET, notification)
                return true
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to send budget notification", e)
                return false
            }
        }
    }
    
    /**
     * Shows a reminder notification to add expenses
     * @return True if notification was sent, false if permission denied
     */
    fun showReminderNotification(): Boolean {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification permission denied for reminder")
            return false
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Expense Reminder")
            .setContentText("Don't forget to record your expenses for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_REMINDER, notification)
                return true
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to send reminder notification", e)
                return false
            }
        }
    }
} 