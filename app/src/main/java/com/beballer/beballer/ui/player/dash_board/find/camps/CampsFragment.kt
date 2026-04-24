package com.beballer.beballer.ui.player.dash_board.find.camps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.model.FindModel
import com.beballer.beballer.databinding.CreateTournamentDialogItemBinding
import com.beballer.beballer.databinding.FragmentCampsBinding
import com.beballer.beballer.databinding.RvCampsItemBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomDialog
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
class CampsFragment : BaseFragment<FragmentCampsBinding>(), OnMapReadyCallback {
    private val viewModel: CampsFragmentVM by viewModels()
    private lateinit var campsAdapter: SimpleRecyclerViewAdapter<FindModel, RvCampsItemBinding>
    private lateinit var createCampsDialogItem: BaseCustomDialog<CreateTournamentDialogItemBinding>


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mMap: GoogleMap? = null

    override fun getLayoutResource(): Int {
        return R.layout.fragment_camps
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // observer
        initObserver()
        // click
        initOnClick()
        // adapter
        initCampsAdapter()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }
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
                    createTournamentDialogItem()
                }

                R.id.courtsMapCard, R.id.view -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "showMapFragment")
                    intent.putExtra("mapType", "camps")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }
    }

    /**** create profile dialog item ****/
    private fun createTournamentDialogItem() {
        createCampsDialogItem = BaseCustomDialog<CreateTournamentDialogItemBinding>(
            requireContext(), R.layout.create_tournament_dialog_item
        ) {
            when (it?.id) {
                // let,s go button click
                R.id.btnNext -> {

                }
            }

        }
        createCampsDialogItem.create()
        createCampsDialogItem.show()
    }


    /** handle adapter **/
    private lateinit var fullList: List<FindModel>
    private fun initCampsAdapter() {
        campsAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_tournament_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clCardView -> {
                        val intent = Intent(requireContext(), UserProfileActivity::class.java)
                        intent.putExtra("userType", "campsDetails")
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                }
            }
        fullList = getList()
        campsAdapter.list = fullList
        binding.rvCamps.adapter = campsAdapter
    }

    // add List in data
    private fun getList(): ArrayList<FindModel> {
        return arrayListOf(
            FindModel(R.drawable.ic_court_24, "Courts", 1),
            FindModel(R.drawable.ic_workout_24, "Workouts", 2),
            FindModel(R.drawable.ic_game_24, "Games", 3),
            FindModel(R.drawable.ic_pro_game_24, "Ticketing", 4),
            FindModel(R.drawable.ic_tournament_24, "Tournaments", 5),
            FindModel(R.drawable.ic_camp_24, "Camps", 6),


            )
    }


    /** handle api response **/
    private fun initObserver() {


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
