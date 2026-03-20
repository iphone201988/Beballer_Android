package com.beballer.beballer.ui.organizers.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class OrganizersProfileFragmentVM @Inject constructor(private val apiHelper: ApiHelper):BaseViewModel(){
    val commonObserver = SingleRequestEvent<JsonObject>()



    fun uniqueName(request: HashMap<String, Any>, url: String) {
        viewModelScope.launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBodyWithToken(request, url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("uniqueName", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(),it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "commonLoginAPi: $e")
            }
        }
    }


    fun createOrganizer( map: HashMap<String, RequestBody>, part: MultipartBody.Part?) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForPostMultipart(Constants.CREATE_ORGANIZER,map,part).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("createOrganizer", it.body()))
                    } else {
                        commonObserver.postValue(Resource.error(handleErrorResponse(it.errorBody(),it.code()), null))
                    }
                }
            } catch (e: Exception) {
                Log.d("error", "createOrganizer: $e")
            }
        }
    }


}