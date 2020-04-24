package ro.wethecitizens.firstcontact.services

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class PeriodicallyDownloadCommandHandler(val service: WeakReference<PeriodicallyDownloadService>) : Handler() {
    override fun handleMessage(msg: Message?) {
        msg?.let {
            //            val cmd = msg.arg1
            val cmd = msg.what
            service.get()?.runService(PeriodicallyDownloadService.Command.findByValue(cmd))
        }
    }

    fun sendCommandMsg(cmd: PeriodicallyDownloadService.Command, delay: Long) {
//        val msg = obtainMessage(cmd.index)
        val msg = Message.obtain(this, cmd.index)
//        msg.arg1 = cmd.index
        sendMessageDelayed(msg, delay)
    }

    fun sendCommandMsg(cmd: PeriodicallyDownloadService.Command) {
        val msg = obtainMessage(cmd.index)
        msg.arg1 = cmd.index
        sendMessage(msg)
    }

    fun startPeriodicallyDownloadService() {
        sendCommandMsg(PeriodicallyDownloadService.Command.ACTION_START)
    }

    fun scheduleNextScan(timeInMillis: Long) {
        cancelNextScan()
        sendCommandMsg(PeriodicallyDownloadService.Command.ACTION_SCAN, timeInMillis)
    }

    fun cancelNextScan() {
        removeMessages(PeriodicallyDownloadService.Command.ACTION_SCAN.index)
    }

    fun hasScanScheduled(): Boolean {
        return hasMessages(PeriodicallyDownloadService.Command.ACTION_SCAN.index)
    }
}
