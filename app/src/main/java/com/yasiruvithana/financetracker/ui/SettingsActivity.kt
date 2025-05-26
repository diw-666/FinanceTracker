package com.yasiruvithana.financetracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.data.FileManager
import com.yasiruvithana.financetracker.data.NotificationHelper
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.ActivitySettingsBinding
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var fileManager: FileManager
    private lateinit var notificationHelper: NotificationHelper

    // Permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            binding.switchBudgetAlerts.isChecked = false
            binding.switchReminders.isChecked = false
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)
        fileManager = FileManager(this)
        notificationHelper = NotificationHelper(this)

        setupCurrencyDropdown()
        setupNotificationSwitches()
        setupNotificationTestButtons()
        setupDataButtons()
        setupBackupPathInfo()
    }

    private fun setupCurrencyDropdown() {
        // Currency options
        val currencies = arrayOf("$", "€", "£", "₹", "¥", "₩", "R")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencies)
        binding.dropdownCurrency.setAdapter(adapter)

        // Set current currency
        binding.dropdownCurrency.setText(prefManager.getCurrency(), false)

        // Save when changed
        binding.dropdownCurrency.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            prefManager.setCurrency(selectedCurrency)
            Toast.makeText(this, "Currency updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNotificationSwitches() {
        // Set current notification settings
        binding.switchBudgetAlerts.isChecked = prefManager.areBudgetAlertsEnabled()
        binding.switchReminders.isChecked = prefManager.isReminderEnabled()
        
        // Handle budget alerts switch
        binding.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !hasNotificationPermission()) {
                requestNotificationPermission()
            }
            prefManager.setBudgetAlertsEnabled(isChecked)
        }
        
        // Handle reminders switch
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !hasNotificationPermission()) {
                requestNotificationPermission()
            }
            prefManager.setReminderEnabled(isChecked)
        }
    }
    
    private fun setupNotificationTestButtons() {
        // Set up test budget alert button
        binding.buttonTestBudgetAlert.setOnClickListener {
            if (!hasNotificationPermission()) {
                Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
                requestNotificationPermission()
                return@setOnClickListener
            }
            
            val isSuccess = notificationHelper.showBudgetNotification(85, false)
            if (isSuccess) {
                Toast.makeText(this, "Budget alert notification sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send notification", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up test reminder button
        binding.buttonTestReminder.setOnClickListener {
            if (!hasNotificationPermission()) {
                Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
                requestNotificationPermission()
                return@setOnClickListener
            }
            
            val isSuccess = notificationHelper.showReminderNotification()
            if (isSuccess) {
                Toast.makeText(this, "Reminder notification sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send notification", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Older versions don't need runtime permission for notifications
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupDataButtons() {
        // Backup button
        binding.buttonBackup.setOnClickListener {
            backupData()
        }
        
        // Restore button
        binding.buttonRestore.setOnClickListener {
            showRestoreDialog()
        }
    }
    
    private fun backupData() {
        try {
            val transactions = prefManager.getTransactions()
            if (transactions.isEmpty()) {
                Toast.makeText(this, "No transactions to backup", Toast.LENGTH_SHORT).show()
                return
            }
            
            val backupPath = fileManager.exportData(transactions)
            Toast.makeText(
                this,
                getString(R.string.export_success) + ": $backupPath",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_occurred) + ": ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showRestoreDialog() {
        val backupFiles = fileManager.getBackupFiles()
        
        if (backupFiles.isEmpty()) {
            Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Format backup file names with date
        val fileNames = backupFiles.map { file ->
            val date = file.name.substringAfter("backup_").substringBefore(".json")
            "Backup from $date"
        }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select Backup to Restore")
            .setItems(fileNames) { _, which ->
                restoreData(backupFiles[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun restoreData(file: File) {
        try {
            val transactions = fileManager.importData(file.absolutePath)
            prefManager.saveTransactions(transactions)
            
            Toast.makeText(
                this,
                getString(R.string.import_success),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_occurred) + ": ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupBackupPathInfo() {
        // Show backup folder path
        val backupFolderPath = filesDir.absolutePath
        binding.textBackupPath.text = getString(R.string.backup_folder, backupFolderPath)
        
        // Set up click listener to copy the path to the clipboard
        binding.textBackupPath.setOnClickListener {
            // Copy to clipboard
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = android.content.ClipData.newPlainText("Backup Folder Path", backupFolderPath)
            clipboardManager.setPrimaryClip(clipData)
            
            // Show toast to confirm
            Toast.makeText(this, getString(R.string.path_copied), Toast.LENGTH_SHORT).show()
        }
    }
}