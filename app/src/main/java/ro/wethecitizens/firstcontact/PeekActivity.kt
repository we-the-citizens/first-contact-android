// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.database_peek.*
import ro.wethecitizens.firstcontact.adapter.RecordListAdapter
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage
import ro.wethecitizens.firstcontact.streetpass.view.RecordViewModel
import ro.wethecitizens.firstcontact.utils.Utils


class PeekActivity : AppCompatActivity() {

    private lateinit var viewModel: RecordViewModel
    private var recordsFound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newPeek()
    }

    private fun newPeek() {
        setContentView(R.layout.database_peek)
        val adapter =
            RecordListAdapter(this)
        recyclerview.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            recyclerview.context,
            layoutManager.orientation
        )
        recyclerview.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        viewModel.allRecords.observe(this, Observer { records ->
            adapter.setSourceData(records)
            if(records.size == 0)
                tv_noRecords.visibility = View.VISIBLE;
            else
                tv_noRecords.visibility = View.INVISIBLE;
        })

        expand.setOnClickListener {
            viewModel.allRecords.value?.let {
                adapter.setMode(RecordListAdapter.MODE.ALL)
            }
        }

        collapse.setOnClickListener {
            viewModel.allRecords.value?.let {
                adapter.setMode(RecordListAdapter.MODE.COLLAPSE)
            }
        }


        start.setOnClickListener {
            startService()
        }

        stop.setOnClickListener {
            stopService()
        }

        delete.setOnClickListener { view ->
            view.isEnabled = false

            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("Are you sure?")
                .setCancelable(false)
                .setMessage("Deleting the DB records is irreversible")
                .setPositiveButton("DELETE") { dialog, which ->
                    Observable.create<Boolean> {
                        StreetPassRecordStorage(this).nukeDb()
                        it.onNext(true)
                    }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe { result ->
                            Toast.makeText(this, "Database nuked: $result", Toast.LENGTH_SHORT)
                                .show()
                            view.isEnabled = true
                            dialog.cancel()
                        }
                }

                .setNegativeButton("DON'T DELETE") { dialog, which ->
                    view.isEnabled = true
                    dialog.cancel()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

        plot.setOnClickListener { view ->
            val intent = Intent(this, PlotActivity::class.java)
            intent.putExtra("time_period", nextTimePeriod())
            startActivity(intent)
        }

        val serviceUUID = BuildConfig.BLE_SSID
        val txt = "SSID: " + serviceUUID.substring(serviceUUID.length - 4)
        info.text = txt

        if (!BuildConfig.DEBUG) {
            start.visibility = View.GONE
            stop.visibility = View.GONE
            delete.visibility = View.GONE
        }
    }

    private var timePeriod: Int = 0

    private fun nextTimePeriod(): Int {
        timePeriod = when (timePeriod) {
            1 -> 3
            3 -> 6
            6 -> 12
            12 -> 24
            else -> 1
        }

        return timePeriod
    }


    private fun startService() {
        Utils.startBluetoothMonitoringService(this)
    }

    private fun stopService() {
        Utils.stopBluetoothMonitoringService(this)
    }

}
