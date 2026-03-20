package com.beballer.beballer.ui.player.dash_board.progression

import android.content.Intent
import android.location.Location
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.BoundPlayer
import com.beballer.beballer.data.model.LocationScope
import com.beballer.beballer.data.model.MapBounds
import com.beballer.beballer.data.model.MyGame
import com.beballer.beballer.data.model.MyGamesApiResponse
import com.beballer.beballer.data.model.PlayerByBoundApiResponse
import com.beballer.beballer.data.model.RankingModel
import com.beballer.beballer.data.model.TopRankingApiResponse
import com.beballer.beballer.databinding.FragmentProgressionsBinding
import com.beballer.beballer.databinding.ProgressionsRvItemBinding
import com.beballer.beballer.ui.player.dash_board.find.my_game.GameViewItem
import com.beballer.beballer.ui.player.dash_board.find.player_profile.PlayerProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.LocationBoundsProvider
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProgressionsFragment : BaseFragment<FragmentProgressionsBinding>() {
    private val viewModel: ProgressionsFragmentVM by viewModels()

    private lateinit var boundsProvider: LocationBoundsProvider
    private var userLocation: Location? = null
    private var searchQuery: String = ""
    private var selectedScope: LocationScope = LocationScope.CITY
    private var searchHandler: Handler? = null
    private var searchRunnable: Runnable? = null

    private lateinit var progressionAdapter: SimpleRecyclerViewAdapter<BoundPlayer, ProgressionsRvItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_progressions
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.type = 1
        binding.tvCity.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        binding.tvRegion.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        binding.tvCountry.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)

        boundsProvider = LocationBoundsProvider(requireContext())


         userLocation = Location("provider").apply {
            latitude = BindingUtils.lat
            longitude = BindingUtils.long

        }

        Log.i("jhgjghjg", "onCreateView:  $userLocation")

        userLocation?.let { location ->
            loadPlayers(location, LocationScope.CITY)
        }


        // observer
        initObserver()
        // click
        initOnClick()
        // adapter
        initRankingAdapter()
    }



    private fun loadPlayers(location: Location, scope: LocationScope) {

        lifecycleScope.launch {

            val bounds = boundsProvider.fetchBounds(location, scope)

            Log.d("BoundsDebug", "NE: ${bounds.northEastLat}, ${bounds.northEastLng}")
            Log.d("BoundsDebug", "SW: ${bounds.southWestLat}, ${bounds.southWestLng}")

            getTopPlayer(bounds)


        }
    }



    private fun getTopPlayer(bounds: MapBounds) {

        val commonParams = hashMapOf<String, Any>(
            "northEastLng" to bounds.northEastLng,
            "southWestLng" to bounds.southWestLng,
            "northEastLat" to bounds.northEastLat,
            "southWestLat" to bounds.southWestLat
        )

        viewModel.getTopRanking(Constants.TOP_PLAYERS, commonParams)

        val requestWithSearch = HashMap(commonParams)

        if (searchQuery.isNotEmpty()) {
            requestWithSearch["search"] = searchQuery
        }

        viewModel.getPlayerByBounds(
            Constants.GET_PLAYER_BY_BOUNDS,
            requestWithSearch
        )
    }


    /**** click handel event ***/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {

                R.id.tvCity -> {
                    binding.type = 1
                    selectedScope = LocationScope.CITY

                    userLocation?.let {
                        loadPlayers(it, selectedScope)
                    }
                    binding.tvCity.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvRegion.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvCountry.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.tvRegion -> {
                    binding.type = 2
                    Log.i("dsdas", "initOnClick: $userLocation ")
                    selectedScope = LocationScope.REGION

                    userLocation?.let {
                        loadPlayers(it, selectedScope)
                    }
                    binding.tvCity.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvRegion.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvCountry.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.tvCountry -> {
                    binding.type = 3
                    selectedScope = LocationScope.COUNTRY

                    userLocation?.let {
                        loadPlayers(it, selectedScope)
                    }
                    binding.tvCity.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvRegion.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvCountry.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                }
            }
        }
    }

    /** handle adapter **/
    private lateinit var fullList: List<RankingModel>
    private fun initRankingAdapter() {
        progressionAdapter =
            SimpleRecyclerViewAdapter(R.layout.progressions_rv_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.progressCard -> {
                        val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
                        intent.putExtra("playerProfile", m._id)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                }
            }

        binding.rvProgression.adapter = progressionAdapter
        setupSearch()
    }



    /*** add search ***/
    private fun setupSearch() {

        val searchView = binding.progressionSearchView
        searchHandler = Handler(requireActivity().mainLooper)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query?.trim() ?: ""

                callSearchApi()

                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                searchRunnable?.let { searchHandler?.removeCallbacks(it) }

                searchRunnable = Runnable {

                    searchQuery = newText?.trim() ?: ""

                    callSearchApi()
                }

                searchHandler?.postDelayed(searchRunnable!!, 1000)

                return true
            }
        })
    }

    private fun callSearchApi() {
        userLocation?.let {
            loadPlayers(it, selectedScope)
        }
    }

    /** handle api response **/
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "getTopRanking" ->{
                            val myDataModel : TopRankingApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    binding.bean = myDataModel.data
                                }
                            }
                        }
                        "getPlayerByBounds" -> {

                            val myDataModel: PlayerByBoundApiResponse? =
                                BindingUtils.parseJson(it.data.toString())

                            if (myDataModel?.data != null) {

                                val currentUserId = sharedPrefManager.getLoginData()?.data?.user?.id

                                myDataModel.data.players.forEach { player ->
                                    player.isCurrentUser = player.id == currentUserId
                                }

                                progressionAdapter.list = myDataModel.data.players
                            }
                        }


                        else ->  {

                        }
                    }
                }
                Status.ERROR -> {

                }
                else -> {

                }
            }
        })

    }
}
