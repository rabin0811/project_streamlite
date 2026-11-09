package com.streamlite.app.util

import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashHandler {

    fun initialize(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logCrash(context, throwable)
            } catch (_: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
            Process.killProcess(Process.myPid())
        }
    }

    private fun logCrash(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val deviceInfo = buildString {
            appendLine("=== Crash Report ===")
            appendLine("Time: $timestamp")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("App Version: 1.0.0")
            appendLine()
            appendLine("=== Stack Trace ===")
            appendLine(stackTrace)
        }

        try {
            val crashDir = File(context.filesDir, "crashes")
            crashDir.mkdirs()
            val crashFile = File(crashDir, "crash_$timestamp.txt")
            crashFile.writeText(deviceInfo)
        } catch (_: Exception) {
        }
    }
}
