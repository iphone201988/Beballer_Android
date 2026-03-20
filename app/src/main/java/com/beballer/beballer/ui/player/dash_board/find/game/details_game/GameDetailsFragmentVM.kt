package com.beballer.beballer.ui.player.dash_board.find.game.details_game

import android.util.Log
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameDetailsFragmentVM @Inject constructor(private val apiHelper: ApiHelper):BaseViewModel(){

    val commonObserver = SingleRequestEvent<JsonObject>()


    fun getGameDetails(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiGetWithoutQuery(url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("getGameDetails", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

    fun addRatingApi(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiPostForRawBody(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("addRatingApi", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }


    fun deleteGame(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiPostForRawBody(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("deleteGame", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

    fun removeReferee(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiPostForRawBody(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("removeReferee", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }
    fun acceptInvite(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForPostQuery(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("accept", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

    fun rejectInvite(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForPostQuery(url,data).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("reject", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }


    fun startGame(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBodyWithToken(data,url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("startGame", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            } 
        }
    }

    fun leaveGame(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBodyWithToken(data ,url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("leaveGame", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }


    fun removePlayer(data : HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            commonObserver.postValue(Resource.loading(null))
            try {
                apiHelper.apiForRawBodyWithToken(data ,url).let {
                    if (it.isSuccessful) {
                        commonObserver.postValue(Resource.success("removePlayer", it.body()))
                    } else commonObserver.postValue(
                        Resource.error(handleErrorResponse(it.errorBody(), it.code()), null)
                    )
                }
            } catch (e: Exception) {
                Log.d("error", "getPostApi: $e")
            }
        }
    }

}