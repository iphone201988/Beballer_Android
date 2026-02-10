package com.beballer.beballer.ui.player.dash_board.find.map

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.MapType
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
class SingleDataFragmentVM @Inject constructor(
    private val apiHelper: ApiHelper
) : BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()
    private var mapApiJob: Job? = null

    fun getMapBound(
        type: MapType,
        northEastLat: Double,
        northEastLng: Double,
        southWestLat: Double,
        southWestLng: Double
    ) {
        val request = createBoundRequest(
            northEastLat,
            northEastLng,
            southWestLat,
            southWestLng
        )
        callMapApi(type, request)
    }

    fun getSearchMap(search: String) {
        val request = hashMapOf<String, Any>(
            "search" to search,
            "limit" to 100
        )
        callMapApi(MapType.SEARCH, request)
    }


    private fun callMapApi(
        type: MapType,
        request: HashMap<String, Any>
    ) {
        mapApiJob?.cancel()
        mapApiJob = viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(type.url, request)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success(type.tag, response.body()))
                } else {
                    observeCommon.postValue(
                        Resource.error(handleErrorResponse(response.errorBody(), response.code()), null))
                }
            } catch (e: CancellationException) {
                Log.d("MapAPI", "Previous API cancelled")
            } catch (e: Exception) {
                observeCommon.postValue(Resource.error(e.message ?: "Something went wrong", null))
            }
        }
    }

    private fun createBoundRequest(
        northEastLat: Double,
        northEastLng: Double,
        southWestLat: Double,
        southWestLng: Double
    ): HashMap<String, Any> = hashMapOf(
        "northEastLat" to northEastLat,
        "northEastLng" to northEastLng,
        "southWestLat" to southWestLat,
        "southWestLng" to southWestLng,
        "limit" to 100
    )
}
