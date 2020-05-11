package ro.wethecitizens.firstcontact

import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val inflater = layoutInflater
        val alertDialog = inflater.inflate(R.layout.activity_alert_dialog,null)
        val alert : AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertDialog)
        alert.setCancelable(false)
        val dialog = alert.create()
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG)
        dialog.show()
    }
}