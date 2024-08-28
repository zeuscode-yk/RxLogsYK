package com.blackbox.plog.pLogs.exporter

import android.util.Log
import com.blackbox.plog.pLogs.PLog
import com.blackbox.plog.pLogs.filter.FileFilter
import com.blackbox.plog.pLogs.filter.FilterUtils
import com.blackbox.plog.pLogs.filter.FilterUtils.getPathForType
import com.blackbox.plog.pLogs.filter.PlogFilters
import com.blackbox.plog.pLogs.impl.PLogImpl
import java.io.File
import kotlin.math.log

private val path = PLog.logPath
private var timeStamp = ""
private var noOfFiles = ""

private val TAG = "ExportTypes"

/*
 * Get logs for export type.
 */
internal fun getFilesForRequestedType(type: String,logType: String): Triple<String, List<File>, String> {
    when (type) {

        ExportType.TODAY.type -> {
            return getLogsForToday(logType)
        }

        ExportType.LAST_HOUR.type -> {
            return getLogsForLastHour(logType)
        }

        ExportType.WEEKS.type -> {
            return getLogsForWeek(logType)
        }

        ExportType.LAST_24_HOURS.type -> {
            return getLogsForLast24Hours(logType)
        }

        ExportType.ALL.type -> {
            return getLogsForAllInRoot(logType)
        }
    }

    return Triple("", arrayListOf(), "")
}

/*
 * Get file path of logs for 'Today'
 */
private fun getLogsForToday(logType: String): Triple<String, List<File>, String> {

    val path = getPathForType(ExportType.TODAY, logType)
    val files = FileFilter.getFilesForToday(path)
    val zipName = composeZipName(files, ExportType.TODAY)

    Log.i(TAG,"getLogsForToday: Path: $path, Files: ${files.first.size}")
    return Triple(zipName, files.first, files.second)
}



/*
 * Get file path of logs for Custom dates
 */
fun getLogsForCustomFilter(filters: PlogFilters): Triple<String, List<File>, String> {
    val allFiles = mutableListOf<File>()
    var tempOutfile = ""
    filters.dates.forEach { date ->
        val path = FilterUtils.rootFolderPath + date
        var fileNames = filters.files + filters.hours.map {h -> "$date$h" }
        val files = FileFilter.getFilesForDate(path, fileNames)
        tempOutfile = files.third
        allFiles.addAll(files.first)
    }

    val zipName = composeZipName(allFiles, "custom")
    Log.i(TAG,"getLogsForCustomFilter: Path: $path, Files: ${allFiles.size}")
    return Triple(zipName, allFiles, tempOutfile)
}


/*
 * Get file path of logs for 'Last Hour'
 */
private fun getLogsForLastHour(logType: String): Triple<String, List<File>, String> {

    val path = getPathForType(ExportType.LAST_HOUR, logType)
    val files = FileFilter.getFilesForLastHour(path)
    val zipName = composeZipName(files, ExportType.LAST_HOUR)

    Log.i(TAG,"getLogsForLastHour: Path: $path, Files: ${files.first.size}")
    return Triple(zipName, files.first, files.second)
}

/*
 * Get file path of logs for 'Week'
 */
private fun getLogsForWeek(logType: String): Triple<String, List<File>, String> {

    val path = getPathForType(ExportType.WEEKS, logType)
    val files = FileFilter.getFilesForLastWeek(path)
    val zipName = composeZipName(files, ExportType.WEEKS)

    Log.i(TAG,"getLogsForWeek: Path: $path, Files: ${files.first.size}")
    return Triple(zipName, files.first, files.second)
}

/*
 * Get file path of logs for '24 Hours'
 */
private fun getLogsForLast24Hours(logType: String): Triple<String, List<File>, String> {

    val path = getPathForType(ExportType.LAST_24_HOURS, logType )
    val files = FileFilter.getFilesForLast24Hours(path)
    val zipName = composeZipName(files, ExportType.LAST_24_HOURS)

    Log.i(TAG,"getLogsForLast24Hours: Path: $path, Files: ${files.first.size}")
    return Triple(zipName, files.first, files.second)
}

/*
 * Get file path of logs for All logs in root directory.
 */
private fun getLogsForAllInRoot(logType: String): Triple<String, List<File>, String> {

    val path = getPathForType(ExportType.ALL,logType)
    val files = FileFilter.getFilesForAll(path)
    val zipName = composeZipName(files, ExportType.ALL)

    Log.i(TAG,"getLogsForAllInRoot: Path: $path, Files: ${files.first.size}")
    return Triple(zipName, files.first, files.second)
}

private fun composeZipName(files: Pair<List<File>, String>, exportType: ExportType): String {

    if (PLogImpl.getConfig()?.attachTimeStamp!!)
        timeStamp = PLog.getTimeStampForOutputFile() + "_" + exportType.type

    if (PLogImpl.getConfig()?.attachNoOfFiles!!)
        noOfFiles = "_[${files.first.size}]"

    val preName = PLogImpl.getConfig()?.exportFileNamePreFix!!
    val zName = PLogImpl.getConfig()?.zipFileName!!
    val postName = PLogImpl.getConfig()?.exportFileNamePostFix!!

    return "$preName$zName$timeStamp$noOfFiles$postName.zip"
}

private fun composeZipName(files: List<File>, name: String): String {

    if (PLogImpl.getConfig()?.attachTimeStamp!!)
        timeStamp = PLog.getTimeStampForOutputFile() + "_" + name

    if (PLogImpl.getConfig()?.attachNoOfFiles!!)
        noOfFiles = "_[${files.size}]"

    val preName = PLogImpl.getConfig()?.exportFileNamePreFix!!
    val zName = PLogImpl.getConfig()?.zipFileName!!
    val postName = PLogImpl.getConfig()?.exportFileNamePostFix!!

    return "$preName$zName$timeStamp$noOfFiles$postName.zip"
}