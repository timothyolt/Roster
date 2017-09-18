package checkin.timothyolt.com.roster

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.*

//TODO does not take into account false keys
class PersonAdapter(private var shallow: Query, private var deep: DatabaseReference) :
        RecyclerView.Adapter<PersonAdapter.PersonView>(), /*ValueEventListener,*/ ChildEventListener {

    private val people: ArrayList<String> = ArrayList(0)
    private fun indexOfFirst(key: String) : Int = people.indexOfFirst { it == key }

    init {
        shallow.addChildEventListener(this)
        notifyDataSetChanged()
    }

    fun update(shallow: Query, deep: DatabaseReference) {
        this.shallow.removeEventListener(this as ChildEventListener)
        people.clear()
        this.shallow = shallow
        this.deep = deep
        shallow.addChildEventListener(this)
        notifyDataSetChanged()
    }

    fun cleanup() {
        this.shallow.removeEventListener(this as ChildEventListener)
    }

    override fun onCancelled(error: DatabaseError?) {
        FirebaseCrash.report(error?.toException())
    }

    override fun onChildMoved(snapshot: DataSnapshot?, previousChildKey: String?) {
        val personId = snapshot?.key ?: return
        val oldIndex = indexOfFirst(personId)
        people.removeAt(oldIndex)
        val index = if (previousChildKey == null) 0 else indexOfFirst(previousChildKey) + 1
        people.add(index, personId)
        notifyItemMoved(oldIndex, index)
    }

    override fun onChildChanged(snapshot: DataSnapshot?, previousChildKey: String?) {
        val index = if (previousChildKey == null) 0 else indexOfFirst(previousChildKey) + 1
        val personId = snapshot?.key ?: return
        people[index] = personId
        notifyItemChanged(index)
    }

    override fun onChildAdded(snapshot: DataSnapshot?, previousChildKey: String?) {
        val personId = snapshot?.key ?: return
        val index = if (previousChildKey == null) 0 else indexOfFirst(previousChildKey) + 1
        people.add(index, personId)
        notifyItemInserted(index)
    }

    override fun onChildRemoved(snapshot: DataSnapshot?) {
        val personId = snapshot?.key ?: return
        val oldIndex = indexOfFirst(personId)
        people.removeAt(oldIndex)
        notifyItemRemoved(oldIndex)
    }

    override fun getItemCount(): Int = people.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PersonView =
            PersonView(LayoutInflater.from(parent!!.context).inflate(R.layout.view_person, parent, false))

    override fun onBindViewHolder(holder: PersonView?, position: Int) {
        holder?.bind(deep.child(people[position]))
    }

    override fun onViewRecycled(holder: PersonView?) {
        holder?.recycle()
    }

    class PersonView(itemView: View?) : RecyclerView.ViewHolder(itemView), ValueEventListener {
        private val name: TextView? = itemView?.findViewById(R.id.person_name_text)
        private val netid: TextView? = itemView?.findViewById(R.id.person_netid_text)

        private var deep: Query? = null

        fun bind(deep: Query?) {
            this.deep = deep
            deep?.addValueEventListener(this)
        }

        override fun onCancelled(error: DatabaseError?) {
            FirebaseCrash.report(error?.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            val person = snapshot?.getValue(Person::class.java)
            name?.text = "${person?.firstName} ${person?.lastName}"
            netid?.text = person?.netId
        }

        fun recycle() {
            deep?.removeEventListener(this)
        }
    }
}
