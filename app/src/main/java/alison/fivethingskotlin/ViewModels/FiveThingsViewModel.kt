package alison.fivethingskotlin.ViewModels

import alison.fivethingskotlin.API.repository.FiveThingsRepositoryImpl
import alison.fivethingskotlin.Models.FiveThingz
import alison.fivethingskotlin.Util.Resource
import alison.fivethingskotlin.Util.getNextDate
import alison.fivethingskotlin.Util.getPreviousDate
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import java.util.*

class FiveThingsViewModel(val token: String) : ViewModel() {

    private val fiveThingsData = MutableLiveData<Resource<FiveThingz>>()
    private val fiveThingsSource = FiveThingsRepositoryImpl()

    fun getFiveThings(date: Date): LiveData<Resource<FiveThingz>> {
        return fiveThingsSource.getFiveThings(token, date, fiveThingsData)
    }

    fun onEditText() {
        val fiveThings = fiveThingsData.value
        fiveThings?.data?.edited = true
        fiveThingsData.value = fiveThings
    }

    fun writeFiveThings(fiveThings: FiveThingz): LiveData<Resource<List<Date>>> {
        return fiveThingsSource.saveFiveThings(token, fiveThings, fiveThingsData)
    }

    fun getToday(): LiveData<Resource<FiveThingz>> {
        return getFiveThings(Date())
    }

    fun getPreviousDay(date: Date): LiveData<Resource<FiveThingz>> {
        val prevDate = getPreviousDate(date)
        return getFiveThings(prevDate)
    }

    fun getNextDay(date: Date): LiveData<Resource<FiveThingz>>  {
        val nextDate = getNextDate(date)
        return getFiveThings(nextDate)
    }

    fun changeDate(date: Date): LiveData<Resource<FiveThingz>> {
        return getFiveThings(date)
    }

    fun getWrittenDays(): LiveData<Resource<List<Date>>> {
        return fiveThingsSource.getWrittenDates(token)
    }
}