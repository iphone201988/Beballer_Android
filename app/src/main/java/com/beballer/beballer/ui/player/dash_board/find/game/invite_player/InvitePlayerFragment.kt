package com.beballer.beballer.ui.player.dash_board.find.game.invite_player

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.GetPlayersApiResponse
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentInvitePlayerBinding
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint


@Suppress("DEPRECATION")
@AndroidEntryPoint
class InvitePlayerFragment : BaseFragment<FragmentInvitePlayerBinding>() {

    private lateinit var playerAdapter: InvitePlayerAdapter
    private var fullList = ArrayList<Player?>()

    private var side: String? = null
    private var gameId: String? = null
    private var refereeData: Player? = null

    private var singlePlayerId: String? = null

    private var currentPage = 1
    private var isLoading = false

    private var maxAwayPlayers: Int = 1

    private var isLastPage = false
    private var searchHandler: Handler? = null
    private var searchRunnable: Runnable? = null
    private var isProgress = false

    private var currentSearchQuery: String? = null

    private var isHomeTeam = true
    private var invitedPlayerIds = ArrayList<String>()

    private val viewModel: InvitePLayerVm by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_invite_player
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // adapter
        initAdapter()
        getData()
        getPlayers()
        // observer
        initObserver()
        // search
        setupSearch()
        // pagination
        pagination()
        /** Refresh **/
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                isProgress = true
                // api call
                getPlayers()
            }, 2000)
        }
    }

    /**
     * all click event handel
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.btnNext -> {
                    when (side) {
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
                            if (selectedList.isEmpty()) {
                                showErrorToast("Please select players")
                            } else {
                                val resultIntent = Intent()
                                resultIntent.putParcelableArrayListExtra(
                                    "selectedPlayers", selectedList
                                )
                                resultIntent.putExtra("isHomeTeam", isHomeTeam)

                                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                                requireActivity().finish()
                            }
                        }

                        "gameDetail" -> {
                            val selectedList = ArrayList(playerAdapter.getSelectedPlayers())
                            if (selectedList.isEmpty()) {
                                showErrorToast("Please select players")
                            } else {
                                selectedList.forEach { player ->
                                    val pid = player.id ?: player._id
                                    val data = HashMap<String, Any>()
                                    data["gameId"] = gameId.toString()
                                    data["playerId"] = pid!!
                                    if (isHomeTeam) {
                                        data["team"] = 1
                                    } else {
                                        data["team"] = 2
                                    }
                                    viewModel.invitePlayer(data, Constants.ADD_NEW_PLAYER)
                                }
                            }
                        }

                        "changeReferee" -> {
                            if (singlePlayerId != null) {
                                val data = HashMap<String, Any>()
                                data["gameId"] = gameId.toString()
                                data["playerId"] = singlePlayerId.toString()
                                viewModel.changeReferee(data, Constants.ADD_REFEREE)
                            }
                        }

                    }
                }

                R.id.cancelImage -> {
                    requireActivity().finish()
                }
            }
        })
    }

    /**
     * get data
     */
    private fun getData() {
        side = arguments?.getString("from")
        gameId = arguments?.getString("gameId")
        maxAwayPlayers = arguments?.getInt("maxPlayer") ?: 1
        isHomeTeam = arguments?.getBoolean("isHomeTeam") ?: true
        invitedPlayerIds = arguments?.getStringArrayList("invitedPlayerIds") ?: ArrayList()

        playerAdapter.setPreSelectedIds(invitedPlayerIds)

        if (side != null) {
            when (side) {
                "referee" -> {
                    playerAdapter.setSelectionType("referee")
                    playerAdapter.setMaxSelection(1)

                }

                "players" -> {
                    playerAdapter.setSelectionType("players")
                    playerAdapter.setMaxSelection(maxAwayPlayers)

                }

                "gameDetail" -> {
                    playerAdapter.setSelectionType("gameDetail")
                    playerAdapter.setMaxSelection(maxAwayPlayers)
                }

                "changeReferee" -> {
                    playerAdapter.setSelectionType("changeReferee")
                    playerAdapter.setMaxSelection(1)

                }
            }
        }
    }


    private fun updateNextButtonState() {
        val selectedCount = playerAdapter.getSelectedPlayers().size
        if (selectedCount > 0) {
            binding.btnNext.backgroundTintList = null
        } else {
            binding.btnNext.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.dark_AAB9EF)
        }
    }

    /**
     * all api observer
     */
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "getPlayerList" -> {
                            try {
                                val myDataModel: GetPlayersApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    val players = myDataModel.players ?: emptyList()
                                    val feedItems: List<PlayerItem> = players.filterNotNull()
                                        .map { data -> PlayerItem.Post(data) }

                                    isLoading = false
                                    isProgress = true

                                    if (currentPage == 1) {
                                        fullList = ArrayList(players)
                                        playerAdapter.setList(feedItems)
                                    } else {
                                        playerAdapter.addToList(feedItems)
                                    }
                                    isLastPage = currentPage >= (myDataModel.totalPages ?: 1)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                binding.ssPullRefresh.isRefreshing = false
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

                        "changeReferee" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                showSuccessToast(myDataModel.message.toString())
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    binding.ssPullRefresh.isRefreshing = false
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        })
    }

    /**
     * api call
     */
    private fun getPlayers() {
        currentSearchQuery = null
        currentPage = 1
        val data = HashMap<String, Any>()
        data["page"] = 1
        data["limit"] = 20
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, data)
    }

    /**
    handel player adapter
     */
    private fun initAdapter() {
        playerAdapter = InvitePlayerAdapter(object : InvitePlayerAdapter.OnItemClickListener {
            override fun onItemClick(item: Player?, clickedViewId: Int, position: Int) {
                updateNextButtonState()
                when (clickedViewId) {
                    R.id.clMain -> {
                        refereeData = item
                        singlePlayerId = item?._id
                    }
                }
            }
        })

        playerAdapter.setSelectionType(side)
        binding.rvPlayers.adapter = playerAdapter
        updateNextButtonState()
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
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        data["limit"] = 20
        currentSearchQuery?.let { data["search"] = it }
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, data)
    }


    /*** add search ***/
    private fun setupSearch() {
        val searchView = binding.courtsSearchView
        searchHandler = Handler(requireActivity().mainLooper)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim() ?: ""
                if (trimmedQuery.isEmpty()) {
                    getPlayers()
                } else {
                    currentPage = 1
                    searchPlayers(trimmedQuery)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler?.removeCallbacks(it) }
                searchRunnable = Runnable {
                    val trimmedQuery = newText?.trim() ?: ""
                    if (trimmedQuery.isNotEmpty()) {
                        currentPage = 1
                        searchPlayers(trimmedQuery)
                    } else if (newText != null && newText.isEmpty()) {
                        currentPage = 1
                        getPlayers()
                    }
                }
                searchHandler?.postDelayed(searchRunnable!!, 800)
                return true
            }
        })
    }

    /**
     * search players
     */
    private fun searchPlayers(query: String) {
        currentSearchQuery = query
        val params = HashMap<String, Any>()
        params["page"] = 1
        params["limit"] = 20
        params["search"] = query
        binding.courtsSearchView.clearFocus()
        viewModel.getPlayerList(Constants.NEARBY_PLAYER, params)
    }
}