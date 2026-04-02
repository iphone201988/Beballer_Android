package com.beballer.beballer.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.net.URISyntaxException

@Suppress("UNCHECKED_CAST")
object SocketManager {
    private const val SERVER_URL = "http://98.86.12.144:9000"

    //    private const val SERVER_URL = "http://192.168.1.57:8888"
    var mSocket: Socket? = null


    @Synchronized
    fun setSocket(token: String) {
        try {
            if (token.isBlank()) {
                Log.e("SocketHandler", "Token is empty! Cannot initialize socket.")
                return
            }

            val options = IO.Options().apply {
                extraHeaders = mapOf("token" to listOf(token.trim()))
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 2000
            }

            val socket = IO.socket(SERVER_URL, options)  // Use a local variable
            mSocket = socket  // Assign to the global property

            Log.i("SocketHandler", "Socket initialized with token: $token")

            // Ensure headers are attached dynamically during handshake
            socket.io()?.on(Manager.EVENT_TRANSPORT) { args ->
                try {
                    val transport = args[0] as? Transport ?: return@on
                    transport.on(Transport.EVENT_REQUEST_HEADERS) { headerArgs ->
                        try {
                         val headers =
                                headerArgs[0] as? MutableMap<String, List<String>> ?: return@on
                            headers["token"] = listOf(token.trim())  // Attach token dynamically
                            Log.i("SocketHandler", "Added token dynamically: $headers")
                        } catch (e: Exception) {
                            Log.e("SocketHandler", "Error modifying request headers: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocketHandler", "Error in transport setup: ${e.message}")
                }
            }

        } catch (e: URISyntaxException) {
            Log.e("SocketHandler", "Socket initialization error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SocketHandler", "Unexpected error: ${e.message}")
        }
    }


    @Synchronized
    fun getSocket(): Socket? {
        if (mSocket?.connected() == true) {
            Log.d(
                "_root_ide_package_.com.tech.young.SocketManager.mSocket",
                "getSocket: Already Connected"
            )
        } else if (mSocket?.connected() == false) {
            Log.d(
                "_root_ide_package_.com.tech.young.SocketManager.mSocket",
                "getSocket: Socket is disconnected, attempting to reconnect."
            )
            mSocket?.connect()
        } else {
            Log.d(
                "_root_ide_package_.com.tech.young.SocketManager.mSocket",
                "getSocket: Socket is neither connected nor disconnected, attempting to connect."
            )
            mSocket?.connect()
        }

        return mSocket
    }


    @Synchronized
    fun establishConnection() {
        if (mSocket == null) {
            Log.e("SocketHandler", "Socket is not initialized. Call setSocket() first.")
            return
        }

        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            Log.d(
                "SocketHandler",
                "Attempting to establish _root_ide_package_.com.tech.young.SocketManager.mSocket connection..."
            )

            // Listen for successful connection
            mSocket!!.on(Socket.EVENT_CONNECT) {
                Log.i("SocketHandler", "Socket connected successfully.")
            }

            // Listen for disconnection
            mSocket!!.on(Socket.EVENT_DISCONNECT) {
                Log.e("SocketHandler", "Socket disconnected.")
            }

            // Listen for connection errors
            mSocket!!.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketHandler", "Socket connection error: ${args.joinToString()}")
            }


        } else {
            Log.d("SocketHandler", "Socket is already connected.")
        }
    }


}

