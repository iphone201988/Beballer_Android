package com.beballer.beballer.ui.player.dash_board.find.map

import MapListItem
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.GetGameMapBoundData
import com.beballer.beballer.data.model.GetMapBoundData
import com.beballer.beballer.data.model.MapBounds
import com.beballer.beballer.data.model.MapCourt
import com.beballer.beballer.data.model.SearchCourtApiData
import com.beballer.beballer.databinding.FragmentShowMapBinding
import com.beballer.beballer.databinding.RvSearchMapItemBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.AddCourtActivity
import com.beballer.beballer.ui.player.dash_board.find.map.adapter.MapMultiTypeAdapter
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.CourtClusterRenderer
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.MapClusterItem
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.MapType
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.beballer.beballer.utils.hideKeyboard
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


@AndroidEntryPoint
class ShowMapFragment : BaseFragment<FragmentShowMapBinding>(), OnMapReadyCallback {
    private val viewModel: SingleDataFragmentVM by viewModels()
    private lateinit var searchDataAdapter: SimpleRecyclerViewAdapter<MapCourt, RvSearchMapItemBinding>
    private val translationYaxis = -100F
    private lateinit var adapter: MapMultiTypeAdapter
    private val list = mutableListOf<MapListItem>()
    private val markerCourtMap = mutableMapOf<String, MapListItem>()

    private var isFabMenuVisible = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var isRecyclerScrollFromMarker = false
    private var lastCameraPosition: CameraPosition? = null
    private var searchHandler: Handler? = null
    private var isCameraMoveFromSelection = false
    private var searchRunnable: Runnable? = null
    private val fetchedBounds = mutableSetOf<String>()
    private var selectedCourtId: String? = null
    private var apiType = 0
    private lateinit var clusterManager: ClusterManager<MapClusterItem>
    private lateinit var clusterRenderer: CourtClusterRenderer


    private var mMap: GoogleMap? = null
    private var isFirstCameraIdle = true
    private var mapJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val interpolator = OvershootInterpolator()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_show_map
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // click
        initOnCLick()
        initClickBottomSheet()
        // adapter
        initAdapter()
        initSearchAdapter()
        // observer
        initObserver()
        // fab button click
        initFabMenu()
        // search
        setupSearch()
        val data =arguments?.getString("mapType")
        when (data) {
            "court" -> apiType = 1
            "game" -> apiType = 2
            "ticket" -> apiType = 3
            "tournament" -> apiType = 4
            "camps" -> apiType = 5
        }
    }

    /*** bottom sheet handel ***/
    private fun initClickBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.clMain)

        binding.clMain.post {
            val screenHeight = resources.displayMetrics.heightPixels
            val sheetHeight = (screenHeight * 0.8f).toInt()

            binding.clMain.layoutParams.height = sheetHeight
            binding.clMain.requestLayout()

            bottomSheetBehavior.apply {
                peekHeight = sheetHeight
                isHideable = true
                state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clearSearchInput()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
    }

    /**
     * Clear search input
     */
    private fun clearSearchInput() {
        binding.mapSearchView.apply {
            setQuery("", false)
            clearFocus()
        }
        hideKeyboard()
    }


    /*** fab button click handel  **/
    private fun initFabMenu() {
        binding.mapMenusLayout.alpha = 0F
        binding.mapMenusLayout.translationY = translationYaxis
        binding.mapMenusLayout.isVisible = false
        binding.cardViewFilter.setOnClickListener {
            when (isFabMenuVisible) {
                true -> {
                    binding.cardViewFilter.animate().rotation(0F).setInterpolator(interpolator)
                        .setDuration(150).start()
                    binding.mapMenusLayout.animate().translationY(translationYaxis).alpha(0F)
                        .setInterpolator(interpolator).setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                binding.mapMenusLayout.isVisible = false

                            }
                        }).start()
                }

                false -> {
                    binding.mapMenusLayout.isVisible = true

                    binding.cardViewFilter.animate().rotation(-90F).setInterpolator(interpolator)
                        .setDuration(150).start()

                    binding.mapMenusLayout.animate().translationY(0F).setListener(null).alpha(1F)
                        .setInterpolator(interpolator).setDuration(300).start()
                }
            }
            isFabMenuVisible = !isFabMenuVisible
        }
    }

    /**
     * Initialize click
     */
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.AddButton -> {
                    val intent = Intent(requireContext(), AddCourtActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }

                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.cardViewCourtGame, R.id.tvGame -> {
                    if (apiType != 2) {
                        apiType = 2
                        clearMapCache()
                        fetchCourtsForBounds(apiType)
                    }
                }

                R.id.cardViewCourt, R.id.tvCourt -> {
                    if (apiType != 1) {
                        apiType = 1
                        clearMapCache()
                        fetchCourtsForBounds(apiType)
                    }
                }

                R.id.cardViewTicket, R.id.tvTicket -> {
                    if (apiType != 3) {
                        apiType = 3
                        clearMapCache()
                        fetchCourtsForBounds(apiType)
                    }
                }

                R.id.cardViewTournaments, R.id.tvTournaments -> {
                    if (apiType != 4) {
                        apiType = 4
                        clearMapCache()
                        fetchCourtsForBounds(apiType)
                    }
                }

                R.id.cardViewCamps, R.id.tvCamps -> {
                    if (apiType != 5) {
                        apiType = 5
                        clearMapCache()
                        fetchCourtsForBounds(apiType)
                    }
                }

            }
        }
    }


    private fun clearMapCache() {
        fetchedBounds.clear()
        lastCameraPosition = null
        isFirstCameraIdle = true
    }


    /**
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        Constants.COURT_MAP_BOUNDS -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetMapBoundData>(it.data.toString())
                                if (model?.success == true && model.courts != null) {
                                    list.clear()
                                    model.courts.forEach { list.add(MapListItem.Court(it)) }
                                    adapter.submitList(list)
                                    addMarkers(list)

                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        Constants.GAME_MAP_BOUNDS -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetGameMapBoundData>(it.data.toString())
                                if (model?.success == true && model.data != null) {
                                    val games = model.data.games
                                    list.clear()
                                    games?.forEach { list.add(MapListItem.Game(it)) }
                                    adapter.submitList(list)
                                    addMarkers(list)


                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        Constants.TICKET_MAP_BOUNDS -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetMapBoundData>(it.data.toString())
                                if (model?.success == true && model.courts != null) {
                                    list.clear()
                                    model.courts.forEach { list.add(MapListItem.Court(it)) }
                                    adapter.submitList(list)
                                    addMarkers(list)

                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        Constants.TOURNAMENTS_MAP_BOUNDS -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetMapBoundData>(it.data.toString())
                                if (model?.success == true && model.courts != null) {
                                    val tournaments = model.courts
                                    list.clear()
                                    tournaments.forEach { list.add(MapListItem.Court(it)) }
                                    adapter.submitList(list)
                                    addMarkers(list)
                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        Constants.CAMPS_MAP_BOUNDS -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetMapBoundData>(it.data.toString())
                                if (model?.success == true && model.courts != null) {
                                    val camps = model.courts
                                    list.clear()
                                    camps.forEach { list.add(MapListItem.Court(it)) }
                                    adapter.submitList(list)
                                    addMarkers(list)
                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "search" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<SearchCourtApiData>(it.data.toString())
                                if (model?.success == true && model.courts != null) {
                                    //addCourtMarkers(model.courts)
                                    showSearchData(model.courts)
                                } else {
                                    showErrorToast(model?.message.toString())
                                }

                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                Status.LOADING -> hideLoading()
                else -> {

                }
            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        adapter = MapMultiTypeAdapter { item ->
            // marker select
            selectedCourtId = item.id
            clusterRenderer.selectMarker(item.id)
            centerMarker(item.lat, item.lng, 17.5f)
            // move camera
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(item.lat, item.lng), 17.5f
                )
            )
            when (item) {
                is MapListItem.Court -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "courtDetailsFragment")
                    intent.putExtra("courtId", item.data.id.toString())
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }

                is MapListItem.Game -> {}
                is MapListItem.Ticket -> {}
                is MapListItem.Tournament -> {}
                is MapListItem.Camp -> {}
            }
        }

        binding.rvCourtMapBound.apply {
            adapter = this@ShowMapFragment.adapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            visibility = View.GONE
        }


        binding.rvCourtMapBound.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (isRecyclerScrollFromMarker) return

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val centerPosition =
                    (layoutManager.findFirstVisibleItemPosition() + layoutManager.findLastVisibleItemPosition()) / 2

                val item = adapter.getItemAt(centerPosition) ?: return
                if (item.id != selectedCourtId) {
                    selectedCourtId = item.id
                    clusterRenderer.selectMarker(item.id)
                    centerMarker(item.lat, item.lng)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isRecyclerScrollFromMarker = false
                }
            }
        })


    }


    private fun centerMarker(lat: Double, lng: Double, zoom: Float? = null) {
        val map = mMap ?: return
        isCameraMoveFromSelection = true
        val cameraUpdate = if (zoom != null) {
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom)
        } else {
            CameraUpdateFactory.newLatLng(LatLng(lat, lng))
        }

        map.animateCamera(cameraUpdate)
    }


    /*** add search ***/
    private fun setupSearch() {
        val searchView = binding.mapSearchView
        searchHandler = Handler(requireActivity().mainLooper)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (trimmedQuery?.isNotBlank() == true || trimmedQuery?.isNotBlank() == true) {
                    searchMapCourts(trimmedQuery)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { searchHandler?.removeCallbacks(it) }
                if (newText.isNullOrBlank()) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }
                searchRunnable = Runnable {
                    searchMapCourts(newText.trim())
                }
                searchHandler?.postDelayed(searchRunnable!!, 1000)
                return true
            }

        })
    }

    /**
     * show loading
     */
    private fun showSearchLoading() {
        binding.searchBottomSheet.apply {
            tvStatus.text = getString(R.string.court_search)
            tvStatus.isVisible = true
            rvMapSearch.isVisible = false
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    /**
     * search court
     */
    private fun searchMapCourts(query: String) {
        binding.mapSearchView.clearFocus()
        showSearchLoading()
        viewModel.getSearchMap(query)
    }

    /*** Initialize search adapter once ***/
    private fun initSearchAdapter() {
        searchDataAdapter = SimpleRecyclerViewAdapter(R.layout.rv_search_map_item, BR.bean) { v, m, _ ->
            if (v?.id == R.id.clMapSearch) {
                m?.let { moveToCourtFromSearch(it) }
            }
        }
        binding.searchBottomSheet.rvMapSearch.adapter = searchDataAdapter
    }

    /**
     * Show search results in bottom sheet
     */
    private fun showSearchData(courts: List<MapCourt?>?) {
        val hasData = !courts.isNullOrEmpty()
        binding.searchBottomSheet.apply {
            if (hasData) {
                tvStatus.isVisible = false
                rvMapSearch.isVisible = true
                searchDataAdapter.list = courts.filterNotNull()
                searchDataAdapter.notifyDataSetChanged()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                showNoResults()
            }
        }
    }

    private var isSearchSelection = false
    /**
     * Shows marker, info window, and scrolls RecyclerView
     */
    private fun moveToCourtFromSearch(court: MapCourt) {
        val id = court.id
        val lat = court.lat
        val lng = court.long

        if (id == null || lat == null || lng == null) return

        isSearchSelection = true
        mapJob?.cancel()

        requireActivity().runOnUiThread {
            clusterManager.clearItems()
            markerCourtMap.clear()

            val mapItem = MapListItem.Court(court)
            markerCourtMap[id] = mapItem
            val clusterItem = MapClusterItem(mapItem)
            clusterManager.addItem(clusterItem)
            clusterManager.cluster()

            val latLng = LatLng(lat, lng)
            mMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(latLng).zoom(17.5f).build()
                )
            )

            selectedCourtId = id
            clusterRenderer.selectMarker(id)
            clusterRenderer.showMarkerInfoWindow(id)

            binding.rvCourtMapBound.visibility = View.VISIBLE

            val index = list.indexOfFirst { it is MapListItem.Court && it.data.id == id }
            if (index >= 0) {
                // Court exists in main list → scroll to it
                binding.rvCourtMapBound.post {
                    isRecyclerScrollFromMarker = true
                    binding.rvCourtMapBound.smoothScrollToPosition(index)
                }
            } else {
                // Court not in main list → show only selected court
                adapter.submitList(listOf(mapItem))
            }

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }




    /**
     * show no result
     */
    private fun showNoResults() {
        binding.searchBottomSheet.apply {
            tvStatus.text = getString(R.string.no_search_results_found)
            tvStatus.isVisible = true
            rvMapSearch.isVisible = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }



    /**
     * add marker
     */

    @SuppressLint("PotentialBehaviorOverride")
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

        clusterManager = ClusterManager(requireContext(), mMap)
        clusterRenderer = CourtClusterRenderer(requireContext(), mMap!!, clusterManager)
        clusterManager.renderer = clusterRenderer

        mMap?.setOnMarkerClickListener(clusterManager)

        mMap?.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.rvCourtMapBound.visibility = View.GONE
                selectedCourtId = null
                clusterRenderer.clearSelection()
                isSearchSelection = false

            }
        }

        mMap?.setOnCameraIdleListener {
            clusterRenderer.currentZoom = mMap?.cameraPosition?.zoom ?: 0f
            clusterManager.onCameraIdle()

            if (isCameraMoveFromSelection) {
                isCameraMoveFromSelection = false
                return@setOnCameraIdleListener
            }


            if (isSearchSelection) {
                isSearchSelection = false
                return@setOnCameraIdleListener
            }
            val currentCamera = mMap?.cameraPosition ?: return@setOnCameraIdleListener
            if (isFirstCameraIdle) {
                isFirstCameraIdle = false
                lastCameraPosition = currentCamera
                return@setOnCameraIdleListener
            }

            if (!shouldRefresh(lastCameraPosition, currentCamera)) {
                return@setOnCameraIdleListener
            }

            lastCameraPosition = currentCamera
            fetchCourtsForBounds(apiType)
        }

        setupClusterClicks()


        mMap?.setOnMapClickListener {
            binding.rvCourtMapBound.visibility = View.GONE
             clusterRenderer.clearSelection()
            selectedCourtId = null
            isRecyclerScrollFromMarker = false
        }

    }

    private fun fetchCourtsForBounds(type: Int) {
        mapJob?.cancel()
        mapJob = lifecycleScope.launch {
            delay(600)
            val bounds = mMap?.projection?.visibleRegion?.latLngBounds ?: return@launch
            val expanded = bounds.toMapBounds().expandBy(0.3)

            val key = expanded.toCacheKey()
            if (fetchedBounds.contains(key)) return@launch
            fetchedBounds.add(key)
            when (type) {
                1 -> {
                    viewModel.getMapBound(
                        type = MapType.COURT,
                        northEastLat = expanded.northEastLat,
                        northEastLng = expanded.northEastLng,
                        southWestLat = expanded.southWestLat,
                        southWestLng = expanded.southWestLng
                    )
                }

                2 -> {
                    viewModel.getMapBound(
                        type = MapType.GAME,
                        northEastLat = expanded.northEastLat,
                        northEastLng = expanded.northEastLng,
                        southWestLat = expanded.southWestLat,
                        southWestLng = expanded.southWestLng
                    )
                }

                3 -> {
                    viewModel.getMapBound(
                        type = MapType.TICKET,
                        northEastLat = expanded.northEastLat,
                        northEastLng = expanded.northEastLng,
                        southWestLat = expanded.southWestLat,
                        southWestLng = expanded.southWestLng
                    )
                }

                4 -> {
                    viewModel.getMapBound(
                        type = MapType.TOURNAMENT,
                        northEastLat = expanded.northEastLat,
                        northEastLng = expanded.northEastLng,
                        southWestLat = expanded.southWestLat,
                        southWestLng = expanded.southWestLng
                    )
                }

                5 -> {
                    viewModel.getMapBound(
                        type = MapType.CAMP,
                        northEastLat = expanded.northEastLat,
                        northEastLng = expanded.northEastLng,
                        southWestLat = expanded.southWestLat,
                        southWestLng = expanded.southWestLng
                    )
                }
            }
        }
    }



    /**
     * Setup cluster clicks
     */
    private fun setupClusterClicks() {
        clusterManager.setOnClusterItemClickListener { item ->
            isRecyclerScrollFromMarker = true

            selectedCourtId = item.item.id
            clusterRenderer.selectMarker(item.item.id)
            clusterRenderer.showMarkerInfoWindow(item.item.id)

            binding.rvCourtMapBound.visibility = View.VISIBLE

            val index = list.indexOfFirst {
                it is MapListItem.Court && it.data.id == item.item.id
            }
            if (index >= 0) {
                binding.rvCourtMapBound.post {
                    binding.rvCourtMapBound.smoothScrollToPosition(index)
                }
            }

            true
        }

        clusterManager.setOnClusterClickListener { cluster ->
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    cluster.position, mMap!!.cameraPosition.zoom + 2
                )
            )
            true
        }
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
                    CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                )
            }
        }
    }

    /**
     * Convert LatLngBounds to MapBounds
     */
    fun LatLngBounds.toMapBounds(): MapBounds {
        return MapBounds(
            northEastLat = northeast.latitude.coerceIn(-90.0, 90.0),
            northEastLng = northeast.longitude.coerceIn(-180.0, 180.0),
            southWestLat = southwest.latitude.coerceIn(-90.0, 90.0),
            southWestLng = southwest.longitude.coerceIn(-180.0, 180.0)
        )
    }

    /**
     * add marker
     */

    private fun addMarkers(items: List<MapListItem>) {
        if (!isAdded || mMap == null) return

        requireActivity().runOnUiThread {
            clusterManager.clearItems()
            markerCourtMap.clear()

            items.forEach { item ->
                markerCourtMap[item.id] = item
                clusterManager.addItem(MapClusterItem(item))
            }

            clusterManager.cluster()
        }
    }




    /**
     * should refresh
     */
    fun shouldRefresh(
        oldCamera: CameraPosition?, newCamera: CameraPosition
    ): Boolean {
        val old = oldCamera ?: return true
        fun zoomToDelta(zoom: Float): Double {
            return 360.0 / 2.0.pow(zoom.toDouble())
        }

        val oldZoomDelta = zoomToDelta(old.zoom)
        val newZoomDelta = zoomToDelta(newCamera.zoom)

        val zoomChange = abs(oldZoomDelta - newZoomDelta) / maxOf(oldZoomDelta, 0.0001)
        if (zoomChange > 0.10) {
            return true
        }

        val latDelta = newCamera.target.latitude - old.target.latitude
        val lngDelta = newCamera.target.longitude - old.target.longitude
        val movementDistance = sqrt(latDelta * latDelta + lngDelta * lngDelta)

        val minMovementThreshold = 0.001
        val movementThreshold = max(newZoomDelta * 0.2, minMovementThreshold)
      //  val movementThreshold = newZoomDelta * 0.20
        if (movementDistance > movementThreshold) {
            return true
        }
        return false
    }

    /**
     * Convert MapBounds to cache key
     */
    private fun MapBounds.toCacheKey(): String {
        return "${northEastLat.round(2)}_${northEastLng.round(2)}_" + "${southWestLat.round(2)}_${
            southWestLng.round(
                2
            )
        }"
    }

    /**
     * Round double value
     */
    private fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).toInt() / factor
    }



    /**
     * Normalize longitude
     */
    private fun normalizeLng(lng: Double): Double {
        var value = lng
        while (value > 180) value -= 360
        while (value < -180) value += 360
        return value
    }

    /**
     * Expand bounds by a factor
     */
    private fun MapBounds.expandBy(factor: Double = 0.25): MapBounds {
        val latSpan = northEastLat - southWestLat
        val lngSpan = northEastLng - southWestLng
        var neLat = northEastLat + latSpan * factor
        var swLat = southWestLat - latSpan * factor
        var neLng = northEastLng + lngSpan * factor
        var swLng = southWestLng - lngSpan * factor
        neLat = neLat.coerceIn(-90.0, 90.0)
        swLat = swLat.coerceIn(-90.0, 90.0)
        neLng = normalizeLng(neLng)
        swLng = normalizeLng(swLng)
        val finalNorthLat = maxOf(neLat, swLat)
        val finalSouthLat = minOf(neLat, swLat)
        val finalEastLng = maxOf(neLng, swLng)
        val finalWestLng = minOf(neLng, swLng)
        return MapBounds(
            northEastLat = finalNorthLat,
            northEastLng = finalEastLng,
            southWestLat = finalSouthLat,
            southWestLng = finalWestLng
        )
    }




}