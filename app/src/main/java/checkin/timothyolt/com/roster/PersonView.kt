package checkin.timothyolt.com.roster

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class PersonView(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    private val name: TextView? = itemView?.findViewById(R.id.person_name_text)
    private val netid: TextView? = itemView?.findViewById(R.id.person_netid_text)

    fun bind(person: Person?) {
        name?.text = "${person?.firstName} ${person?.lastName}"
        netid?.text = person?.netId
    }
}
