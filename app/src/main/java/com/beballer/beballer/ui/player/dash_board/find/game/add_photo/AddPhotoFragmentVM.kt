package com.beballer.beballer.ui.player.dash_board.find.game.add_photo

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
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Source
import javax.inject.Inject


@HiltViewModel
class AddPhotoFragmentVM @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()

    // create court api
    fun createCourt(
        name: String,
        address: String,
        accessibility: String,
        hoopsCount: Int,
        lat: String,
        long: String,
        boardType: String,
        netType: String,
        floorType: String,
        hasWaterPoint: Boolean,
        areDimensionsStandard: Boolean,
        country: String,
        grade: String,
        city: String,
        zipCode: String,
        region: String,
        description: String,
        photos: MutableList<MultipartBody.Part>
    ) {
        val request = hashMapOf<String, RequestBody>(
            "name" to name.toRequestBody(),
            "addressString" to address.toRequestBody(),
            "accessibility" to accessibility.toRequestBody(),
            "hoopsCount" to hoopsCount.toString().toRequestBody(),
            "lat" to lat.toRequestBody(),
            "long" to long.toRequestBody(),
            "boardType" to boardType.toRequestBody(),
            "netType" to netType.toRequestBody(),
            "floorType" to floorType.toRequestBody(),
            "hasWaterPoint" to hasWaterPoint.toString().toRequestBody(),
            "areDimensionsStandard" to areDimensionsStandard.toString().toRequestBody(),
            "grade" to grade.toRequestBody(),
            "country" to country.toRequestBody(),
            "city" to city.toRequestBody(),
            "zipCode" to zipCode.toRequestBody(),
            "region" to region.toRequestBody(),
            "description" to description.toRequestBody(),
        )
        callApi(Constants.NEW_COURT, request, photos)
    }


    fun callApi(
        url: String, map: HashMap<String, RequestBody>, part: MutableList<MultipartBody.Part>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiForPostMultipartList(url, map, part)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("createCourt", response.body()))
                } else {
                    val errorMsg = handleErrorResponse(response.errorBody(), response.code())
                    observeCommon.postValue(Resource.error(errorMsg, null))
                }
            }.onFailure { e ->
                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                observeCommon.postValue(Resource.error("${e.message}", null))
            }
        }
    }
}