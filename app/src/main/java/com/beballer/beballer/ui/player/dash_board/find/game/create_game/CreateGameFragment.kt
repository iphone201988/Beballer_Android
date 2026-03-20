package com.beballer.beballer.ui.player.dash_board.find.game.create_game

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.LinearLayoutManager
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CreateGameApiResponse
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.data.model.GameModes
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.MpvModel
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.TeamSlotModel
import com.beballer.beballer.databinding.AlertDialodItemBinding
import com.beballer.beballer.databinding.AutoRefereeingBottomSheetItemBinding
import com.beballer.beballer.databinding.FragmentCreateGameBinding
import com.beballer.beballer.databinding.GameModeBottomSheetItemBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.databinding.RvOutSideItemBinding
import com.beballer.beballer.databinding.RvTeamItemBinding
import com.beballer.beballer.databinding.UpdateFeatureDialogBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class CreateGameFragment : BaseFragment<FragmentCreateGameBinding>()  ,TeamAdapter.OnInviteClickListener {
    private val viewModel: CreateGameFragmentVM by viewModels()

    private lateinit var gameModeAdapter: SimpleRecyclerViewAdapter<GameModes, RvGameModeItemBinding>
    private lateinit var gameModeSheet: BaseCustomBottomSheet<GameModeBottomSheetItemBinding>
    private lateinit var autoRefereeingBottomSheet: BaseCustomBottomSheet<AutoRefereeingBottomSheetItemBinding>
    private lateinit var alertDialogItem: BaseCustomDialog<AlertDialodItemBinding>


    private lateinit var homeTeamAdapter: TeamAdapter
    private lateinit var outSideTeamAdapter: TeamAdapter
    private lateinit var invitePlayerLauncher: ActivityResultLauncher<Intent>
    private var gameStartDateTime: String = ""
    private val calendar = Calendar.getInstance()
    private var currentUser: Player? = null
    private var autoRefereeing  = true



    private val homeTeamList = mutableListOf<TeamSlotModel>()
    private val awayTeamList = mutableListOf<TeamSlotModel>()

    private var maxHomePlayers = 0
    private var maxAwayPlayers = 0

    private val selectedHomePlayers = mutableListOf<Player>()
    private val selectedAwayPlayers = mutableListOf<Player>()
    private var courtData : GetCourtData ?= null
    private var refereeData : Player ? = null

    private var refereeId  : String ? = null

    private var mode = 0

    private var courtId : String ? = null

    private var selectedGameMode : String ? = null



    private val refereeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data?.getParcelableExtra<Player>("data")

                data?.let {
                    refereeData = it
                    binding.gameRefereeTv.text = "@${it.username}"
                    refereeId = it.id
                    autoRefereeing = false

                }
            }
        }

    private val courtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data?.getParcelableExtra<GetCourtData>("courtData")

                data?.let {
                    courtData = it
                    courtId = it._id

                    BindingUtils.setImageFromUrl(binding.courtImage, it.photos?.get(0))
                }
            }
        }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        Log.i("dfdsfsdfsd", "onCreateView: gfgfgfgfg")
        // click
        setCurrentUser()
        initOnClick()
      //  initData()
        setupAdapters()

        setObserver()

        invitePlayerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val selectedPlayers =
                        result.data?.getParcelableArrayListExtra<Player>("selectedPlayers")

                    val isHomeTeam =
                        result.data?.getBooleanExtra("isHomeTeam", true)

                    selectedPlayers?.let { players ->

                        if (isHomeTeam == true) {
                            selectedHomePlayers.addAll(players)
                            updateHomeTeamUI()
                        } else {
                            selectedAwayPlayers.addAll(players)
                            updateAwayTeamUI()
                        }
                    }
                }
            }


    }

    /**** alert dialog item ****/
    private fun alertDialogItem() {
        alertDialogItem = BaseCustomDialog<AlertDialodItemBinding>(
            requireContext(), R.layout.alert_dialod_item
        ) {
            when (it?.id) {
                R.id.tvBtn -> {
                    alertDialogItem.dismiss()
                    gameModeSheet.dismiss()
                }
            }

        }
        alertDialogItem.create()
        alertDialogItem.show()
    }

    private fun setObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when(it?.status){
            Status.LOADING ->  {
                showLoading()
            }
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "createGame" ->{
                            val myDataModel : CreateGameApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showSuccessToast(myDataModel.message.toString())
                                    requireActivity().finish()
                                }
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

//    private fun initData() {
//
//        val argCourtData = arguments?.getParcelable<GetCourtData>("courtData")
//        val argRefereeData = arguments?.getParcelable<Player>("data")
//
//        if (argCourtData != null) {
//            viewModel.courtData = argCourtData
//        }
//
//        if (argRefereeData != null) {
//            viewModel.refereeData = argRefereeData
//            autoRefereeing = false
//        }
//
//
//        courtData = viewModel.courtData
//        refereeData = viewModel.refereeData
//
//        courtData?.let {
//            courtId = it._id
//            BindingUtils.setImageFromUrl(binding.courtImage, it.photos?.get(0))
//        }
//
//        refereeData?.let {
//            binding.gameRefereeTv.text = "@${it.username}"
//            refereeId = it._id
//            autoRefereeing = false
//        }
//
//    }

    override fun onResume() {
        super.onResume()
        Log.i("court", "initData: $courtData")
        Log.i("refreeDAta", "initData: $refereeData")
    }


    /*** click event handel **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.tvGameMode -> {
                    gameModeBottomSheet()
                }

                R.id.game_referee_tv -> {
                    autoRefereeBottomSheet()
                }

                R.id.tvGameCourt -> {

                    val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
                        putExtra("userType", "findGameFragment")
                    }

                    courtLauncher.launch(intent)

                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }


                R.id.tvGameStartDate ->{
                    calendarOpen()
                }
                R.id.game_start_time_tv ->{
                    openTimePicker()
                }
                R.id.btnNext -> {

                    val team1Players = selectedHomePlayers.mapNotNull { it.id }
                    val team2Players = selectedAwayPlayers.mapNotNull { it.id }

                    // 1️⃣ Court validation
                    if (courtData == null) {
                        showErrorToast("Please select a court")
                    }

                    // 2️⃣ Date validation
                    else if (gameStartDateTime.isNullOrEmpty()) {
                        showErrorToast("Please select date & time")
                    }


                    // 4️⃣ Mode validation
                    else if (mode == null || mode == 0) {
                        showErrorToast("Please select game mode")
                    }

                    // 5️⃣ Referee validation (if not auto refereeing)
                    else if (!autoRefereeing && refereeId.isNullOrEmpty()) {
                        showErrorToast("Please select a referee")
                    }

                    // 6️⃣ Team empty validation
                    else if (team1Players.isEmpty()) {
                        showErrorToast("Please select Team 1 players")
                    }
                    else if (team2Players.isEmpty()) {
                        showErrorToast("Please select Team 2 players")
                    }

                    // 7️⃣ Duplicate inside same team
                    else if (team1Players.size != team1Players.distinct().size) {
                        showErrorToast("Duplicate players in Team 1")
                    }
                    else if (team2Players.size != team2Players.distinct().size) {
                        showErrorToast("Duplicate players in Team 2")
                    }

                    // 8️⃣ Same player in both teams
                    else if (team1Players.intersect(team2Players.toSet()).isNotEmpty()) {
                        showErrorToast("Same player cannot be in both teams")
                    }

                    // 9️⃣ Mode based validation (1v1 to 5v5)
                    else if (mode in 1..5) {

                        if (team1Players.size != mode || team2Players.size != mode) {
                            showErrorToast("Each team must have $mode players")
                        } else {
                            createGameRequest(team1Players, team2Players)
                        }
                    }

                }




            }
        }
    }
    private fun createGameRequest(
        team1Players: List<String>,
        team2Players: List<String>
    ) {

        val data = hashMapOf<String, Any>(
            "refereeId" to refereeId.orEmpty(),
            "date" to gameStartDateTime,
            "creationDate" to getCurrentCreationDate(),  // ✅ here
            "visible" to false,
            "courtId" to courtData?.id.orEmpty(),
            "isAutoRefereeing" to autoRefereeing,
            "team1Players" to team1Players,
            "team2Players" to team2Players,
            "mode" to mode
        )

        viewModel.createGame(Constants.CREATE_GAME, data)
    }


    /** game mode bottom sheet **/
    private fun gameModeBottomSheet() {
        gameModeSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.game_mode_bottom_sheet_item) {
                when (it?.id) {

                }
            }
        gameModeSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        gameModeSheet.behavior.isDraggable = true
        gameModeSheet.create()
        gameModeSheet.show()

        initGameModeAdapter()
    }

    /** auto refereeing bottom sheet **/
    private fun autoRefereeBottomSheet() {
        autoRefereeingBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.auto_refereeing_bottom_sheet_item) {
                when (it?.id) {
                    R.id.tvName1 -> {

                        autoRefereeingBottomSheet.dismiss()

                        val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
                            putExtra("userType", "invitePlayer")
                            putExtra("from", "referee")
                        }

                        refereeLauncher.launch(intent)

                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                    }

                    R.id.tvName ->{
                        autoRefereeing = true
                        autoRefereeingBottomSheet.dismiss()
                    }
                }
            }
        autoRefereeingBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        autoRefereeingBottomSheet.behavior.isDraggable = true
        autoRefereeingBottomSheet.create()
        autoRefereeingBottomSheet.show()

    }

    /** handle game mode adapter **/
    private fun initGameModeAdapter() {
        gameModeAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clGame -> {

                        val restrictedModes = listOf(6, 7, 8, 9)

                        if (restrictedModes.contains(m.modeId)) {
                            alertDialogItem()
                            return@SimpleRecyclerViewAdapter
                        }

                        selectedGameMode = m.title
                        mode = m.modeId
                        onGameModeSelected(selectedGameMode!!)
                    }
                }
            }

        gameModeAdapter.list = getListGame()
        gameModeSheet.binding.rvGameModel.adapter = gameModeAdapter
    }


    private fun onGameModeSelected(mode: String) {

        selectedGameMode = mode

        val count = selectedGameMode!!
            .split("VS")[0]
            .trim()
            .toInt()

        maxHomePlayers = count
        maxAwayPlayers = count

        Log.i("dsadsadas", "onGameModeSelected: $maxAwayPlayers")

        selectedHomePlayers.clear()
        selectedAwayPlayers.clear()

        // Add current user to home team
        currentUser?.let {
            selectedHomePlayers.add(it)
        }

        updateHomeTeamUI()
        updateAwayTeamUI()

        binding.tvGameMode.text = mode
        gameModeSheet.dismiss()
        binding.ballersLayout.visibility = View.VISIBLE
    }

    private fun updateHomeTeamUI() {

        val list = mutableListOf<TeamSlotModel>()

        selectedHomePlayers.forEachIndexed { index, player ->
            list.add(
                TeamSlotModel(
                    player = player,
                    isCurrentUser = index == 0
                )
            )
        }

        // If not full → show ONE invite only
        if (selectedHomePlayers.size < maxHomePlayers) {
            list.add(TeamSlotModel()) // invite slot
        }

        homeTeamAdapter.submitList(list)
    }


    private fun updateAwayTeamUI() {

        val list = mutableListOf<TeamSlotModel>()

        selectedAwayPlayers.forEach { player ->
            list.add(TeamSlotModel(player))
        }

        if (selectedAwayPlayers.size < maxAwayPlayers) {
            list.add(TeamSlotModel()) // invite slot
        }

        outSideTeamAdapter.submitList(list)
    }

    private fun setupAdapters() {

        homeTeamAdapter = TeamAdapter(true, this,true)
        outSideTeamAdapter = TeamAdapter(false, this,true)

        binding.rvHomeTeam.adapter = homeTeamAdapter
        binding.rvOutsideTeam.adapter = outSideTeamAdapter
    }



    // add list game mode
    private fun getListGame(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("1 VS 1",1),
            GameModes("2 VS 2", 2),
            GameModes("3 VS 3",3),
            GameModes("4 VS 4",4),
            GameModes("5 VS 5",5),
            GameModes("H.O.R.S.E", 6),
            GameModes("Lucky Luke",7),
            GameModes("Concours 3 pts",8),
            GameModes("Concours Dunks",9),


            )
    }




    private fun setCurrentUser() {

        if (sharedPrefManager.getLoginData() != null){
            val userData = sharedPrefManager.getLoginData()?.data?.user
            currentUser = Player(
                _id = userData?._id,
                firstName = userData?.firstName,
                lastName = userData?.lastName,
                country = userData?.country,
                profilePicture = userData?.profilePicture,
                username = userData?.username,
                city =  userData?.city,
                distance = null ,
                id = userData?.id,
                lat = null,
                long = null,
                score = userData?.score
            )
        }

    }




    private fun calendarOpen() {
        val currentCalendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            { _, year, month, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvGameStartDate.text = dateFormat.format(calendar.time)

                updateDateTimeForApi()   // 👈 update global variable
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = currentCalendar.timeInMillis
        datePickerDialog.show()
    }




    private fun openTimePicker() {

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->

                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val hour12 = when {
                    selectedHour == 0 -> 12
                    selectedHour > 12 -> selectedHour - 12
                    else -> selectedHour
                }

                val formattedMinute = String.format("%02d", selectedMinute)
                binding.gameStartTimeTv.text = "$hour12:$formattedMinute $amPm"

                updateDateTimeForApi()   // 👈 update global variable
            },
            hour,
            minute,
            false
        )

        timePickerDialog.show()
    }


    private fun updateDateTimeForApi() {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        gameStartDateTime = isoFormat.format(calendar.time)

        Log.d("API_DATE_TIME", gameStartDateTime)
    }


    private fun getCurrentCreationDate(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    override fun onInviteClick(isHomeTeam: Boolean) {

        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra("userType", "invitePlayer")
        intent.putExtra("from", "players")
        intent.putExtra("isHomeTeam", isHomeTeam)

        // send correct max player based on team
        if (isHomeTeam) {
            val remainingSlots = maxHomePlayers - selectedHomePlayers.size

            intent.putExtra("maxPlayer", remainingSlots)
        } else {
            intent.putExtra("maxPlayer", maxAwayPlayers)
        }

        invitePlayerLauncher.launch(intent)

        requireActivity().overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    override fun onRemoveClick(
        player: Player,
        isHomeTeam: Boolean
    ) {
        if (isHomeTeam) {
            selectedHomePlayers.remove(player)
        } else {
            selectedAwayPlayers.remove(player)
        }

        updateHomeTeamUI()
        updateAwayTeamUI()
    }


}