package com.beballer.beballer.ui.player.dash_board.find.map

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class SingleDataFragmentVM @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()
    private var mapApiJob: Job? = null
    fun getMapBound(
        northEastLat: Double, northEastLng: Double, southWestLat: Double, southWestLng: Double
    ) {
        val request = hashMapOf<String, Any>(
            "northEastLat" to northEastLat,
            "northEastLng" to northEastLng,
            "southWestLat" to southWestLat,
            "southWestLng" to southWestLng,
            "limit" to 100
        )
        mapApiCall(Constants.COURT_MAP_BOUNDS, request,Constants.COURT_MAP_BOUNDS)
    }


    // map bound api
    fun mapApiCall(url: String, request: HashMap<String, Any>,tag: String) {
        mapApiJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiGetOnlyAuthToken(url, request)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success(tag, response.body()))
                } else {
                    val errorMsg = handleErrorResponse(response.errorBody(), response.code())
                    observeCommon.postValue(Resource.error(errorMsg, null))
                }
            }.onFailure { e ->
                if (e is CancellationException) {
                    Log.d("MapAPI", "Previous API cancelled")
                } else {
                    observeCommon.postValue(
                        Resource.error(e.message ?: "Something went wrong", null)
                    )
                }
            }
        }
    }
}