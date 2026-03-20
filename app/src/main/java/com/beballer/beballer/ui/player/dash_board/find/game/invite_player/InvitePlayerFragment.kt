package com.beballer.beballer.ui.player.dash_board.find.game.invite_player

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.GetCourtApiResponse
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.GetPlayersApiResponse
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentInvitePlayerBinding
import com.beballer.beballer.databinding.ItemLayoutPlayersBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.ViewItem
import com.beballer.beballer.ui.player.dash_board.find.game.find_game.FindGameAdapter
import com.beballer.beballer.ui.player.dash_board.profile.team.TeamFragment.Companion.teamType
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InvitePlayerFragment : BaseFragment<FragmentInvitePlayerBinding>() {



    private lateinit var playerAdapter: InvitePlayerAdapter
    private var fullList = ArrayList<Player?>()

    private var side : String ? = null
    private var query : String ?= null
    private var gameId : String ?= null
    private var refereeData : Player ? = null

    private var singlePlayerId : String ? = null

    private var currentPage = 1
    private var isLoading = false

    private var maxAwayPlayers: Int = 1

    private var isLastPage = false
    private var searchHandler: Handler? = null
    private var searchRunnable: Runnable? = null
    private var isProgress = false

    private var isHomeTeam = true
    private val viewModel : InvitePLayerVm by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_invite_player
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()

        initAdapter()
        getData()
        getPlayers()

        initObserver()
        setupSearch()

        pagination()
        /** Refresh **/
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color))
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                getPlayers()
            }, 2000)
        }
    }

    private fun getData() {
        side = arguments?.getString("from")
        gameId = arguments?.getString("gameId")
        maxAwayPlayers = arguments?.getInt("maxPlayer") ?: 1
        isHomeTeam = arguments?.getBoolean("isHomeTeam") ?: true


        if(side != null){
            when(side){
                "referee" ->{
                    playerAdapter.setSelectionType("referee")
                    playerAdapter.setMaxSelection(1)

                }
                "players" ->{
                    Log.i("fdsfds", "getData: $maxAwayPlayers")
                    playerAdapter.setSelectionType("players")
                    playerAdapter.setMaxSelection(maxAwayPlayers)

                }
                "gameDetail" ->{
                    playerAdapter.setSelectionType("gameDetail")
                    playerAdapter.setMaxSelection(1)
                }
                "changeReferee" ->{
                    playerAdapter.setSelectionType("changeReferee")
                    playerAdapter.setMaxSelection(1)

                }
            }
        }
    }


    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING ->  {
                    if (!isProgress) {
                        showLoading()
                    }
                }
                Status.SUCCESS ->  {
                    hideLoading()
                    when(it.message){
                        "getPlayerList" ->{
                            try {
                                val myDataModel: GetPlayersApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.players?.isNotEmpty() == true) {
                                        val pastSessionData = myDataModel.players
                                        val feedItems: List<PlayerItem> =
                                            pastSessionData.filterNotNull()
                                                .map { PlayerItem.Post(it) } ?: emptyList()
                                        isLoading = false
                                        isLastPage = false
                                        isProgress = true

                                        if (currentPage == 1) {
                                            myDataModel.players.let {
                                                fullList = it as ArrayList<Player?>
                                                playerAdapter.setList(feedItems)

                                            }
                                            Log.i("fdsfsd", "initObserver: $fullList")
                                        } else {
                                            playerAdapter.addToList(feedItems)
                                        }
                                        isLastPage =
                                            currentPage == myDataModel?.totalPages
                                    }

                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                playerAdapter.hideLoader()
                                hideLoading()
                            }
                        }
                        "invitePlayer" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        "changeReferee" ->{
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
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

    private fun getPlayers() {
        val data = HashMap<String , Any>()
        data["page"] = 1
        data["limit"] = 20
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, data)
    }

    private fun initAdapter() {
        playerAdapter = InvitePlayerAdapter(object : InvitePlayerAdapter.OnItemClickListener {
            override fun onItemClick(item: Player?, clickedViewId: Int, position: Int) {
                when(clickedViewId){
                    R.id.clMain -> {
                       refereeData = item
                        singlePlayerId = item?.id
                    }
                }
            }
        })

        playerAdapter.setSelectionType(side)  // "referee" or "players"
        binding.rvPlayers.adapter = playerAdapter
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.btnNext  ->{
                    when(side){
                        "referee" -> {

                            if (refereeData != null) {

                                val resultIntent = Intent().apply {
                                    putExtra("data", refereeData)
                                }

                                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                                requireActivity().finish()

                            } else {
                                showErrorToast("Please select referee ")
                            }
                        }

                        "players" -> {

                            val selectedList = ArrayList(playerAdapter.getSelectedPlayers())

                            Log.i("dsdsdad", "Selected: $selectedList")

                            if (selectedList.isEmpty()) {
                                showErrorToast("Please select players")

                            }

                            // ✅ Validate selection count
                            if (selectedList.size != maxAwayPlayers) {
                                showErrorToast("Please select exactly $maxAwayPlayers players")
                            }
                            val resultIntent = Intent()
                            resultIntent.putParcelableArrayListExtra("selectedPlayers", selectedList)
                            resultIntent.putExtra("isHomeTeam", isHomeTeam)

                            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                            requireActivity().finish()

                        }

                        "gameDetail" ->{
                            if (singlePlayerId != null){
                                val data = HashMap<String, Any>()
                                data["gameId"] =  gameId.toString()
                                data["playerId"] = singlePlayerId.toString()
                                if (isHomeTeam){
                                    data["team"] = 1

                                }else{
                                    data["team"] = 2
                                }
                                viewModel.invitePlayer(data, Constants.ADD_NEW_PLAYER)
                            }
                        }

                        "changeReferee" ->{
                            if (singlePlayerId != null){
                                val data = HashMap<String, Any>()
                                data["gameId"] =  gameId.toString()
                                data["playerId"] = singlePlayerId.toString()
                                viewModel.changeReferee(data, Constants.ADD_REFEREE)
                            }
                        }

                    }
                }
            }
        })
    }


    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvPlayers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }

        })
    }

    /**
     *  load more function call
     **/
    private fun loadMoreItems() {
        playerAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val data = HashMap<String , Any>()
        data["page"] = currentPage
        data["limit"] = 20
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, data)
    }


    /*** add search ***/
    private fun setupSearch() {
        val searchView = binding.courtsSearchView
        searchHandler = Handler(requireActivity().mainLooper)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (trimmedQuery.isNullOrEmpty() || trimmedQuery.isBlank()) {
                    getPlayers()
                } else {
                    currentPage = 1
                    searchCourts(trimmedQuery)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler?.removeCallbacks(it) }
                searchRunnable = Runnable {
                    if (newText != null) {
                        val trimmedQuery = newText.trim()
                        if (trimmedQuery.isNotEmpty() && trimmedQuery.isNotBlank()) {
                            currentPage = 1
                            searchCourts(trimmedQuery)
                        } else if (newText.isEmpty()) {
                            currentPage = 1
                            getPlayers()
                        }
                    }
                }
                searchHandler?.postDelayed(searchRunnable!!, 1000)
                return true
            }
        })
    }

    /**
     * search court
     */
    private fun searchCourts(query: String) {
        val params = HashMap<String, Any>()
        params["page"] = 1
        params["limit"] = 50
        params["search"] = query
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, params)
    }
}