package ro.wethecitizens.firstcontact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.wethecitizens.firstcontact.R

class MyAdapter(private val myDataset: List<objType>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

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
        var txt_date: TextView? = itemView.findViewById<View>(R.id.data_alerta) as TextView
        var txt_time: TextView? = itemView.findViewById<View>(R.id.timp_data_alerta) as TextView

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_alert_infectare , parent , false)
        // set the view's size, margins, paddings and layout parameters

            return MyViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.txt_date?.text = myDataset.get(position).getDate()
        holder.txt_time?.text = myDataset.get(position).getTime()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}