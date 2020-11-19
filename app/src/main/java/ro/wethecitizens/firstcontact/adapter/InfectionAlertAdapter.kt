// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.infectionalert.persistence.InfectionAlertRecord
import java.text.SimpleDateFormat
import java.util.*

class InfectionAlertAdapter(private val myDataset: List<InfectionAlertRecord>) :
    RecyclerView.Adapter<InfectionAlertAdapter.MyViewHolder>() {

    var body: String = ""

    class objType {
        private var date: String? = null
        private var time: String? = null

        constructor(date: String?, time: String?) {
            this.date = date
            this.time = time
        }


        fun getDate(): String? {
            return date
        }

        fun getTime(): String? {
            return time
        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var txt_body: AppCompatTextView? = itemView.findViewById<View>(R.id.alert_body) as AppCompatTextView
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): InfectionAlertAdapter.MyViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_infection_alert , parent , false)

        body = parent.context.getString(R.string.exposure_body)

        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var text = body.replace("#", "<b>" + formatCalendar(myDataset.get(position).exposureDate) + "</b>");
        text = text.replace("$", "<b>" + myDataset.get(position).exposureInMinutes.toString() + " min</b>");

        holder.txt_body?.setText(Html.fromHtml(text));
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun formatCalendar(c: Calendar): String {
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
            sdf.timeZone = TimeZone.getTimeZone("CET")
            return sdf.format(c.time)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}