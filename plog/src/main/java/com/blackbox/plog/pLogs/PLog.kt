package com.blackbox.plog.pLogs

/**
 * Created by Umair Adil on 12/04/2017.
 */

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import androidx.annotation.Keep
import com.blackbox.plog.dataLogs.DataLogger
import com.blackbox.plog.dataLogs.exporter.DataLogsExporter
import com.blackbox.plog.mqtt.MQTTSender
import com.blackbox.plog.mqtt.PLogMQTTProvider
import com.blackbox.plog.pLogs.events.EventTypes
import com.blackbox.plog.pLogs.events.LogEvents
import com.blackbox.plog.pLogs.exporter.ExportType
import com.blackbox.plog.pLogs.exporter.LogExporter
import com.blackbox.plog.pLogs.filter.PlogFilters
import com.blackbox.plog.pLogs.impl.AutoExportHelper
import com.blackbox.plog.pLogs.impl.PLogImpl
import com.blackbox.plog.pLogs.models.LogLevel
import com.blackbox.plog.pLogs.models.LogType
import com.blackbox.plog.pLogs.utils.LOG_FOLDER
import com.blackbox.plog.utils.RxBus
import com.blackbox.plog.utils.getLogsSavedPaths
import io.reactivex.Flowable
import io.reactivex.Observable
import java.io.File

@SuppressLint("StaticFieldLeak")
@Keep
object PLog : PLogImpl() {

    internal val TAG = PLogImpl.TAG
    internal val DEBUG_TAG = PLogImpl.DEBUG_TAG
    internal val handler = Handler()

    /**
     * Log this.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param text         the text
     */
    fun logThis(className: String, info: String) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                "",
                info,
                LogLevel.INFO
            )
            if (logsConfig.first) {
                writeLogsAsync(logsConfig.second, LogLevel.INFO)
            }
        }
        handler.post(runnable)
    }

    /**
     * Log this.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param text         the text
     */
    fun logThis(className: String, functionName: String, info: String) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                info,
                LogLevel.INFO
            )
            if (logsConfig.first) {
                writeLogsAsync(logsConfig.second, LogLevel.INFO)
            }
        }
        handler.post(runnable)
    }

    /**
     * Log this.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param text         the text
     * @param type         the type
     */
    fun logThis(
        className: String,
        functionName: String,
        info: String,
        level: LogLevel,
        threadId: String = Thread.currentThread().name
    ) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                info,
                level,
                threadId = threadId
            )

            if (logsConfig.first) {
                writeLogsAsync(logsConfig.second, level)
            }
        }
        handler.post(runnable)
    }

    /**
     * Log Exception.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param e             Exception
     * @param type         the type
     */
    fun logThis(
        className: String = "",
        functionName: String = "",
        info: String = "",
        throwable: Throwable,
        level: LogLevel = LogLevel.ERROR,
        threadId: String = Thread.currentThread().name
    ) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                info,
                level,
                throwable = throwable,
                threadId = threadId
            )
            if (logsConfig.first) {

                RxBus.send(LogEvents(EventTypes.NON_FATAL_EXCEPTION_REPORTED, throwable = throwable))

                val data = formatErrorMessage(info, throwable = throwable)

                writeLogsAsync(data, level)

            }
        }
        handler.post(runnable)
    }

    /**
     * Log Exception.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param e             Exception
     * @param type         the type
     */
    fun logThis(
        className: String = "",
        functionName: String = "",
        throwable: Throwable,
        level: LogLevel = LogLevel.ERROR,
        threadId: String = Thread.currentThread().name
    ) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                "",
                level,
                throwable = throwable,
                threadId = threadId
            )
            if (logsConfig.first) {

                RxBus.send(LogEvents(EventTypes.NON_FATAL_EXCEPTION_REPORTED, throwable = throwable))

                val data = formatErrorMessage("", throwable = throwable)

                writeLogsAsync(data, level)

            }
        }
        handler.post(runnable)
    }

    /**
     * Log Exception.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param e             Exception
     * @param type         the type
     */
    fun logThis(
        className: String = "",
        functionName: String = "",
        info: String = "",
        exception: Exception,
        level: LogLevel = LogLevel.ERROR,
        threadId: String = Thread.currentThread().name
    ) {

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                info,
                level,
                exception = exception,
                threadId = threadId
            )
            if (logsConfig.first) {

                RxBus.send(LogEvents(EventTypes.NON_FATAL_EXCEPTION_REPORTED, exception = exception))
                val data = formatErrorMessage(info, exception = exception)

                writeLogsAsync(data, level)

            }
        }
        handler.post(runnable)
    }

    /**
     * Log Exception.
     *
     * Logs 'String' data along with class & function name to hourly based file with formatted timestamps.
     *
     * @param className    the class name
     * @param functionName the function name
     * @param e             Exception
     * @param type         the type
     */
    fun logThis(
        className: String = "",
        functionName: String = "",
        exception: Exception,
        level: LogLevel = LogLevel.ERROR,
        threadId: String = Thread.currentThread().name
    ) {

        RxBus.send(LogEvents(EventTypes.NON_FATAL_EXCEPTION_REPORTED, exception = exception))

        val runnable = Runnable {
            val logsConfig = isLogsConfigValid(
                className,
                functionName,
                "",
                level,
                exception = exception,
                threadId = threadId
            )
            if (logsConfig.first) {
                val data = formatErrorMessage("", exception = exception)
                writeLogsAsync(data, level)
            }
        }
        handler.post(runnable)
    }

    /**
     * Gets logs.
     *
     * This will export logs based on filter type to export location with export name provided.
     *
     * @return the logs
     */
    fun exportDataLogsForName(name: String, exportDecrypted: Boolean = false): Observable<String> {

        PLogImpl.getConfig()?.let {

            val path = getLogsSavedPaths(it.nameForEventDirectory)
            return DataLogsExporter.getDataLogs(name, path, outputPath, exportDecrypted)
        }

        return returnDefaultObservableForNoConfig()
    }

    /**
     * Gets logs.
     *
     * This will export logs based on filter type to export location with export name provided.
     *
     * @return the logs
     */
    fun exportAllDataLogs(exportDecrypted: Boolean = false): Observable<String> {

        PLogImpl.getConfig()?.let {

            val path = getLogsSavedPaths(it.nameForEventDirectory, isForAll = true)
            return DataLogsExporter.getDataLogs("", path, outputPath, exportDecrypted)
        }

        return returnDefaultObservableForNoConfig()
    }

    /**
     * Gets logs.
     *
     * This will export logs as plain String.
     *
     * @return the String data
     */
    fun printDataLogsForName(name: String, printDecrypted: Boolean = false): Observable<String> {

        PLogImpl.getConfig()?.let {

            val path = getLogsSavedPaths(it.nameForEventDirectory)
            return DataLogsExporter.printLogsForName(name, path, printDecrypted)
        }

        return returnDefaultObservableForNoConfig()
    }

    private fun returnDefaultObservableForNoConfig(): Observable<String> {
        return Observable.create {

            if (!it.isDisposed) {
                it.onError(Throwable("No Logs configuration provided! Can not perform this action with logs configuration."))
            }
        }
    }

    /*
     * This will return 'DataLogger' for log type defined in Config File.
     */
    fun getLoggerFor(type: String): DataLogger? {

        if (PLog.isLogsConfigSet()) {
            if (PLog.logTypes.containsKey(type))
                return PLog.logTypes.get(type)

            return null
        } else {
            return null
        }
    }

    /**
     * Gets logs.
     *
     * This will export logs based on filter type to export location with export name provided.
     *
     * @param type the type
     * @return the logs
     */
    fun exportLogsForType(type: ExportType, exportDecrypted: Boolean = false,logType: String): Observable<String> {
        return LogExporter.getZippedLogs(type.type, exportDecrypted,logType)
    }

    /**
     * Gets logs.
     *
     * This will export logs based on filters to export location with export name provided.
     *
     * @param filters the filters for the files
     * @return the logs
     */
    fun exportLogsForFilters(filters: PlogFilters, exportDecrypted: Boolean = false): Observable<String> {
        return LogExporter.getZippedLogs(filters, exportDecrypted)
    }

    /**
     * Gets logs.
     *
     * This will export logs as plain String.
     *
     * @return the String data
     */
    fun printLogsForType(type: ExportType, printDecrypted: Boolean = false,logType: String): Flowable<String> {
        return LogExporter.printLogsForType(type.type, printDecrypted,logType)
    }

    /**
     * Clear all logs from storage directory.
     *
     */
    fun clearLogs() {
        val rootFolderName = LOG_FOLDER
        val rootFolderPath = PLog.logPath + rootFolderName + File.separator
        File(rootFolderPath).deleteRecursively()
        MQTTSender.clearSummaryValues()
    }

    /**
     * Clear all zipped loges from storage directory.
     *
     */
    fun clearExportedLogs() {
        File(outputPath).deleteRecursively()
    }

    private fun writeLogsAsync(dataToWrite: String, logLevel: LogLevel) {

        //Only write to local storage if this flag is set 'true'
        if (PLogMQTTProvider.writeLogsToLocalStorage) {

            try {
                SaveAsync(dataToWrite, logLevel).execute()
            } catch (e: Exception) {
                e.printStackTrace()

                //Write directly
                writeAndExportLog(dataToWrite, logLevel)
            }
        }
    }

    private class SaveAsync(var dataToWrite: String, var logLevel: LogLevel) : AsyncTask<String, String, Boolean>() {

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

            if (getConfig()?.isDebuggable!!) {

                if (dataToWrite.isNotEmpty()) {
                    if (logLevel == LogLevel.INFO) {
                        Log.i(Companion.TAG, dataToWrite)
                    } else {
                        Log.e(Companion.TAG, dataToWrite)
                    }
                }
            }

            //Check if log level is of Error
            AutoExportHelper.autoExportError(dataToWrite, logLevel)
        }

        override fun doInBackground(vararg p0: String?): Boolean {
            writeAndExportLog(dataToWrite, logLevel)
            return true
        }
    }
}
