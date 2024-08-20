package com.blackbox.plog.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.annotation.Keep
import com.blackbox.plog.pLogs.PLog
import com.blackbox.plog.pLogs.models.LogLevel
import kotlin.system.exitProcess

@Keep
class AppExceptionHandler(
    val systemHandler: Thread.UncaughtExceptionHandler,
    val crashlyticsHandler: Thread.UncaughtExceptionHandler,
    application: Application
) : Thread.UncaughtExceptionHandler {

    private val TAG = "AppExceptionHandler"
    private var lastStartedActivity: Activity? = null

    private var startCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?
                ) {

                }

                override fun onActivityStarted(activity: Activity) {
                    startCount++
                    lastStartedActivity = activity
                }

                override fun onActivityResumed(activity: Activity) {

                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityStopped(activity: Activity) {
                    startCount--
                    if (startCount <= 0) {
                        lastStartedActivity = null
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

                }

                override fun onActivityDestroyed(activity: Activity) {

                }
            })
    }


    override fun uncaughtException(t: Thread?, e: Throwable) {

        PLog.logThis(
            TAG,
            "uncaughtException",
            info = "Thread: ${t?.name}, ${PLogUtils.getStackTrace(e)}",
            throwable = e,
            level = LogLevel.SEVERE,
            Thread.currentThread().name
        )

        lastStartedActivity?.let { activity ->
            val isRestarted = activity.intent
                .getBooleanExtra(RESTARTED, false)

            val lastException = activity.intent
                .getSerializableExtra(LAST_EXCEPTION) as Throwable?

            if (!isRestarted || !isSameException(e, lastException)) {
                killThisProcess {
                    // signal exception to be logged by crashlytics
                    crashlyticsHandler.uncaughtException(t, e)

                    val intent = activity.intent
                        .putExtra(RESTARTED, true)
                        .putExtra(LAST_EXCEPTION, e)
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        )

                    with(activity) {
                        finish()
                        startActivity(intent)
                    }
                }
            } else {
                killThisProcess { systemHandler.uncaughtException(t, e) }
            }
        } ?: killThisProcess {
            crashlyticsHandler.uncaughtException(t, e)
            systemHandler.uncaughtException(t, e)
        }
    }

    /**
     * Not bullet-proof, but it works well.
     */
    private fun isSameException(
        originalException: Throwable,
        lastException: Throwable?
    ): Boolean {
        if (lastException == null) return false

        return originalException.javaClass == lastException.javaClass &&
                originalException.stackTrace[0] == originalException.stackTrace[0] &&
                originalException.message == lastException.message
    }

    private fun killThisProcess(action: () -> Unit = {}) {
        action()

        android.os.Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    companion object {
        private const val RESTARTED = "appExceptionHandler_restarted"
        private const val LAST_EXCEPTION = "appExceptionHandler_lastException"
    }
}