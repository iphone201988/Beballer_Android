package com.beballer.beballer.ui.player.dash_board

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardActivityVM @Inject constructor(private val apiHelper: ApiHelper):BaseViewModel(){

    private val _userLocation = MutableLiveData<Location>()

    val commonObserver = SingleRequestEvent<JsonObject>()

    val userLocation: LiveData<Location> = _userLocation

    fun setLocation(location: Location) {
        _userLocation.value = location
    }


    fun commonLoginAPi(request: HashMap<String, Any>, url: String) {
        viewModelScope.launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBody(request, url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("commonLoginAPi", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(),it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "commonLoginAPi: $e")
            }
        }
    }


    fun enterCode(request: HashMap<String, Any>, url: String) {
        viewModelScope.launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBodyWithToken(request, url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("enterCode", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(),it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "commonLoginAPi: $e")
            }
        }
    }

}