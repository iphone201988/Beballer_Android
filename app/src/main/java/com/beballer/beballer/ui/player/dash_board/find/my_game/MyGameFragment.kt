package com.beballer.beballer.ui.player.dash_board.find.my_game

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.MyGame
import com.beballer.beballer.data.model.MyGamesApiResponse
import com.beballer.beballer.databinding.FragmentMyGameBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGameFragment : BaseFragment<FragmentMyGameBinding>(), OnMapReadyCallback {
    private val viewModel: MyGameFragmentVm by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mMap: GoogleMap? = null

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var isProgress = false

    private var fullList = ArrayList<MyGame?>()
    private lateinit var gameAdapter: GameAdapter

    private lateinit var publicGameAdapter: GameAdapter
    override fun getLayoutResource(): Int {
        return R.layout.fragment_my_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // set block pos
        binding.pos = 1
        binding.tvFeed.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        binding.tvSubscriptions.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        // click
        initOnClick()
        //  api call
        getMyGames()
        // observer
        initGameAdapter()
        // adapter
        setObserver()
        // pagination
        pagination()

        /** Refresh **/
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                getMyGames()
            }, 2000)
        }


        binding.ssRefreshPublicGames.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color)
        )
        binding.ssRefreshPublicGames.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssRefreshPublicGames.isRefreshing = false
                isProgress = true
                // api call
                getAllGames()
            }, 2000)
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
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
                        "getMyGames" -> {
                            val myDataModel: MyGamesApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data?.games?.isNotEmpty() == true) {
                                    binding.tvNoData.visibility = View.GONE
                                    val pastSessionData = myDataModel.data.games
                                    val feedItems: List<GameViewItem> =
                                        pastSessionData.map { list -> GameViewItem.Post(list) }
                                    isLoading = false
                                    isLastPage = false
                                    isProgress = false

                                    if (currentPage == 1) {
                                        myDataModel.data.games.let { list ->
                                            fullList = list as ArrayList<MyGame?>
                                            gameAdapter.setList(feedItems)

                                        }
                                    } else {
                                        gameAdapter.addToList(feedItems)
                                    }
                                    isLastPage =
                                        currentPage == myDataModel.data.pagination?.totalPages
                                } else {
                                    if (currentPage == 1) {
                                        binding.tvNoData.visibility = View.VISIBLE
                                        gameAdapter.setList(emptyList())
                                    }
                                    isLoading = false
                                    isProgress = false
                                }
                            }
                        }

                        "getAllGames" -> {
                            val myDataModel: MyGamesApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data?.games?.isNotEmpty() == true) {
                                    binding.tvPublicNoData.visibility = View.GONE
                                    val pastSessionData = myDataModel.data.games
                                    val feedItems: List<GameViewItem> =
                                        pastSessionData.map { list -> GameViewItem.Post(list) }
                                    isLoading = false
                                    isLastPage = false
                                    isProgress = false

                                    if (currentPage == 1) {
                                        myDataModel.data.games.let { list ->
                                            fullList = list as ArrayList<MyGame?>
                                            publicGameAdapter.setList(feedItems)

                                        }
                                    } else {
                                        publicGameAdapter.addToList(feedItems)
                                    }
                                    isLastPage =
                                        currentPage == myDataModel.data.pagination?.totalPages
                                } else {
                                    if (currentPage == 1) {
                                        binding.tvPublicNoData.visibility = View.VISIBLE
                                        publicGameAdapter.setList(emptyList())
                                    }
                                    isLoading = false
                                    isProgress = false
                                }
                            }
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

    /**
     * get my games api
     */
    private fun getMyGames() {
        currentPage = 1
        binding.tvNoData.visibility = View.GONE
        val data = HashMap<String, Any>()
        data["limit"] = 20
        data["page"] = 1
        data["getAll"] = false
        viewModel.getMyGames(Constants.MY_GAMES, data)
    }

    /**
     * get all games api
     */
    private fun getAllGames() {
        currentPage = 1
        binding.tvPublicNoData.visibility = View.GONE
        val data = HashMap<String, Any>()
        data["limit"] = 20
        data["page"] = 1
        data["getAll"] = true
        viewModel.getAllGames(Constants.MY_GAMES, data)
    }


    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvMyGame.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        gameAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val params = HashMap<String, Any>()
        params["page"] = currentPage
        params["limit"] = 10
        viewModel.getMyGames(Constants.MY_GAMES, params)
    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                // iv notifications
                R.id.ivNotification -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "notification")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )

                }

                R.id.cardView -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "createGame")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }

                R.id.cancelImage -> {
                    requireActivity().finish()
                }
                // tvFeed button click
                R.id.tvFeed -> {
                    binding.pos = 1
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_regular)

                }
                // tvSubscriptions  button click
                R.id.tvSubscriptions -> {
                    getAllGames()
                    binding.pos = 2
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.courtsMapCard, R.id.view -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "showMapFragment")
                    intent.putExtra("mapType", "game")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }
    }


    /**
     * init game adapter
     */
    private fun initGameAdapter() {

        val userId = sharedPrefManager.getLoginData()?.data?.user?.id ?: ""

        val listener = object : GameAdapter.OnItemClickListener {
            override fun onItemClick(
                item: MyGame?, clickedViewId: Int, position: Int
            ) {
                val intent = Intent(requireContext(), UserProfileActivity::class.java)
                intent.putExtra("userType", "gameDetails")
                intent.putExtra("gameId", item?.id)
                startActivity(intent)

                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right, R.anim.slide_out_left
                )
            }
        }

        gameAdapter = GameAdapter(userId, listener)
        binding.rvMyGame.adapter = gameAdapter

        publicGameAdapter = GameAdapter(userId, listener)
        binding.rvPublicGames.adapter = publicGameAdapter
    }

    /**
     * add marker
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setCurrentLocation()

    }

    /**
     * set current location
     */
    private fun setCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
            )
            return
        }

        mMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)

                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                )
            }
        }
    }


}