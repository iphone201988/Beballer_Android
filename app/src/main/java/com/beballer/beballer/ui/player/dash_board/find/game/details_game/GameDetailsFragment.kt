package com.beballer.beballer.ui.player.dash_board.find.game.details_game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.ChatModel
import com.beballer.beballer.data.model.CourtDataById
import com.beballer.beballer.data.model.GameDetail
import com.beballer.beballer.data.model.GameDetailTeam1Player
import com.beballer.beballer.data.model.GameState
import com.beballer.beballer.data.model.GetGameDetailsApiResponse
import com.beballer.beballer.data.model.MpvModel
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.data.model.Team2Player
import com.beballer.beballer.data.model.TeamSlotModel
import com.beballer.beballer.databinding.AddToCalendarDialogLayoutBinding
import com.beballer.beballer.databinding.CreateGameDialogBinding
import com.beballer.beballer.databinding.DialogRemoveRefereeBinding
import com.beballer.beballer.databinding.FragmentGameDetailsBinding
import com.beballer.beballer.databinding.GameDeletePopupBinding
import com.beballer.beballer.databinding.LevelPracticedDialogBinding
import com.beballer.beballer.databinding.OpenMapDialogBinding
import com.beballer.beballer.databinding.RatingDialogBinding
import com.beballer.beballer.databinding.RecyclerGameMessageItemBinding
import com.beballer.beballer.databinding.RefereeOptionDailogBinding
import com.beballer.beballer.databinding.RemovePlayerDialogBinding
import com.beballer.beballer.ui.player.dash_board.find.details.ImagesPagerAdapter
import com.beballer.beballer.ui.player.dash_board.find.game.create_game.TeamAdapter
import com.beballer.beballer.ui.player.dash_board.find.player_profile.PlayerProfileActivity
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.BindingUtils.vectorToBitmapDescriptor
import com.beballer.beballer.utils.Status
import com.google.android.gms.auth.api.signin.internal.HashAccumulator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class GameDetailsFragment : BaseFragment<FragmentGameDetailsBinding>(), OnMapReadyCallback,
    TeamAdapter.OnInviteClickListener {
    private val viewModel: GameDetailsFragmentVM by viewModels()

    //    private lateinit var homeTeamAdapter: SimpleRecyclerViewAdapter<MpvModel, RvTeamItemBinding>
//    private lateinit var outSideTeamAdapter: SimpleRecyclerViewAdapter<MpvModel, RvOutSideItemBinding>
    private lateinit var chatAdapter: SimpleRecyclerViewAdapter<ChatModel, RecyclerGameMessageItemBinding>

    private var googleMap: GoogleMap? = null
    private lateinit var removePlayerDialog: BaseCustomDialog<RemovePlayerDialogBinding>

    private lateinit var deleteGamePopup: BaseCustomDialog<GameDeletePopupBinding>

    private lateinit var team1Adapter: TeamAdapter
    private lateinit var team2Adapter: TeamAdapter

    private var gameChatId : String ? = null
    private var playerId : String  ? = null
    private lateinit var ratingPopup: BaseCustomDialog<RatingDialogBinding>

    private lateinit var addCalendarPopup: BaseCustomDialog<AddToCalendarDialogLayoutBinding>
    private lateinit var refereeOption: BaseCustomDialog<RefereeOptionDailogBinding>
    private lateinit var removeReferee: BaseCustomDialog<DialogRemoveRefereeBinding>

    private lateinit var openMapApp: BaseCustomDialog<OpenMapDialogBinding>



    private var gameMarker: Marker? = null

    private var formattedRating: String? = null

    private lateinit var levelPopup: BaseCustomDialog<LevelPracticedDialogBinding>

    private lateinit var kingCourtPopup: BaseCustomDialog<CreateGameDialogBinding>

    private val translationYaxis = -100F
    private var isFabMenuVisible = false

    private lateinit var imagesPagerAdapter: ImagesPagerAdapter

    private var eventDate: String? = null
    private var courtId: String? = null
    private var courtUserId: String? = null

    private var organizerId: String? = null

    private var refereeId: String? = null


    private var gameLat: Double? = null
    private var gameLong: Double? = null


    private var gameId: String? = null
    private val interpolator = OvershootInterpolator()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_game_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // set block pos
        binding.pos = 1
        binding.tvFeed.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        binding.tvSubscriptions.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)

        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // fab button click
        initFabMenu()

        initPopup()
        // view pager
        initViewPager()


        // click
        initOnClick()

        initObserver()

        setupAdapters()

        initChatAdapter()
        val fullText = "Created by\n@Junior78"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("@Junior78")
        val end = start + "@Junior78".length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#1877F2")),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvGameOrganizer.text = spannable

        setupRating()



    }

    private fun initPopup() {
        ratingPopup = BaseCustomDialog(requireContext(), R.layout.rating_dialog) {
            when (it.id) {
                R.id.btnCreateGame -> {
                    if (!formattedRating.isNullOrEmpty()) {
                        val data = HashMap<String, Any>()
                        data["rating"] = formattedRating.toString()
                        data["courtId"] = courtId.toString()
                        viewModel.addRatingApi(data, Constants.ADD_RATING)
                        ratingPopup.dismiss()
                    }
                }

                R.id.btnCancel -> {
                    ratingPopup.dismiss()
                }

            }

        }
        levelPopup = BaseCustomDialog(requireContext(), R.layout.level_practiced_dialog) {

        }
        kingCourtPopup = BaseCustomDialog(requireContext(), R.layout.create_game_dialog) {

        }
        openMapApp = BaseCustomDialog(requireContext(), R.layout.open_map_dialog) {
            when (it.id) {

                R.id.btnConfirm -> {

                    if (gameLat != null && gameLong != null) {

                        val uri = Uri.parse("geo:$gameLat,$gameLong?q=$gameLat,$gameLong")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps") // Open directly in Google Maps (optional)

                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Location not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    openMapApp.dismiss()
                }

                R.id.btnCancel -> {
                    openMapApp.dismiss()
                }
            }
        }


        deleteGamePopup = BaseCustomDialog(requireContext(), R.layout.game_delete_popup) {
            when (it.id) {
                R.id.btnConfirm -> {
                    if (gameId != null) {
                        val data = HashMap<String, Any>()
                        data["gameId"] = gameId.toString()
                        viewModel.deleteGame(data, Constants.DELETE_GAME)
                    }
                    deleteGamePopup.dismiss()
                }

                R.id.btnCancel -> {
                    deleteGamePopup.dismiss()
                }

            }
        }

        addCalendarPopup =
            BaseCustomDialog(requireContext(), R.layout.add_to_calendar_dialog_layout) {
                when (it.id) {
                    R.id.btnConfirm -> {

                        if (!eventDate.isNullOrEmpty()) {
                            val (startMillis, endMillis) = getStartAndEndTime(eventDate)

                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, "Match Event")
                                putExtra(CalendarContract.Events.DESCRIPTION, "Game Scheduled")
                                putExtra(
                                    CalendarContract.Events.EVENT_LOCATION,
                                    "Your Ground Location"
                                )
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                            }

                            startActivity(intent)
                        } else {
                            showErrorToast("Invalid event date")
                        }

                        addCalendarPopup.dismiss()
                    }

                    R.id.btnCancel -> {
                        addCalendarPopup.dismiss()
                    }
                }
            }

        removePlayerDialog = BaseCustomDialog(requireContext(), R.layout.remove_player_dialog){
            when(it.id){
                R.id.btnConfirm ->{
                    if (playerId != null && gameId != null){
                        val data = HashMap<String, Any>()
                        data["playerId"] = playerId.toString()
                        data["gameId"] = gameId.toString()

                        viewModel.removePlayer(data, Constants.REMOVE_PLAYER)
                        removePlayerDialog.dismiss()
                    }
                }
                R.id.btnCancel -> {
                    removePlayerDialog.dismiss()
                }
            }
        }


        refereeOption = BaseCustomDialog(requireContext(), R.layout.referee_option_dailog){
            when(it.id){
                R.id.btnViewProfile ->{
                    if (refereeId != null) {
                        val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
                        intent.putExtra("playerProfile", refereeId)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                    refereeOption.dismiss()
                }
                R.id.btnChangeReferee ->{

                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "invitePlayer")
                    intent.putExtra("from", "changeReferee")
                    intent.putExtra("gameId" ,gameChatId)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )

                    refereeOption.dismiss()
                }
                R.id.btnRemoveReferee ->{
                    removeReferee.show()

                    refereeOption.dismiss()
                }
                R.id.btnCancel ->{
                    refereeOption.dismiss()
                }
            }
        }


        removeReferee = BaseCustomDialog(requireContext(), R.layout.dialog_remove_referee){
            when(it.id){
                R.id.btnConfirm ->{
                    val data = HashMap<String, Any>()
                    data["gameId"] = gameChatId.toString()

                    viewModel.removeReferee(data, Constants.REMOVE_REFEREE)
                    removeReferee.dismiss()
                }
                R.id.btnCancel ->{
                    removeReferee.dismiss()
                }
            }
        }

    }


    private fun getStartAndEndTime(apiDate: String?): Pair<Long, Long> {

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date = sdf.parse(apiDate)
        val startTimeMillis = date?.time ?: 0L

        // Example: 1 hour event
        val endTimeMillis = startTimeMillis + (60 * 60 * 1000)

        return Pair(startTimeMillis, endTimeMillis)
    }


    private fun setupRating() {

        val popupBinding = ratingPopup.binding


        popupBinding.tvYourRating.text =
            "Your rating : ${String.format("%.1f", popupBinding.courtRatingBar.rating)}"



        popupBinding.courtRatingBar.setOnRatingBarChangeListener = { rating, fromUser ->

            if (fromUser) {
                formattedRating = String.format("%.1f", rating)

                Log.i("fdsfsdf", "setupRating: $formattedRating")

                popupBinding.tvYourRating.text = "Your rating : $formattedRating"

            }
        }


    }


    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()

                    when (it.message) {
                        "getGameDetails" -> {

                            val myDataModel: GetGameDetailsApiResponse? =
                                BindingUtils.parseJson(it.data.toString())

                            val game = myDataModel?.game ?: return@Observer
                            updateMapLocation()


                            if (game?.field?.latitude != null && game?.field?.longitude  != null) {
                                val lat1 = game?.field?.latitude
                                val lon1 = game?.field?.longitude
                                val lat2 = BindingUtils.lat
                                val lon2 = BindingUtils.long
                                val distance = BindingUtils.formattedDistance(
                                    lat1, lon1, lat2, lon2
                                )
                                binding.tvCourtDistance.text = distance
                            }

                            // 1️⃣ Map state
                            val gameState = mapGameState(game)

                            gameState?.let { state ->
                                configureGameState(state)
                                binding.removeCourtFab.isVisible = state.isOrganizer

                                team1Adapter.updateEditPermission(state.isOrganizer)
                                team2Adapter.updateEditPermission(state.isOrganizer)
                            }
//                            gameState?.let { state ->
//                                configureGameState(state)
//                                binding.removeCourtFab.isVisible = state.isOrganizer
//
//                                team1Adapter.updateEditPermission(state.isOrganizer)
//                                team2Adapter.updateEditPermission(state.isOrganizer)
//                            }

                            // 2️⃣ Basic binding
                            binding.bean = game



                            gameChatId = game._id
                            courtId = game.field?.id
                            organizerId = game.organizer?._id
                            eventDate = game.date
                            courtUserId = game.field?.contributor?._id
                            gameLat = game.field?.latitude
                            gameLong = game.field?.longitude

                            // 3️⃣ Players
                            val team1Players = game.team1Players
                                ?.filterNotNull()
                                ?.map { it.toPlayer() }
                                ?: emptyList()

                            val team2Players = game.team2Players
                                ?.filterNotNull()
                                ?.map { it.toPlayer() }
                                ?: emptyList()

                            gameState?.let { state ->

                                team1Adapter.submitList(
                                    prepareTeamList(
                                        players = team1Players,
                                        totalSlots = state.maxPlayersPerTeam,
                                        isEditable = state.isOrganizer
                                    )
                                )

                                team2Adapter.submitList(
                                    prepareTeamList(
                                        players = team2Players,
                                        totalSlots = state.maxPlayersPerTeam,
                                        isEditable = state.isOrganizer
                                    )
                                )
                            }

                            // 4️⃣ Referee
                            if (game.isAutoRefereeing == true) {
                                binding.tvGameReferee.text = "Self Refereeing"
                            } else {
                                binding.tvGameReferee.text = game.referee?.username
                                refereeId = game.referee?._id
                            }

                            // 5️⃣ Images
                            val photos = game.field?.photos ?: emptyList()
                            imagesPagerAdapter.updateImages(photos as List<String>)
                            binding.dotsIndicator.setPageSize(photos.size)
                            binding.dotsIndicator.notifyDataChanged()
                            binding.noCourtPicture.isVisible = photos.isEmpty()


                        }

                        "addRatingApi" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                initData()
                            }
                        }

                        "deleteGame" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }

                        "accept" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }

                        "reject" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }

                        "leaveGame" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }

                        "startGame" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }

                        "removePlayer" ->{
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }
                        "removeReferee" ->{
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                initData()
                            }
                        }
                    }

                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        })
    }




    /**
     * Method to initialize map
     */

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMapLocation()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun updateMapLocation() {
        val map = googleMap ?: return
        val latitude = binding.bean?.field?.latitude
        val longitude = binding.bean?.field?.longitude

        Log.i("fdsfds", "updateMapLocation: $latitude , $longitude")

        if (latitude != null && longitude != null) {
            val latLng = LatLng(latitude , longitude )

            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val markerIcon = vectorToBitmapDescriptor(
                requireContext(), R.drawable.findcourticon, 72, 72
            )

            gameMarker = map.addMarker(
                MarkerOptions().position(latLng).icon(markerIcon)
            )



            map.uiSettings.apply {
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isTiltGesturesEnabled = false
                isRotateGesturesEnabled = false
                isMapToolbarEnabled = false
                isZoomControlsEnabled = false
                isCompassEnabled = false
            }
            map.setOnMarkerClickListener { marker ->
                if (marker == gameMarker) {
                    binding.bean?.let { court ->
                    //    openNextFragment(court)
                    }
                    true
                } else {
                    false
                }
            }
            map.isTrafficEnabled = false
            map.isBuildingsEnabled = false
        }
    }


    private fun openNextFragment(court: CourtDataById) {
        val bundle = Bundle().apply {
            putParcelable("courtData", court)
        }
        val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
            putExtra("userType", "singleDataFragment")
            putExtras(bundle)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )


    }
    private fun initData() {
        gameId = arguments?.getString("gameId")
        if (gameId != null) {
            viewModel.getGameDetails(Constants.GET_GAME_BY_ID + gameId)
        }
    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                // back button click
                R.id.cancelImage -> {
                    requireActivity().finish()
                }
                // message send button  click
                R.id.ivSend -> {
                    val message = binding.etSendMessage.text.toString()
                    if (message.isEmpty()) {
                        showInfoToast("Please enter message")
                    } else {
                        chatList.add(ChatModel(message, true))
                        chatAdapter.list = chatList
                        binding.messagesRecyclerView.scrollToPosition(chatList.size - 1)
                        binding.etSendMessage.setText("")
                        binding.etSendMessage.clearFocus()
                    }

                }

                // tvFeed button click
                R.id.tvFeed -> {
                    binding.pos = 1
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }
                // tvSubscriptions  button click
                R.id.tvSubscriptions -> {
                    binding.pos = 2
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                }

                R.id.courtRatingBar -> {
                    ratingPopup.show()
                }

                R.id.tvCourtKing -> {
                    kingCourtPopup.show()
                }

                R.id.tvCourtLevel -> {
                    levelPopup.show()
                }

                R.id.removeCourtFab -> {
                    deleteGamePopup.show()
                }

                R.id.tvGameReferee -> {

                    refereeOption.show()
//                    if (refereeId != null) {
//                        val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
//                        intent.putExtra("playerProfile", refereeId)
//                        startActivity(intent)
//                        requireActivity().overridePendingTransition(
//                            R.anim.slide_in_right, R.anim.slide_out_left
//                        )
//                    }
                }
                R.id.tvChat ->{
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "gameChat")
                    intent.putExtra("gameChatId",gameChatId)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
                R.id.tvGameOrganizer -> {
                    val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
                    intent.putExtra("playerProfile", organizerId)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }

                R.id.tvCourtPicture -> {
                    if (courtUserId != null) {
                        val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
                        intent.putExtra("playerProfile", courtUserId)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }

                }

                R.id.btnNext -> {
                    val textData = binding.btnNext.text.toString().trim()
                    when (textData) {
                        "Accept" -> {
                            if (gameId != null) {
                                val data = HashMap<String, Any>()
                                data["gameId"] = gameId.toString()
                                data["type"] = "accept"
                                viewModel.acceptInvite(data, Constants.ACCEPT_OR_REJECT)
                            }
                        }

                        "Start game" -> {
                            if (gameId != null) {
                                val data = HashMap<String, Any>()
                                data["gameId"] = gameId.toString()
                                viewModel.startGame(data, Constants.START_GAME)
                            }
                        }

                        "Join" -> {

                        }
                        "Game Score" ->{
                            val intent = Intent(requireContext(), UserProfileActivity::class.java)
                            intent.putExtra("userType", "gameScore")
                            intent.putExtra("gameId", gameId)
                            startActivity(intent)
                            requireActivity().overridePendingTransition(
                                R.anim.slide_in_right, R.anim.slide_out_left
                            )
                        }

                    }
                }

                R.id.btnRefuse -> {
                    val textData = binding.btnNext.text.toString().trim()

                    when (textData) {
                        "Refuse" -> {
                            if (gameId != null) {
                                val data = HashMap<String, Any>()
                                data["gameId"] = gameId.toString()
                                data["type"] = "reject"
                                viewModel.rejectInvite(data, Constants.ACCEPT_OR_REJECT)
                            }
                        }

                        "Leave" -> {
                            if (gameId != null) {
                                val data = HashMap<String, Any>()
                                data["gameId"] = gameId.toString()
                                viewModel.leaveGame(data, Constants.LEAVE_GAME)
                            }
                        }

                    }
                }

                R.id.tvCourtAddress, R.id.tvCourtDistance -> {
                    openMapApp.show()
                }

                R.id.tvGameStartDate -> {
                    addCalendarPopup.show()
                }
            }
        }
    }

    /*** view pager handel **/
    private fun initViewPager() {

        imagesPagerAdapter = ImagesPagerAdapter(requireContext(), arrayListOf())
        binding.viewPager.adapter = imagesPagerAdapter

        binding.dotsIndicator.apply {
            setSliderColor(
                ContextCompat.getColor(requireContext(), R.color.beballer_grey),
                ContextCompat.getColor(requireContext(), R.color.beballer_orange)
            )
            setSliderWidth(
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
            )
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSlideMode(IndicatorSlideMode.NORMAL)
            setIndicatorStyle(IndicatorStyle.CIRCLE)

            setupWithViewPager(binding.viewPager)
        }
    }


    /*** fab button click handel  **/
    private fun initFabMenu() {
        binding.courtMenusLayout.alpha = 0F
        binding.courtMenusLayout.translationY = translationYaxis
        binding.courtMenusLayout.isVisible = false
        binding.courtMenuFab.setOnClickListener {
            when (isFabMenuVisible) {
                true -> {
                    binding.courtMenuFab.animate().rotation(0F).setInterpolator(interpolator)
                        .setDuration(150).start()
                    binding.courtMenusLayout.animate().translationY(translationYaxis).alpha(0F)
                        .setInterpolator(interpolator).setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                binding.courtMenusLayout.isVisible = false

                            }
                        }).start()
                }

                false -> {
                    binding.courtMenusLayout.isVisible = true

                    binding.courtMenuFab.animate().rotation(-90F).setInterpolator(interpolator)
                        .setDuration(150).start()

                    binding.courtMenusLayout.animate().translationY(0F).setListener(null).alpha(1F)
                        .setInterpolator(interpolator).setDuration(300).start()
                }
            }
            isFabMenuVisible = !isFabMenuVisible
        }
    }





    private fun setupAdapters() {

        team1Adapter = TeamAdapter(true, this)
        team2Adapter = TeamAdapter(false, this)

        binding.rvHomeTeam.adapter = team1Adapter
        binding.rvOutsideTeam.adapter = team2Adapter
    }


    /** handle out side adapter **/
    private var chatList = ArrayList<ChatModel>()
    private fun initChatAdapter() {
        chatAdapter =
            SimpleRecyclerViewAdapter(R.layout.recycler_game_message_item, BR.bean) { v, m, pos ->
                when (v.id) {

                }
            }
        chatList = getChatList()
        chatAdapter.list = chatList
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


    override fun onInviteClick(isHomeTeam: Boolean) {

        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra("userType", "invitePlayer")
        intent.putExtra("from", "gameDetail")
        intent.putExtra("gameId" ,gameId)
        intent.putExtra("isHomeTeam", isHomeTeam)
        startActivity(intent)
        requireActivity().overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    override fun onRemoveClick(
        player: Player,
        isHomeTeam: Boolean
    ) {
        if (player._id != null) {
            playerId  = player.id
            removePlayerDialog.show()
        }

    }


    private fun prepareTeamList(
        players: List<Player>,
        totalSlots: Int,
        isEditable: Boolean
    ): List<TeamSlotModel> {

        val list = mutableListOf<TeamSlotModel>()

        // Add real players
        players.forEach {
            list.add(TeamSlotModel(player = it))
        }

        // ✅ Only add invite slots if editable
        if (isEditable) {
            val remaining = totalSlots - players.size
            repeat(remaining.coerceAtLeast(0)) {
                list.add(TeamSlotModel(player = null)) // invite slot
            }
        }

        return list
    }


    private fun GameDetailTeam1Player.toPlayer(): Player {
        return Player(
            _id = _id,
            city = city,
            country = country,
            distance = null,
            firstName = firstName,
            id = id,
            lastName = lastName,
            lat = null,
            long = null,
            profilePicture = profilePicture,
            score = score,
            username = username,
            accepted = accepted
        )
    }


    private fun Team2Player.toPlayer(): Player {
        return Player(
            _id = _id,
            city = null, // not available in Team2Player
            country = country,
            distance = null,
            firstName = firstName,
            id = id,
            lastName = lastName,
            lat = null,
            long = null,
            profilePicture = profilePicture,
            score = score,
            username = null ,
            accepted = accepted// not available in Team2Player
        )
    }


    private fun configureGameState(state: GameState) {


        Log.i("statua", "configureGameState: $state")

        when (state.status) {

            GameState.Status.DONE,
            GameState.Status.IN_PROGRESS -> {
                binding.btnNext.text = "Game Score"
                binding.btnNext.visibility = View.VISIBLE
                binding.btnRefuse.visibility = View.GONE
                return
            }

            GameState.Status.WAITING -> {

            }
            GameState.Status.NONE -> return
        }

        if (state.isOrganizer) {


            if (state.canStartGame) {
                binding.btnNext.text = "Start game"
                binding.btnNext.visibility = View.VISIBLE
                binding.btnRefuse.visibility = View.GONE
            } else {
                binding.btnNext.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue_00bef5)
                binding.btnNext.text = "Waiting for the other players"
                binding.btnNext.visibility = View.VISIBLE
                binding.btnRefuse.visibility = View.GONE
            }

            return
        }

        when (state.currentUserStatus) {

            GameState.UserStatus.JOINED -> {
                binding.btnRefuse.text = "Leave"
                binding.btnNext.visibility = View.GONE
                binding.btnRefuse.visibility = View.VISIBLE
            }

            GameState.UserStatus.INVITED -> {
                binding.btnNext.text = "Accept"
                binding.btnNext.visibility = View.VISIBLE
                binding.btnRefuse.visibility = View.VISIBLE
            }

            GameState.UserStatus.GAME_FULL -> {
                binding.btnNext.visibility = View.GONE
                binding.btnRefuse.visibility = View.GONE
            }

            GameState.UserStatus.NOT_JOINED -> {
                binding.btnNext.visibility = View.VISIBLE
                binding.btnNext.text = "Join"
                binding.btnRefuse.visibility = View.GONE
            }
        }
    }

    private fun mapGameState(game: GameDetail): GameState? {

        val organizer = game.organizer ?: return null
        val organizerId = organizer._id ?: return null
        val maxPlayers = game.mode ?: return null

        val currentUserId = sharedPrefManager.getLoginData()?.data?.user?._id
        val isOrganizer = organizerId == currentUserId


        val normalizedStatus = game.status
            ?.replace(Regex("([a-z])([A-Z])"), "$1_$2")  // inProgress → in_Progress
            ?.uppercase()                                // → IN_PROGRESS
        // Safe enum mapping
        val status = GameState.Status.values()
            .find { it.name.equals(normalizedStatus, ignoreCase = true) }
            ?: GameState.Status.WAITING
     //   val status = mapStatus(game.status)

        val team1Players = game.team1Players
            ?.filterNotNull()
            ?: emptyList()

        val team2Players = game.team2Players
            ?.filterNotNull()
            ?: emptyList()

        // ✅ Use accepted directly from API
        val team1HasAccepted = team1Players.any { it.accepted == true }
        val team2HasAccepted = team2Players.any { it.accepted == true }

        val canStartGame = team1HasAccepted && team2HasAccepted

        // Combine both teams


        val currentUserStatus = when {

            status == GameState.Status.DONE -> {
                GameState.UserStatus.JOINED
            }

            team1Players.any { it._id == currentUserId } -> {

                val currentPlayer =
                    team1Players.first { it._id == currentUserId }

                if (currentPlayer.accepted == true) {
                    GameState.UserStatus.JOINED
                } else {
                    GameState.UserStatus.INVITED
                }
            }

            team2Players.any { it._id == currentUserId } -> {

                val currentPlayer =
                    team2Players.first { it._id == currentUserId }

                if (currentPlayer.accepted == true) {
                    GameState.UserStatus.JOINED
                } else {
                    GameState.UserStatus.INVITED
                }
            }

            else -> {

                val isGameFull =
                    team1Players.size >= maxPlayers &&
                            team2Players.size >= maxPlayers &&
                            ((game.isAutoRefereeing ?: false) ||
                                    game.referee != null)

                if (isGameFull) {
                    GameState.UserStatus.GAME_FULL
                } else {
                    GameState.UserStatus.NOT_JOINED
                }
            }
        }


        return GameState(
            status = status,
            currentUserStatus = currentUserStatus,
            maxPlayersPerTeam = maxPlayers,
            isOrganizer = isOrganizer,
            canStartGame = canStartGame,
            team1Players = emptyList(),  // adjust if needed
            team2Players = emptyList()   // adjust if needed
        )
    }

    override fun onResume() {
        super.onResume()
        initData()
    }




    private fun mapStatus(apiStatus: String?): GameState.Status {
        return when (apiStatus?.trim()) {
            "done", "DONE" -> GameState.Status.DONE
            "inProgress", "IN_PROGRESS", "INPROGRESS" -> GameState.Status.IN_PROGRESS
            "waiting", "WAITING" -> GameState.Status.WAITING
            else -> GameState.Status.NONE
        }
    }

}