package com.beballer.beballer.ui.player.dash_board.find.map

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.animation.OvershootInterpolator
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
import com.beballer.beballer.data.model.GetMapBoundData
import com.beballer.beballer.data.model.MapBounds
import com.beballer.beballer.data.model.MapCourt
import com.beballer.beballer.databinding.FragmentShowMapBinding
import com.beballer.beballer.databinding.RvMapBoundItemBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.AddCourtActivity
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.CourtClusterItem
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.CourtClusterRenderer
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
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
import com.google.maps.android.clustering.ClusterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


@AndroidEntryPoint
class ShowMapFragment : BaseFragment<FragmentShowMapBinding>(), OnMapReadyCallback {
    private val viewModel: SingleDataFragmentVM by viewModels()
    private lateinit var mapBoundAdapter: SimpleRecyclerViewAdapter<MapCourt, RvMapBoundItemBinding>
    private val translationYaxis = -100F
    private val markerCourtMap = mutableMapOf<String, MapCourt>()
    private var isFabMenuVisible = false
    private var isRecyclerScrollFromMarker = false
    private var lastCameraPosition: CameraPosition? = null

    private val fetchedBounds = mutableSetOf<String>()
    private var selectedCourtId: String? = null
    private var selectedMarker: com.google.android.gms.maps.model.Marker? = null

    private lateinit var clusterManager: ClusterManager<CourtClusterItem>
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
        // adapter
        initAdapter()
        // observer
        initObserver()
        // fab button click
        initFabMenu()

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
            }
        }
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
                                    addCourtMarkers(model.courts)
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
    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter() {
        binding.rvMapBound.visibility = View.GONE
        mapBoundAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_map_bound_item, BR.bean) { v, m, pos ->
                when (v?.id) {
                    R.id.clMain -> {
                        val intent = Intent(requireContext(), UserProfileActivity::class.java)
                        intent.putExtra("userType", "courtDetailsFragment")
                        intent.putExtra("courtId", m?.id.toString())
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                }
            }
        binding.rvMapBound.adapter = mapBoundAdapter

        binding.rvMapBound.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isRecyclerScrollFromMarker) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val centerPosition =
                    (layoutManager.findFirstVisibleItemPosition() + layoutManager.findLastVisibleItemPosition()) / 2
                val court = mapBoundAdapter.list.getOrNull(centerPosition) ?: return
                if (court.id != selectedCourtId) {
                    selectMarker(court.id!!)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isRecyclerScrollFromMarker = false
                }
            }
        })


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
                binding.rvMapBound.visibility = View.GONE
                selectedCourtId = null
                selectedMarker?.showInfoWindow()
            }
        }

        mMap?.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
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
            mapJob?.cancel()
            mapJob = lifecycleScope.launch {
                delay(600)

                val bounds = mMap?.projection?.visibleRegion?.latLngBounds ?: return@launch
                val expandedBounds = bounds.toMapBounds().expandBy(0.3)

                val cacheKey = expandedBounds.toCacheKey()
                if (fetchedBounds.contains(cacheKey)) return@launch
                fetchedBounds.add(cacheKey)

                viewModel.getMapBound(
                    northEastLat = expandedBounds.northEastLat,
                    northEastLng = expandedBounds.northEastLng,
                    southWestLat = expandedBounds.southWestLat,
                    southWestLng = expandedBounds.southWestLng
                )
            }
            updateRecyclerForVisibleMarkers()
        }

        setupClusterClicks()


        mMap?.setOnMapClickListener {
            binding.rvMapBound.visibility = View.GONE
            clusterRenderer.unselectMarker()
            selectedCourtId = null
            isRecyclerScrollFromMarker = false
        }

    }

    private fun setupClusterClicks() {
        clusterManager.setOnClusterItemClickListener { item ->
            isRecyclerScrollFromMarker = true
            selectedCourtId = item.court.id
            clusterRenderer.selectMarker(item.court.id)
            binding.rvMapBound.visibility = View.VISIBLE
            val index = mapBoundAdapter.list.indexOfFirst {
                it.id == item.court.id
            }
            if (index >= 0) {
                binding.rvMapBound.smoothScrollToPosition(index)
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

    fun LatLngBounds.toMapBounds(): MapBounds {
        return MapBounds(
            northEastLat = northeast.latitude.coerceIn(-90.0, 90.0),
            northEastLng = northeast.longitude.coerceIn(-180.0, 180.0),
            southWestLat = southwest.latitude.coerceIn(-90.0, 90.0),
            southWestLng = southwest.longitude.coerceIn(-180.0, 180.0)
        )
    }

    private fun addCourtMarkers(courts: List<MapCourt?>?) {
        clusterManager.clearItems()
        markerCourtMap.clear()
        courts?.forEach { court ->
            court ?: return@forEach
            markerCourtMap[court.id!!] = court
            clusterManager.addItem(CourtClusterItem(court))
        }
        clusterManager.cluster()
    }

    private fun selectMarker(courtId: String) {
        selectedCourtId = courtId
        clusterRenderer.selectMarker(courtId)
    }

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
        if (zoomChange > 0.15) {
            return true
        }
        val latDelta = newCamera.target.latitude - old.target.latitude
        val lngDelta = newCamera.target.longitude - old.target.longitude
        val movementDistance = sqrt(latDelta * latDelta + lngDelta * lngDelta)
        val movementThreshold = newZoomDelta * 0.25
        if (movementDistance > movementThreshold) {
            return true
        }
        return false
    }

    private fun MapBounds.toCacheKey(): String {
        return "${northEastLat.round(2)}_${northEastLng.round(2)}_" + "${southWestLat.round(2)}_${
            southWestLng.round(
                2
            )
        }"
    }

    private fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).toInt() / factor
    }

    private fun updateRecyclerForVisibleMarkers() {
        val bounds = mMap?.projection?.visibleRegion?.latLngBounds ?: return
        val visibleCourts = markerCourtMap.values.filter { court ->
            bounds.contains(LatLng(court.lat!!, court.long!!))
        }
        if (visibleCourts.isEmpty()) {
            binding.rvMapBound.visibility = View.GONE
        } else {
            mapBoundAdapter.list = visibleCourts
        }
    }

    private fun normalizeLng(lng: Double): Double {
        var value = lng
        while (value > 180) value -= 360
        while (value < -180) value += 360
        return value
    }

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