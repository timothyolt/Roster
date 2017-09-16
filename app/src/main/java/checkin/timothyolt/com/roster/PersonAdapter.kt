package checkin.timothyolt.com.roster

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.*
import org.jetbrains.annotations.NotNull

class PersonAdapter : RecyclerView.Adapter<PersonAdapter.PersonView>(), ValueEventListener, ChildEventListener {

    private val people: ArrayList<Person> = ArrayList(0)
    private val index: HashMap<String, Int> = HashMap()

    private var shallowQueryBehind: Query? = null
    var shallowQuery: Query?
        get() = shallowQueryBehind
        set(@NotNull value) {
            assert(value != null)
            shallowQueryBehind?.removeEventListener(this as ChildEventListener)
            people.clear()
            index.clear()
            notifyDataSetChanged()
            value?.addListenerForSingleValueEvent(this)
            value?.addChildEventListener(this)
            //shallowQueryBehind = value.orderByKey()
        }

    override fun onDataChange(p0: DataSnapshot?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCancelled(p0: DatabaseError?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildRemoved(p0: DataSnapshot?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PersonView {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: PersonView?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onViewRecycled(holder: PersonView?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class PersonView(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView? = itemView?.findViewById(R.id.person_name_text)
        private val netid: TextView? = itemView?.findViewById(R.id.person_netid_text)

        fun bind(person: Person?) {
            name?.text = "${person?.firstName} ${person?.lastName}"
            netid?.text = person?.netId
        }
    }
}

