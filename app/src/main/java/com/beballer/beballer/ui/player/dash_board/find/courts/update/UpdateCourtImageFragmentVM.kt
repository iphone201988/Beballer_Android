package com.beballer.beballer.ui.player.dash_board.find.courts.update

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
import javax.inject.Inject

@HiltViewModel
class UpdateCourtImageFragmentVM @Inject constructor(private val apiHelper: ApiHelper) :
    BaseViewModel() {
    val observeCommon = SingleRequestEvent<JsonObject>()

    /**
     * Create or Edit Court API
     * @param courtId If null, it's Create mode (POST). If not null, it's Edit mode (PUT).
     */
    fun updateImageCourt(
        grade: String,
        description: String,
        photos: MutableList<MultipartBody.Part>,
        courtId: String? = null
    ) {
        val request = hashMapOf(
            "rating" to grade.toRequestBody(),
            "description" to description.toRequestBody(),
        )
        val imageOrderParts = mutableListOf<MultipartBody.Part>()
        if (!courtId.isNullOrEmpty()) {
            for (i in photos.indices) {
                imageOrderParts.add(MultipartBody.Part.createFormData("imageOrder[]", "NEW_IMAGE"))
                Log.d("data", "updateImageCourt: $i")
            }
            photos.addAll(imageOrderParts)

            val urlWithId = "${Constants.UPDATE_COURT}/$courtId"
            putAllApi(urlWithId, request, photos)
        }
    }


    /**
     * PUT method for Edit mode
     */
    fun putAllApi(
        url: String, map: HashMap<String, RequestBody>, part: MutableList<MultipartBody.Part>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            observeCommon.postValue(Resource.loading(null))
            runCatching {
                val response = apiHelper.apiForMultipartPut(url, map, part)
                if (response.isSuccessful) {
                    observeCommon.postValue(Resource.success("updateAPiCall", response.body()))
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