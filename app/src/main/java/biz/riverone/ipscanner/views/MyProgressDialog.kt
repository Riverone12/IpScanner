package biz.riverone.ipscanner.views

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.widget.ProgressBar
import android.widget.TextView
import biz.riverone.ipscanner.R

/**
 * MyProgressDialog.kt: プログレスダイアログ
 * Created by kawahara on 2018/02/08.
 */
class MyProgressDialog : DialogFragment() {
    companion object {
        private const val DELAY_MILLISECOND: Long = 450
        private const val SHOW_MIN_MILLISECOND: Long = 300
        private const val ARG_KEY_MESSAGE = "message"

        fun create(mainThreadHandler: Handler, message: String): MyProgressDialog {
            val dialog = MyProgressDialog()
            dialog.mainThreadHandler = mainThreadHandler

            val arguments = Bundle()
            arguments.putString(ARG_KEY_MESSAGE, message)
            dialog.arguments = arguments
            return dialog
        }
    }

    private var progressBar: ProgressBar? = null
    private var progressMessage: TextView? = null
    private var startedShowing: Boolean = false
    private var startMilliSecond: Long = 0
    private var stopMilliSecond: Long = 0

    private var message: String = ""

    private var mainThreadHandler: Handler? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        message = arguments.getString(ARG_KEY_MESSAGE)

        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_my_progress, null))
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        progressBar = dialog.findViewById(R.id.progress)
        progressMessage = dialog.findViewById(R.id.progress_message)
        progressMessage?.text = message
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        startMilliSecond = System.currentTimeMillis()
        startedShowing = false
        stopMilliSecond = Long.MAX_VALUE

        mainThreadHandler?.postDelayed({
            if (stopMilliSecond > System.currentTimeMillis()) {
                showDialogAfterDelay(manager, tag)
            }
        }, DELAY_MILLISECOND)
    }

    private fun showDialogAfterDelay(manager: FragmentManager?, tag: String?) {
        startedShowing = true
        super.show(manager, tag)
    }

    fun cancel() {
        stopMilliSecond = System.currentTimeMillis()

        if (startedShowing) {
            if (progressBar != null) {
                cancelWhenShowing()
            } else {
                cancelWhenNotShowing()
            }
        }
    }

    private fun cancelWhenShowing() {
        if (stopMilliSecond < startMilliSecond + DELAY_MILLISECOND + SHOW_MIN_MILLISECOND) {
            mainThreadHandler?.postDelayed({
                dismissAllowingStateLoss()
            }, SHOW_MIN_MILLISECOND)
        } else {
            dismissAllowingStateLoss()
        }
    }

    private fun cancelWhenNotShowing() {
        mainThreadHandler?.postDelayed({
            dismissAllowingStateLoss()
        }, SHOW_MIN_MILLISECOND)
    }
}