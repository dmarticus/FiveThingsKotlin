package alison.fivethingskotlin.API

import alison.fivethingskotlin.Models.FiveThings
import alison.fivethingskotlin.Models.Status
import alison.fivethingskotlin.Util.Resource
import alison.fivethingskotlin.Util.getDatabaseStyleDate
import alison.fivethingskotlin.Util.getDateFromDatabaseStyle
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class FiveThingsFirebaseRepository(private val user: FirebaseUser): FiveThingsRepository {

    private var database = FirebaseDatabase.getInstance().reference

    override fun getFiveThings(date: Date, fiveThingsData: MutableLiveData<Resource<FiveThings>>): LiveData<Resource<FiveThings>> {
        val formattedDate = getDatabaseStyleDate(date)
        val dateQuery = database.child("users").child(user.uid).child(formattedDate)
        Log.d("fivethings", "date query: " + dateQuery)
        dateQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e("fivethings", p0.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val results= dataSnapshot.value
                if (results != null) {
                    val things = results as ArrayList<String>
                    val fiveThings = FiveThings(date,
                            things[0],
                            things[1],
                            things[2],
                            things[3],
                            things[4],
                            true)
                    fiveThingsData.value = Resource(Status.SUCCESS, "", fiveThings)
                    Log.d("fivethings", "data set!")
                } else {
                    Log.d("fivethings", "no data found for this day")
                    val emptyDay = FiveThings(date, "", "", "", "", "", false)
                    fiveThingsData.value = Resource(Status.SUCCESS, "", emptyDay)
                }
            }
        })
        return fiveThingsData
    }

    override fun saveFiveThings(fiveThings: FiveThings, fiveThingsData: MutableLiveData<Resource<FiveThings>>) {
        val things = ArrayList<String>()
        things.add(fiveThings.one)
        things.add(fiveThings.two)
        things.add(fiveThings.three)
        things.add(fiveThings.four)
        things.add(fiveThings.five)

        val formattedDate = getDatabaseStyleDate(fiveThings.date)

        val child = database.child("users").child(user.uid).child(formattedDate)
        if (fiveThings.isEmpty) {
            //user hasn't written or has deleted a whole day
            child.setValue(null)
        } else {
            child.setValue(things) { error, ref ->
                if (error != null) {
                    fiveThings.saved = true
                    fiveThingsData.value = Resource(Status.SUCCESS, "", fiveThings)
                    Log.d("fivethings", "No error: " + ref)
                } else {
                    fiveThingsData.value = Resource(Status.ERROR, "error found", error)
                }
            }
        }
    }

    override fun getWrittenDates(): MutableLiveData<Resource<List<Date>>> {
        val fiveThingsDates = MutableLiveData<Resource<List<Date>>>()
        val query = database.child("users").child(user.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.e("fivethings", error.toString())
                fiveThingsDates.value = Resource(Status.ERROR, error.toString(), null)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val results= dataSnapshot.value as Map<String, List<String>>
                val dayStrings = results.keys
                Log.d("blerg", "dayStrings: " + dayStrings)
                val days = dayStrings.map { getDateFromDatabaseStyle(it) }
                fiveThingsDates.value = Resource(Status.SUCCESS, "", days)
            }
        })
        return fiveThingsDates
    }
}
