package com.beballer.beballer.ui.organizers.camps_create

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommonCreateCampsFragmentVM @Inject constructor(  private val apiHelper: ApiHelper):BaseViewModel(){

    val commonObserver = SingleRequestEvent<JsonObject>()





}