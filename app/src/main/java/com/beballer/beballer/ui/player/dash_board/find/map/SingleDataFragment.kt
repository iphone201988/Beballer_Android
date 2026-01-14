package com.beballer.beballer.ui.player.dash_board.find.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.CourtDataById
import com.beballer.beballer.databinding.FragmentSingleDataBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.AddCourtActivity
import com.beballer.beballer.ui.player.dash_board.find.map.cluster.CourtInfoWindowAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleDataFragment : BaseFragment<FragmentSingleDataBinding>(), OnMapReadyCallback {
    private val viewModel: SingleDataFragmentVM by viewModels()
    private var googleMap: GoogleMap? = null
    private var courtMarker: Marker? = null
    private var selectedMarker: Marker? = null
    private val courtData: CourtDataById? by lazy {
        arguments?.getParcelable("courtData")
    }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_single_data
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.bean = courtData
        // click
        initOnCLick()
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()
        updateMapLocation()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setupMap() {
        googleMap?.apply {
            setInfoWindowAdapter(CourtInfoWindowAdapter(requireContext()))
            setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                true
            }
            setOnMapClickListener {
                courtMarker?.hideInfoWindow()
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun updateMapLocation() {
        val map = googleMap ?: return
        val latitude = courtData?.lat
        val longitude = courtData?.long
        if (latitude != null && longitude != null) {
            val latLng = LatLng(latitude, longitude)
            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            // default icon
            val defaultIcon =
                vectorToBitmapDescriptor(requireContext(), R.drawable.findcourticon, 80, 80)
            val selectedIcon =
                vectorToBitmapDescriptor(requireContext(), R.drawable.pinfilledcourt, 80, 80)
            courtMarker = map.addMarker(
                MarkerOptions().position(latLng).icon(defaultIcon).title(courtData?.name ?: "Court")
                    .snippet(courtData?.address ?: "")
            )
            //  marker click
            map.setOnMarkerClickListener { marker ->
                selectedMarker?.setIcon(defaultIcon)
                marker.setIcon(selectedIcon)
                marker.showInfoWindow()
                selectedMarker = marker
                binding.clMain.visibility = View.VISIBLE
                true
            }
            map.setOnMapClickListener {
                selectedMarker?.setIcon(defaultIcon)
                selectedMarker?.hideInfoWindow()
                binding.clMain.visibility = View.GONE
                selectedMarker = null
            }
            map.isTrafficEnabled = false
            map.isBuildingsEnabled = false
        }
    }

    private fun vectorToBitmapDescriptor(
        context: Context, drawableId: Int, width: Int, height: Int
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: throw IllegalArgumentException("Drawable not found")
        drawable.setBounds(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}