package com.beballer.beballer.ui.player.dash_board.find.game.details_game.game_chat

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.ChatData
import com.beballer.beballer.data.model.ChatModel
import com.beballer.beballer.data.model.GetChatApiResponse
import com.beballer.beballer.data.model.GetPlayersApiResponse
import com.beballer.beballer.data.model.Message
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentGameChatBinding
import com.beballer.beballer.databinding.RecyclerGameMessageItemBinding
import com.beballer.beballer.ui.player.dash_board.find.game.invite_player.PlayerItem
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.SocketManager
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject

@AndroidEntryPoint
class GameChatFragment : BaseFragment<FragmentGameChatBinding>() {


    private lateinit var chatAdapter: SimpleRecyclerViewAdapter<Message, RecyclerGameMessageItemBinding>

    private var gameChatId : String  ? = null

    private val viewModel :  GameChatVm by viewModels()


    private var mSocket: Socket? = null

    private var currentPage = 1
    private var isLoading = false
    private var hasNextPage = true
    private val limit = 50

    override fun getLayoutResource(): Int {
         return R.layout.fragment_game_chat
    }

    override fun getViewModel(): BaseViewModel {
      return  viewModel
    }

    override fun onCreateView(view: View) {

        initChatAdapter()
        initObserver()



        binding.ssPullRefresh.setOnRefreshListener {
            if (!isLoading && hasNextPage) {
                loadMoreMessages()
            } else {
                binding.ssPullRefresh.isRefreshing = false
            }
        }

        binding.messagesRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager =
                        recyclerView.layoutManager as LinearLayoutManager

                    val lastVisible =
                        layoutManager.findLastCompletelyVisibleItemPosition()

                    // ✅ Enable refresh only when user is at TOP (oldest message)
                    binding.ssPullRefresh.isEnabled =
                        lastVisible == chatAdapter.itemCount - 1
                }
            }
        )

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        socketHandler()
        initData()
        listenIncomingMessages()
        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.cancelImage ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.ivSend ->{
                    val message = binding.etSendMessage.text.toString().trim()

                    if (message.isNullOrBlank()){
                        showErrorToast("Please enter message")
                    }
                    else{
                        sendMessage(message)
                    }
                }
            }
        })
    }




    override fun onStart() {
        super.onStart()

        if (gameChatId != null && mSocket?.connected() == true) {
            joinRoom(gameChatId!!)
        }
    }

    private fun socketHandler() {

        mSocket = SocketManager.getSocket()

        if (mSocket == null) {
            Log.e("SocketHandler", "Socket is null. Make sure setSocket() is called first.")
            return
        }

        if (mSocket?.connected() == true) {

        } else {
            Log.d("SocketHandler", "Connecting socket...")
            mSocket?.connect()
        }
    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING ->  {

                }
                Status.SUCCESS ->  {
                    hideLoading()
                    when(it.message){
                        "getMessages" -> {
                            try {
                                val myDataModel: GetChatApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())

                                if (myDataModel != null) {

                                    val currentUserId =
                                        sharedPrefManager.getLoginData()?.data?.user?.id

                                    val updatedList = myDataModel.data?.messages
                                        ?.map { message ->
                                            message.apply {
                                                chatType = senderId == currentUserId
                                            }
                                        }


                                    if (currentPage == 1) {
                                        chatAdapter.setList(updatedList)
                                        binding.messagesRecyclerView.scrollToPosition(0)
                                    } else {
                                        if (!updatedList.isNullOrEmpty()) {
                                            chatAdapter.addToList(updatedList)
                                        } else {
                                            hasNextPage = false
                                            currentPage--   // rollback page
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                                binding.ssPullRefresh.isRefreshing = false
                                hideLoading()
                            }
                        }

                    }
                }
                Status.ERROR ->  {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }


    private fun joinRoom(gameId: String) {

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        mSocket?.emit("join_game_chat", data)
        Log.d("SocketHandler", "Joined chat room: $gameId")
    }


    private fun leaveRoom() {
        mSocket?.emit("leave_game_chat")
        Log.d("SocketHandler", "Left chat room")
    }

    private fun sendMessage(message: String) {

        if (message.isBlank()) return

        val data = JSONObject().apply {
            put("message", message)
            put("gameId", gameChatId)
        }

        mSocket?.emit("send_group_message", data)

        binding.etSendMessage.text?.clear()
    }


    private fun listenIncomingMessages() {

        mSocket?.off("receive_group_message")

        mSocket?.on("receive_group_message") { args ->

            requireActivity().runOnUiThread {

                if (args.isEmpty()) return@runOnUiThread

                val json = args[0] as? JSONObject ?: return@runOnUiThread

                Log.d("SocketChat", "Received JSON: $json")

                val currentUserId =
                    sharedPrefManager.getLoginData()?.data?.user?.id

                val senderId = json.optString("senderId")

                val isMyMessage = senderId.trim() == currentUserId?.trim()

                Log.d("SocketChat", "SenderId: $senderId")
                Log.d("SocketChat", "CurrentUserId: $currentUserId")
                Log.d("SocketChat", "IsMyMessage: $isMyMessage")


                Log.d("CHAT_DEBUG", "SenderId -> '$senderId'")
                Log.d("CHAT_DEBUG", "CurrentUserId -> '$currentUserId'")
                Log.d("CHAT_DEBUG", "Equal? -> ${senderId == currentUserId}")
                Log.d("CHAT_DEBUG", "Trim Equal? -> ${senderId.trim() == currentUserId?.trim()}")

                val messageObj = Message(
                    _id = json.optString("messageId"), // FIXED
                    chatGroupId = json.optString("chatGroupId"),
                    createdAt = json.optString("createdAt"),
                    isDeleted = false,                 // default
                    isRead = false,                    // default
                    message = json.optString("message"),
                    senderId = senderId,
                    senderImage = json.optString("senderImage"),
                    senderUsername = json.optString("senderUsername"),
                    updatedAt = json.optString("createdAt"), // fallback
                    chatType = isMyMessage
                )

                chatAdapter.addDataAtTop(messageObj)
                binding.messagesRecyclerView.scrollToPosition(0)
            }
        }
    }


    /** handle out side adapter **/
    private var chatList = ArrayList<ChatModel>()
    private fun initChatAdapter() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        binding.messagesRecyclerView.layoutManager = layoutManager

        chatAdapter = SimpleRecyclerViewAdapter(
            R.layout.recycler_game_message_item,
            BR.bean
        ) { view, item, pos ->
            // handle click if needed
        }

        binding.messagesRecyclerView.adapter = chatAdapter
    }


    // add List in data chat
    private fun getChatList(): ArrayList<ChatModel> {
        return arrayListOf(
            ChatModel("hi", true),
            ChatModel("how are you", true),
            ChatModel("i am fine", false),
            ChatModel("where are you at this time", true),
            ChatModel("jaunpur", false),
            ChatModel("ok ok ", true),
        )

    }


    override fun onResume() {
        super.onResume()

    }

    private fun initData() {
        gameChatId = arguments?.getString("gameChatId")

        if (gameChatId != null) {
            currentPage = 1
            hasNextPage = true
            callGetMessagesApi()
        }

        val profileImage = sharedPrefManager.getLoginData()?.data?.user?.profilePicture
        BindingUtils.setImageFromUrl(binding.ivProfile, profileImage)
    }

    private fun callGetMessagesApi() {

        if (gameChatId == null) return

        isLoading = true

        val data = HashMap<String, Any>()
        data["page"] = currentPage
        data["limit"] = limit

        viewModel.getMessages(Constants.GET_MESSAGES + gameChatId, data)
    }

    private fun loadMoreMessages() {

        if (isLoading || !hasNextPage) {
            binding.ssPullRefresh.isRefreshing = false
            return
        }

        currentPage++
        callGetMessagesApi()
    }
}