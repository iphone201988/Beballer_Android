package com.beballer.beballer.ui.player.dash_board.find.courts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.GetCourtApiResponse
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.databinding.FragmentCourtsBinding
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
class CourtsFragment : BaseFragment<FragmentCourtsBinding>(), OnMapReadyCallback {
    private val viewModel: CourtsFragmentVM by viewModels()
    private lateinit var courtsAdapter: CourtAdapter
    private var fullList = ArrayList<GetCourtData>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var searchHandler: Handler? = null
    private var searchRunnable: Runnable? = null
    private var isProgress = false
    private var mMap: GoogleMap? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_courts
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
        initOnClick()
        // adapter
        initAdapter()
        setupSearch()
        // api call
        loadAllCourts()
        // observer
        initObserver()
        // pagination
        pagination()
        /** Refresh **/
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.organize_color))
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                loadAllCourts()
            }, 2000)
        }
    }

    /**
     * handle api response
     */
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getCourt" -> {
                            try {
                                val myDataModel: GetCourtApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.courts?.isNotEmpty() == true) {
                                        val pastSessionData = myDataModel.courts
                                        val feedItems: List<ViewItem> =
                                            pastSessionData.filterNotNull()
                                                .map { ViewItem.Post(it) } ?: emptyList()
                                        isLoading = false
                                        isLastPage = false
                                        isProgress = true

                                        if (currentPage == 1) {
                                            myDataModel.courts.let {
                                                fullList = it as ArrayList<GetCourtData>
                                                courtsAdapter.setList(feedItems)
                                            }
                                        } else {
                                            courtsAdapter.addToList(feedItems)
                                        }
                                        isLastPage =
                                            currentPage == myDataModel.pagination?.totalPages
                                    }

                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                courtsAdapter.hideLoader()
                                hideLoading()
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
        }


    }

    /**
     *  handle click
     */
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

                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.tvAddCourt -> {
                    val intent = Intent(requireContext(), AddCourtActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }

                R.id.courtsMapCard , R.id.view->{
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "showMapFragment")
                    intent.putExtra("mapType", "court")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }


    }


    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvCourts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        courtsAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val params = HashMap<String, Any>()
        params["page"] = currentPage
        params["limit"] = 10
        binding.courtsSearchView.clearFocus()
        viewModel.getCourt(Constants.GET_COURTS, params)
    }

    /**
     * Method to initialize adapter
     */

    private fun initAdapter() {
        courtsAdapter = CourtAdapter(object : CourtAdapter.OnItemClickListener {
            override fun onItemClick(item: GetCourtData?, clickedViewId: Int, position: Int) {
                when (clickedViewId) {
                    R.id.clMain -> {
                        val intent = Intent(requireContext(), UserProfileActivity::class.java)
                        intent.putExtra("userType", "courtDetailsFragment")
                        intent.putExtra("courtId", item?.id.toString())
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }

                }
            }
        })

        binding.rvCourts.adapter = courtsAdapter

    }

    /*** add search ***/
    private fun setupSearch() {
        val searchView = binding.courtsSearchView
        searchHandler = Handler(requireActivity().mainLooper)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (trimmedQuery.isNullOrEmpty() || trimmedQuery.isBlank()) {
                    loadAllCourts()
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
                            loadAllCourts()
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
        viewModel.getCourt(Constants.GET_COURTS, params)
    }

    /**
     * load all courts
     */
    private fun loadAllCourts() {
        val params = HashMap<String, Any>()
        params["page"] = 1
        params["limit"] = 10
        binding.courtsSearchView.clearFocus()
        viewModel.getCourt(Constants.GET_COURTS, params)
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