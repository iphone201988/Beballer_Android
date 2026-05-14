@file:Suppress("DEPRECATION")

package com.beballer.beballer.ui.player.dash_board.find.game.create_game

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CreateGameApiResponse
import com.beballer.beballer.data.model.GameModes
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.TeamSlotModel
import com.beballer.beballer.databinding.AlertDialodItemBinding
import com.beballer.beballer.databinding.AutoRefereeingBottomSheetItemBinding
import com.beballer.beballer.databinding.FragmentCreateGameBinding
import com.beballer.beballer.databinding.GameModeBottomSheetItemBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.databinding.TimePickerBottomSheetBinding
import com.beballer.beballer.ui.player.dash_board.find.player_profile.PlayerProfileActivity
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class CreateGameFragment : BaseFragment<FragmentCreateGameBinding>(),
    TeamAdapter.OnInviteClickListener {
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
    private var autoRefereeing = true


    private var maxHomePlayers = 0
    private var maxAwayPlayers = 0

    private val selectedHomePlayers = mutableListOf<Player>()
    private val selectedAwayPlayers = mutableListOf<Player>()
    private var courtData: GetCourtData? = null
    private var refereeData: Player? = null

    private var refereeId: String? = null

    private var mode = 0

    private var courtId: String? = null

    private var selectedGameMode: String? = null


    @SuppressLint("SetTextI18n")
    private val refereeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data?.getParcelableExtra<Player>("data")

                data?.let {
                    refereeData = it
                    binding.refereeName = "@${it.username}"
                    refereeId = it.id ?: it._id
                    autoRefereeing = false
                    validateForm()
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

                    BindingUtils.setImageCourtUrl(binding.courtImage, it.photos as List<String>?)
                    validateForm()
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
        // click
        setCurrentUser()
        initOnClick()
        //  initData()
        setupAdapters()

        setObserver()
        initDateTime()

        arguments?.getParcelable<GetCourtData>("courtData")?.let {
            courtData = it
            courtId = it.id ?: it._id
            BindingUtils.setImageCourtUrl(binding.courtImage, it.photos as List<String>?)
            validateForm()
        }

        invitePlayerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val selectedPlayers =
                        result.data?.getParcelableArrayListExtra<Player>("selectedPlayers")

                    val isHomeTeamReturn = result.data?.getBooleanExtra("isHomeTeam", true)

                    selectedPlayers?.let { players ->

                        if (isHomeTeamReturn == true) {
                            players.forEach { p ->
                                if (selectedHomePlayers.none {
                                        (it._id ?: it.id) == (p._id ?: p.id)
                                    }) {
                                    selectedHomePlayers.add(p)
                                }
                            }
                            updateHomeTeamUI()
                        } else {
                            players.forEach { p ->
                                if (selectedAwayPlayers.none {
                                        (it._id ?: it.id) == (p._id ?: p.id)
                                    }) {
                                    selectedAwayPlayers.add(p)
                                }
                            }
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

    private fun initDateTime() {
        // Snap current time to the next 15-minute interval
        val minutes = calendar.get(Calendar.MINUTE)
        val mod = minutes % 15
        if (mod != 0) {
            calendar.add(Calendar.MINUTE, 15 - mod)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvGameStartDate.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.gameStartTimeTv.text = timeFormat.format(calendar.time)

        updateDateTimeForApi()
    }

    private fun setObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "createGame" -> {
                            val myDataModel: CreateGameApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data != null) {
                                    showSuccessToast(myDataModel.message.toString())
                                    requireActivity().finish()
                                } else {
                                    showErrorToast(myDataModel.message.toString())
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
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }


                R.id.tvGameStartDate -> {
                    calendarOpen()
                }

                R.id.game_start_time_tv -> {
                    openTimePickerBottomSheet()
                }

                R.id.btnNext -> {

                    val team1Players =
                        selectedHomePlayers.mapNotNull { team1 -> team1.id ?: team1._id }
                    val team2Players =
                        selectedAwayPlayers.mapNotNull { team2 -> team2.id ?: team2._id }

                    if (binding.buttonCheck == true) {
                        createGameRequest(team1Players, team2Players)
                        return@observe
                    }

                    // Court validation
                    if (courtData == null) {
                        showErrorToast("Please select a court")
                    }

                    // Date validation
                    else if (gameStartDateTime.isEmpty()) {
                        showErrorToast("Please select date & time")
                    }


                    // Mode validation
                    else if (mode == 0) {
                        showErrorToast("Please select game mode")
                    }

                    //  Referee validation (if not auto refereeing)
                    else if (!autoRefereeing && refereeId.isNullOrEmpty()) {
                        showErrorToast("Please select a referee")
                    }

                    // Team empty validation
                    else if (team1Players.size < maxHomePlayers) {
                        showErrorToast("Team 1 is not full. Please invite more players.")
                    } else if (team2Players.isEmpty()) {
                        showErrorToast("Please select at least one player for Team 2")
                    }

                    // Duplicate inside same team
                    else if (team1Players.size != team1Players.distinct().size) {
                        showErrorToast("Duplicate players in Team 1")
                    } else if (team2Players.size != team2Players.distinct().size) {
                        showErrorToast("Duplicate players in Team 2")
                    }

                    //  Same player in both teams
                    else if (team1Players.intersect(team2Players.toSet()).isNotEmpty()) {
                        showErrorToast("Same player cannot be in both teams")
                    }

                    //  Mode based validation (1v1 to 5v5)
                    else if (mode in 1..5) {
                        createGameRequest(team1Players, team2Players)
                    }

                }


            }
        }
    }

    private fun createGameRequest(
        team1Players: List<String>, team2Players: List<String>
    ) {

        val data = hashMapOf(
            "refereeId" to refereeId.orEmpty(),
            "date" to gameStartDateTime,
            "creationDate" to getCurrentCreationDate(),
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

                        val invitedIds = ArrayList<String>()
                        selectedHomePlayers.forEach { player ->
                            player._id?.let { id -> invitedIds.add(id) }
                        }
                        selectedAwayPlayers.forEach { player ->
                            player._id?.let { id -> invitedIds.add(id) }
                        }

                        val intent =
                            Intent(requireContext(), UserProfileActivity::class.java).apply {
                                putExtra("userType", "invitePlayer")
                                putExtra("from", "referee")
                                putExtra("maxPlayer", 1)
                                putStringArrayListExtra("invitedPlayerIds", invitedIds)
                            }

                        refereeLauncher.launch(intent)

                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }

                    R.id.tvName -> {
                        autoRefereeing = true
                        binding.refereeName = null
                        refereeId = null
                        refereeData = null
                        autoRefereeingBottomSheet.dismiss()
                        validateForm()
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
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, _ ->
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

        val count = selectedGameMode!!.split("VS")[0].trim().toInt()

        maxHomePlayers = count
        maxAwayPlayers = count


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
                    player = player, isCurrentUser = index == 0
                )
            )
        }

        // If not full → show ONE invite only
        if (selectedHomePlayers.size < maxHomePlayers) {
            list.add(TeamSlotModel()) // invite slot
        }

        homeTeamAdapter.submitList(list)
        validateForm()
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
        validateForm()
    }

    private fun setupAdapters() {
        homeTeamAdapter = TeamAdapter(true, this, true)
        outSideTeamAdapter = TeamAdapter(false, this, true)

        binding.rvHomeTeam.adapter = homeTeamAdapter
        binding.rvOutsideTeam.adapter = outSideTeamAdapter
    }


    // add list game mode
    private fun getListGame(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("1 VS 1", 1),
            GameModes("2 VS 2", 2),
            GameModes("3 VS 3", 3),
            GameModes("4 VS 4", 4),
            GameModes("5 VS 5", 5),
            GameModes("H.O.R.S.E", 6),
            GameModes("Lucky Luke", 7),
            GameModes("Concours 3 pts", 8),
            GameModes("Concours Dunks", 9),
        )
    }


    private fun setCurrentUser() {

        if (sharedPrefManager.getLoginData() != null) {
            val userData = sharedPrefManager.getLoginData()?.data?.user
            currentUser = Player(
                _id = userData?._id,
                firstName = userData?.firstName,
                lastName = userData?.lastName,
                country = userData?.country,
                profilePicture = userData?.profilePicture,
                username = userData?.username,
                city = userData?.city,
                distance = null,
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

                updateDateTimeForApi()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = currentCalendar.timeInMillis
        datePickerDialog.show()
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun openTimePickerBottomSheet() {
        lateinit var bottomSheet: BaseCustomBottomSheet<TimePickerBottomSheetBinding>
        bottomSheet = BaseCustomBottomSheet<TimePickerBottomSheetBinding>(
            requireContext(), R.layout.time_picker_bottom_sheet, "custom"
        ) { view ->
            when (view.id) {
                R.id.tvCancel -> bottomSheet.dismiss()
                R.id.tvDone -> {
                    val bindingSheet = bottomSheet.binding
                    val hour = bindingSheet.hourPicker.value
                    val minuteIndex = bindingSheet.minutePicker.value
                    val amPm = bindingSheet.amPmPicker.value // 0 = AM, 1 = PM

                    val actualMinute = minuteIndex * 15
                    var hour24 = hour
                    if (amPm == 1 && hour < 12) hour24 += 12
                    if (amPm == 0 && hour == 12) hour24 = 0

                    calendar.set(Calendar.HOUR_OF_DAY, hour24)
                    calendar.set(Calendar.MINUTE, actualMinute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val amPmStr = if (amPm == 0) "AM" else "PM"
                    val formattedMinute = String.format("%02d", actualMinute)
                    binding.gameStartTimeTv.text = "$hour:$formattedMinute $amPmStr"

                    updateDateTimeForApi()
                    bottomSheet.dismiss()
                }
            }
        }

        val bindingSheet = bottomSheet.binding

        // Hour Picker
        bindingSheet.hourPicker.minValue = 1
        bindingSheet.hourPicker.maxValue = 12
        val currentHour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val currentHour12 = if (currentHour24 % 12 == 0) 12 else currentHour24 % 12
        bindingSheet.hourPicker.value = currentHour12

        // Minute Picker
        bindingSheet.minutePicker.minValue = 0
        bindingSheet.minutePicker.maxValue = 3
        bindingSheet.minutePicker.displayedValues = arrayOf("00", "15", "30", "45")
        bindingSheet.minutePicker.value = (calendar.get(Calendar.MINUTE) / 15) % 4

        // AM/PM Picker
        bindingSheet.amPmPicker.minValue = 0
        bindingSheet.amPmPicker.maxValue = 1
        bindingSheet.amPmPicker.displayedValues = arrayOf("AM", "PM")
        bindingSheet.amPmPicker.value = if (calendar.get(Calendar.AM_PM) == Calendar.AM) 0 else 1

        bottomSheet.show()
    }

    private fun updateDateTimeForApi() {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        gameStartDateTime = isoFormat.format(calendar.time)

        Log.d("API_DATE_TIME", gameStartDateTime)
        validateForm()
    }


    private fun getCurrentCreationDate(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    override fun onInviteClick(isHomeTeam: Boolean) {

        val invitedIds = ArrayList<String>()
        selectedHomePlayers.forEach { (it.id ?: it._id)?.let { id -> invitedIds.add(id) } }
        selectedAwayPlayers.forEach { (it.id ?: it._id)?.let { id -> invitedIds.add(id) } }
        refereeId?.let { invitedIds.add(it) }

        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra("userType", "invitePlayer")
        intent.putExtra("from", "players")
        intent.putExtra("isHomeTeam", isHomeTeam)
        intent.putStringArrayListExtra("invitedPlayerIds", invitedIds)

        // send correct max player based on team
        if (isHomeTeam) {
            val remainingSlots = maxHomePlayers - selectedHomePlayers.size
            intent.putExtra("maxPlayer", remainingSlots)
        } else {
            val remainingSlots = maxAwayPlayers - selectedAwayPlayers.size
            intent.putExtra("maxPlayer", remainingSlots)
        }

        invitePlayerLauncher.launch(intent)

        requireActivity().overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )
    }

    override fun onRemoveClick(
        player: Player, isHomeTeam: Boolean
    ) {
        if (isHomeTeam) {
            selectedHomePlayers.remove(player)
        } else {
            selectedAwayPlayers.remove(player)
        }

        updateHomeTeamUI()
        updateAwayTeamUI()
    }

    override fun onPlayerClick(player: Player) {
        if (player._id != null || player.id != null) {
            val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
            intent.putExtra("playerProfile", player._id ?: player.id)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
        }
    }

    private fun validateForm() {

        val team1Players = selectedHomePlayers.mapNotNull { it.id ?: it._id }
        val team2Players = selectedAwayPlayers.mapNotNull { it.id ?: it._id }

        val isValid =
            courtData != null && gameStartDateTime.isNotEmpty() && mode in 1..5 && (autoRefereeing || !refereeId.isNullOrEmpty()) && team1Players.size == maxHomePlayers && team2Players.isNotEmpty() && team1Players.intersect(
                team2Players.toSet()
            ).isEmpty()

        binding.buttonCheck = isValid
    }

}
