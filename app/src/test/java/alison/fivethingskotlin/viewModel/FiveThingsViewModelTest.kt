package alison.fivethingskotlin.viewModel

import alison.fivethingskotlin.API.repository.FiveThingsRepository
import alison.fivethingskotlin.LiveDataTestUtil
import alison.fivethingskotlin.Models.FiveThings
import alison.fivethingskotlin.Models.Status
import alison.fivethingskotlin.Util.Resource
import alison.fivethingskotlin.Util.getPreviousDate
import alison.fivethingskotlin.ViewModels.FiveThingsViewModel
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldEqual
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyString
import java.util.*

@RunWith(JUnit4::class)
class FiveThingsViewModelTest {

    private lateinit var repository: FiveThingsRepository
    private lateinit var viewModel: FiveThingsViewModel

    private val token = "Token"
    private lateinit var date: Date
    private lateinit var dates: List<Date>
    private lateinit var fiveThings: FiveThings
    private val fiveThingsLiveData = MutableLiveData<Resource<FiveThings>>()
    private val datesLiveData = MutableLiveData<Resource<List<Date>>>()
    private lateinit var expectedFiveThingsResource: Resource<FiveThings>
    private lateinit var expectedDatesResource: Resource<List<Date>>


    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = mock()
        viewModel = FiveThingsViewModel(token, repository)

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2017)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        date = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 23)
        val nextDate = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 24)
        val nextNextDate = cal.time

        dates = listOf(date, nextDate, nextNextDate)
        fiveThings =  FiveThings(date, listOf("one", "two", "three", "four", "five"), false, true)

        //set up mocks for getFiveThings
        expectedFiveThingsResource = Resource(Status.SUCCESS, "success", fiveThings)
        fiveThingsLiveData.value = expectedFiveThingsResource
        whenever(repository.getFiveThings(any(), any(), any())).thenReturn(fiveThingsLiveData)

        //setup mocks for writeFiveThings
        expectedDatesResource = Resource(Status.SUCCESS, "success", dates)
        datesLiveData.value = expectedDatesResource
        whenever(repository.saveFiveThings(any(), any(), any())).thenReturn(datesLiveData)
    }

    @Test
    fun getFiveThings_returnsLiveData() {
        val actualResource = LiveDataTestUtil.getValue(viewModel.getFiveThings(date))
        actualResource shouldEqual expectedFiveThingsResource
    }

    @Test
    fun writeFiveThings_returnsLiveData() {
        val actualResource = LiveDataTestUtil.getValue(viewModel.writeFiveThings(fiveThings))
        actualResource shouldEqual expectedDatesResource
    }

    @Test
    fun getPreviousDayThings_CallsGetFiveThingsWithPrevDay() {
        val previousDay = getPreviousDate(date)

        LiveDataTestUtil.getValue(viewModel.getPreviousDay(date))

        argumentCaptor<Date>().apply {
            verify(repository, times(1)).getFiveThings(anyString(), capture(), any())
            firstValue shouldEqual previousDay
        }
    }

//    //mockito and kotlin fix
//    private fun <T> any(): T {
//        Mockito.any<T>()
//        return uninitialized()
//    }
//    private fun <T> uninitialized(): T = null as T
}