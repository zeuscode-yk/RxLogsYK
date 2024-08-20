package com.blackbox.plog.pLogs.formatter

import androidx.annotation.Keep
import com.blackbox.plog.pLogs.impl.PLogImpl
import com.blackbox.plog.pLogs.models.LogData

/**
 * Created by umair on 03/01/2018.
 */

@Keep
object LogFormatter {

    private fun formatCurly(data: LogData): String {

        val SCREEN = data.className
        val FUNCTION = data.functionName
        val DATA = data.logText
        val TIME = data.logTime
        val TYPE = data.logType

        return "{$SCREEN}  {$FUNCTION}  {$DATA}  {$TIME}  {$TYPE}\n"
    }

    private fun formatVBar(data: LogData): String {

        val SCREEN = data.className
        val FUNCTION = data.functionName
        val DATA = data.logText
        val TIME = data.logTime
        val TYPE = data.logType
        val THREADID = data.threadId

        return "$TYPE | $THREADID | $TIME | $SCREEN | $FUNCTION | $DATA\n"
    }

    private fun formatSquare(data: LogData): String {

        val SCREEN = data.className
        val FUNCTION = data.functionName
        val DATA = data.logText
        val TIME = data.logTime
        val TYPE = data.logType

        return "[" + SCREEN + "]  [" + FUNCTION + "]  [" + DATA + "]  {" +
                "[" + TIME + "]  [" + TYPE + "]\n"
    }

    private fun formatCSV(data: LogData, deliminator: String): String {

        val SCREEN = data.className
        val FUNCTION = data.functionName
        val DATA = data.logText
        val TIME = data.logTime
        val TYPE = data.logType

        return SCREEN + deliminator + FUNCTION + deliminator + DATA + deliminator + TIME + deliminator + TYPE + "\n"
    }

    private fun formatCustom(data: LogData, dividerOpen: String, dividerClose: String): String {

        val SCREEN = data.className
        val FUNCTION = data.functionName
        val DATA = data.logText
        val TIME = data.logTime
        val TYPE = data.logType

        return dividerOpen + SCREEN + dividerClose + dividerOpen + FUNCTION + dividerClose + dividerOpen + DATA + dividerClose + dividerOpen +
                TIME + dividerClose + dividerOpen + TYPE + dividerClose + "\n"
    }

    internal fun getFormatType(data: LogData): String {

        var t = formatCurly(data)

        PLogImpl.getConfig()?.let {

            t = when (it.formatType) {
                FormatType.FORMAT_CURLY -> formatCurly(data)

                FormatType.FORMAT_SQUARE -> formatSquare(data)

                FormatType.FORMAT_VBAR -> formatVBar(data)

                FormatType.FORMAT_CSV -> formatCSV(data, PLogImpl.getConfig()?.csvDelimiter!!)

                FormatType.FORMAT_CUSTOM -> formatCustom(data, PLogImpl.getConfig()?.customFormatOpen!!, PLogImpl.getConfig()?.customFormatClose!!)
            }
        }

        return t
    }
}
