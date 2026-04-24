package com.beballer.beballer.ui.organizers.tournament_create

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.TournamentCategory
import com.beballer.beballer.data.model.TournamentData
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class CommonTournamentVM @Inject constructor(
    private val apiHelper: ApiHelper
) : BaseViewModel() {

    val commonObserver = SingleRequestEvent<JsonObject>()

    // ✅ Store all form data
    val tournamentData = TournamentData()


    val selectedTournament = MutableLiveData<TournamentCategory>()

    val tournamentList = MutableLiveData<ArrayList<TournamentCategory>>()

    // ✅ Store images
    val selectedImageParts = mutableListOf<MultipartBody.Part>()

    // ✅ COMMON REQUEST BUILDER
    private fun buildTournamentRequest(): HashMap<String, RequestBody> {

        val data = tournamentData

        return HashMap<String, RequestBody>().apply {


            putSafe("startDate", data.startDate)
            putSafe("endDate", data.endDate)
            putSafe("type", "tournament")
            putSafe("level", data.level)
            putSafe("name", data.name)
            putSafe("city", data.city)
            putSafe("format", data.format)
            putSafe("priceRange", data.priceRange)
            putSafe("country", data.country)
            putSafe("ageRange", data.ageRange)
            putSafe("description", data.description)
            putSafe("address", data.address)
            putSafe("region", data.region)

            putSafe("lat", data.lat?.toString())
            putSafe("long", data.long?.toString())

            putSafe("usesBeballerForm", data.usesBeballerForm?.toString())
            putSafe("hasCategories", data.hasCategories?.toString())


        }
    }



    private fun buildAdvancedTournamentRequest(): HashMap<String, Any> {

        val data = tournamentData

        return HashMap<String, Any>().apply {

            fun putWithLog(key: String, value: Any?) {
                if (value != null) {
                    Log.i("ADV_API", "ADDING -> $key: $value")
                    put(key, value)
                } else {
                    Log.w("ADV_API", "SKIPPED (NULL) -> $key")
                }
            }

            putWithLog("eventId", data.eventId)
            putWithLog("startDate", data.startDate)
            putWithLog("endDate", data.endDate)
            putWithLog("name", data.name)
            putWithLog("level", data.level)
            putWithLog("ageRange", data.ageRange)
            putWithLog("priceRange", data.priceRange)
            putWithLog("description", data.description)
            putWithLog("usesBeballerForm", data.usesBeballerForm)
            putWithLog("courtsCount", data.courtsCount)
            putWithLog("teamsCount", data.teamsCount)
            putWithLog("poolsCount", data.poolsCount)
            putWithLog("categoryId", data.categoryId)
        }
    }


    // ✅ EXISTING API (NO CHANGE)
    fun createTournament() {
        val request = buildTournamentRequest()
        val parts = selectedImageParts.toMutableList()

        Log.i("CREATE_API", "Request: $request")

        callApi(Constants.CREATE_EVENT_TOURNAMENT, request, parts)
    }

    // ✅ NEW API CALL
    fun createAdvancedTournament() {
        val request = buildAdvancedTournamentRequest()

        Log.i("ADV_API", "Request: $request")

        callAdvancedApi(Constants.ADD_ORGANIZER_CATEGORY, request)
    }

    // ✅ EXISTING API FUNCTION (UNCHANGED)
    private fun callApi(
        url: String,
        map: HashMap<String, RequestBody>,
        part: MutableList<MultipartBody.Part>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            commonObserver.postValue(Resource.loading(null))

            try {
                val response = apiHelper.apiForPostMultipartList(url, map, part)

                if (response.isSuccessful && response.body() != null) {

                    commonObserver.postValue(
                        Resource.success("CREATE_TOURNAMENT", response.body())
                    )

                } else {

                    val errorMsg = handleErrorResponse(
                        response.errorBody(),
                        response.code()
                    )

                    commonObserver.postValue(Resource.error(errorMsg, null))
                }

            } catch (e: Exception) {

                Log.e("API_ERROR", "Error: ${e.localizedMessage}", e)

                val message = when (e) {
                    is java.net.UnknownHostException -> "No Internet Connection"
                    is java.net.SocketTimeoutException -> "Request Timeout"
                    is javax.net.ssl.SSLException -> "SSL Error"
                    else -> e.localizedMessage ?: "Something went wrong"
                }

                commonObserver.postValue(Resource.error(message, null))
            }
        }
    }

    // ✅ NEW SEPARATE API FUNCTION
    private fun callAdvancedApi(
        url: String,
        map: HashMap<String, Any>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            commonObserver.postValue(Resource.loading(null))

            try {
                val response = apiHelper.apiForRawBodyWithToken( map,url)

                if (response.isSuccessful && response.body() != null) {

                    commonObserver.postValue(
                        Resource.success("ADVANCED_TOURNAMENT", response.body())
                    )

                } else {

                    val errorMsg = handleErrorResponse(
                        response.errorBody(),
                        response.code()
                    )

                    commonObserver.postValue(Resource.error(errorMsg, null))
                }

            } catch (e: Exception) {

                Log.e("ADV_API_ERROR", "Error: ${e.localizedMessage}", e)

                val message = when (e) {
                    is java.net.UnknownHostException -> "No Internet Connection"
                    is java.net.SocketTimeoutException -> "Request Timeout"
                    is javax.net.ssl.SSLException -> "SSL Error"
                    else -> e.localizedMessage ?: "Something went wrong"
                }

                commonObserver.postValue(Resource.error(message, null))
            }
        }
    }

    // ✅ SAFE PUT FUNCTION
    fun HashMap<String, RequestBody>.putSafe(key: String, value: String?) {
        val safeValue = value ?: ""   // 👈 NEVER send null
        this[key] = safeValue.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // ✅ CLEAR DATA
    fun clearTournamentData() {
        tournamentData.apply {
            eventId = null
            startDate = null
            endDate = null
            name = null
            level = null
            city = null
            format = null
            priceRange = null
            country = null
            ageRange = null
            description = null
            address = null
            region = null
            lat = null
            long = null
            usesBeballerForm = null
            hasCategories = null
            courtsCount = null
            teamsCount = null
            poolsCount = null
        }

        selectedImageParts.clear()
    }



    fun updateCourt(url: String, map: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {

            commonObserver.postValue(Resource.loading(null))

            try {
                val response = apiHelper.apiForRawBodyWithToken( map,url)

                if (response.isSuccessful && response.body() != null) {

                    commonObserver.postValue(
                        Resource.success("UPDATE_COURT", response.body())
                    )

                } else {

                    val errorMsg = handleErrorResponse(
                        response.errorBody(),
                        response.code()
                    )

                    commonObserver.postValue(Resource.error(errorMsg, null))
                }

            } catch (e: Exception) {

                Log.e("ADV_API_ERROR", "Error: ${e.localizedMessage}", e)

                val message = when (e) {
                    is java.net.UnknownHostException -> "No Internet Connection"
                    is java.net.SocketTimeoutException -> "Request Timeout"
                    is javax.net.ssl.SSLException -> "SSL Error"
                    else -> e.localizedMessage ?: "Something went wrong"
                }

                commonObserver.postValue(Resource.error(message, null))
            }
        }
    }




    val tournamentDataList = ArrayList<TournamentCategory>()

    init {
        if (tournamentDataList.isEmpty()) {
            tournamentDataList.add(TournamentCategory("Tournament 1", "1", true))
            tournamentDataList.add(TournamentCategory("Tournament 2", "2"))
        }
    }

    fun addTournament(): Boolean {
        if (tournamentDataList.size >= 6) {
            return false   // ❌ limit reached
        }

        val nextNumber = tournamentDataList.size + 1

        tournamentDataList.add(
            TournamentCategory(
                tournamentName = "Tournament $nextNumber",
                count = nextNumber.toString()
            )
        )

        return true // ✅ added successfully
    }

}