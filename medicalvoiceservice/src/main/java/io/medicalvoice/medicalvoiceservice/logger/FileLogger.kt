package io.medicalvoice.medicalvoiceservice.logger

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

// Класс для логирования в файл
object FileLogger {
    private val sdfTime = SimpleDateFormat("HHmmddMMyyyy", Locale("ru"))

    private val scope = CoroutineScope(Dispatchers.IO)

    private val logMessageActor = scope.actor<FileLogMessage> {
        for (msg in channel)
            saveLogInFile(msg.logToSave, msg.directory, msg.throwable)
    }

    fun createDirIfNotExists(path: String): Boolean {
        var isSuccess = true
        val file =  File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toString() + "/" + path + "/"
        )
        if (!file.exists()) {
            try {
                !file.mkdirs()
            } catch (e: java.lang.Exception) {
                e.message?.let { Log.e("File :: ", it) }
                isSuccess = false
            }
        }
        return isSuccess
    }


    fun saveLog(tag: String, event: String, t: Throwable?) {
        if (t == null) {
            Log.v(tag, event)
        } else {
            Log.e(tag, event, t)
        }

        val currentMilliseconds = System.currentTimeMillis()
        val resultDate = Date(currentMilliseconds)
        val fileName = "med${sdfTime.format(resultDate)}.txt"
        writeLogToFile(fileName, event, t)
    }

    private fun writeLogToFile(fileName: String, note: String, throwable: Throwable?) {
        scope.launch {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + "MedicalVoice" + "/"
            )

            Log.i("File", fileName)
            if (createDirIfNotExists("MedicalVoice")) {
                val fileLogsDirectory = File(directory, fileName)
                withContext(Dispatchers.IO) {
                    fileLogsDirectory.createNewFile()
                }
                val logToSave = note
                logMessageActor.send(FileLogMessage(logToSave, fileLogsDirectory, throwable))
           }
        }
    }

    private suspend fun saveLogInFile(content: String, file: File, throwable: Throwable?) {
        withContext(Dispatchers.IO) {
            try {
                val pw = PrintWriter(FileWriter(file, true))
                pw.println(content)
                throwable?.printStackTrace(pw)
                pw.close()
            } catch (e: IOException) {
                Log.e("LogSystem", "Error writing to the log file", e)
            }
        }
    }
}