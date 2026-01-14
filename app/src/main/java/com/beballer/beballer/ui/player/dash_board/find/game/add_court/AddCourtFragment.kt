package com.beballer.beballer.ui.player.dash_board.find.game.add_court

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.databinding.AccessibilityDialogItemBinding
import com.beballer.beballer.databinding.FragmentAddCourtBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.BindingUtils.lat
import com.beballer.beballer.utils.BindingUtils.long
import com.beballer.beballer.utils.DummyList.getListAccessibility
import com.beballer.beballer.utils.DummyList.getListHoopsCount
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class AddCourtFragment : BaseFragment<FragmentAddCourtBinding>() {
    private val viewModel: AddCourtFragmentVM by viewModels()
    private lateinit var accessibilityDialog: BaseCustomBottomSheet<AccessibilityDialogItemBinding>
    private lateinit var accessibilityAdapter: SimpleRecyclerViewAdapter<GameModeModel, RvGameModeItemBinding>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var saveLat: String? = null
    private var saveLong: String? = null
    private var city: String? = null
    private var country: String? = null
    private var region: String? = null
    private var zipCode: String? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_court
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // click
        initOnCLick()
        // places search
        initPlaces()
    }


    /*** click event handel **/
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                   requireActivity().finish()
                }

                R.id.btnNext -> {
                    val courtName = binding.etCourtName.text.toString().trim()
                    val courtAddress = binding.etCourtAddress.text.toString().trim()
                    val accessibility = binding.etAccessibility.text.toString().trim()
                    val hoopsCount = binding.etHoopsCount.text.toString().trim()
                    if (!validate(courtName, courtAddress, accessibility, hoopsCount)) {
//                        if (saveLat.isNullOrBlank() || saveLong.isNullOrBlank()) {
//                            showErrorToast("Location not found. Please select location again.")
//                            return@observe
//                        }

                        val bundle = Bundle().apply {
                            putString("courtName", courtName)
                            putString("courtAddress", courtAddress)
                            putString("accessibility", accessibility)
                            putString("hoopsCount", hoopsCount)
                            putString("lat", saveLat)
                            putString("long", saveLong)
                            putString("city", city)
                            putString("country", country)
                            putString("region", region)
                            putString("zipCode", zipCode)
                        }

                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.navigateCourtsAboutFragment, bundle
                        )
                    }

                }

                R.id.etHoopsCount -> {
                    // ope bottom sheet
                    accessibilityBottomSheet(2)
                }

                R.id.etAccessibility -> {
                    // ope bottom sheet
                    accessibilityBottomSheet(1)
                }

                R.id.etCourtAddress -> {
                    binding.etCourtName.clearFocus()
                    val fields = listOf(
                        Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME
                    )
                    val intent = Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields
                    ).build(requireActivity())
                    placeLauncher.launch(intent)
                }

                R.id.tvCurrentLocation -> {
                    checkLocationPermissionAndFetch()
                }
            }
        }


        // Add TextWatchers
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsNotEmpty()
            }
        }

        binding.etCourtName.addTextChangedListener(textWatcher)
        binding.etCourtAddress.addTextChangedListener(textWatcher)
        binding.etAccessibility.addTextChangedListener(textWatcher)
        binding.etHoopsCount.addTextChangedListener(textWatcher)
    }

    private val placeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AutocompleteActivity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                handlePlace(place)
            }
        }

    @SuppressLint("DefaultLocale")
    private fun handlePlace(place: Place) {
        val latLng = place.latLng ?: return
        getAddressFromLatLng(latLng.latitude, latLng.longitude)

        saveLat = String.format("%.5f", latLng.latitude)
        saveLong = String.format("%.5f", latLng.longitude)

    }


    /*** Function to check all fields  ***/
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etCourtName.text?.isNotEmpty() == true && binding.etCourtAddress.text?.isNotEmpty() == true && binding.etAccessibility.text?.isNotEmpty() == true && binding.etHoopsCount.text?.isNotEmpty() == true

        binding.buttonCheck = isAllNotEmpty
    }

    /** game mode bottom sheet **/
    private fun accessibilityBottomSheet(type: Int) {
        accessibilityDialog =
            BaseCustomBottomSheet(requireContext(), R.layout.accessibility_dialog_item) {
                when (it?.id) {
                    R.id.tvCancel -> {
                        accessibilityDialog.dismiss()
                    }
                }
            }
        accessibilityDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        accessibilityDialog.behavior.isDraggable = true
        accessibilityDialog.create()
        accessibilityDialog.show()
        if (type == 1) {
            accessibilityDialog.binding.tvTitle.text = getString(R.string.accessbility)
        } else {
            accessibilityDialog.binding.tvTitle.text = getString(R.string.hoops_count)
        }
        initAccessibilityAdapter(type)
    }


    /** handle game mode adapter **/
    private fun initAccessibilityAdapter(type: Int) {
        accessibilityAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clGame -> {
                        accessibilityDialog.dismiss()
                        if (type == 1) {
                            binding.etAccessibility.setText(m.title)
                            binding.etCourtName.clearFocus()
                        } else {
                            binding.etHoopsCount.setText(m.title)
                            binding.etCourtName.clearFocus()
                        }

                    }
                }
            }
        if (type == 1) {
            accessibilityAdapter.list = getListAccessibility()
        } else {
            accessibilityAdapter.list = getListHoopsCount()
        }

        accessibilityDialog.binding.rvGameModel.adapter = accessibilityAdapter
    }


    /**
     *  location permission
     */
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                getCurrentLocation()
            } else {
                showInfoToast("Location permission required")
            }
        }

    /**
     *  check location permission and fetch location
     */
    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     *  get current location
     */
    @SuppressLint("MissingPermission", "DefaultLocale")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                saveLat = String.format("%.5f", it.latitude)
                saveLong = String.format("%.5f", it.longitude)

                getAddressFromLatLng(it.latitude, it.longitude)
            } ?: run {
                showInfoToast("Unable to get location")
            }
        }
    }

    /**
     *  get address from lat lng
     */
    private fun getAddressFromLatLng(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)

        if (!addresses.isNullOrEmpty()) {
            val a = addresses[0]
            // Save values
            city = a.locality ?: a.subAdminArea
            country = a.countryName
            region = a.adminArea
            zipCode = a.postalCode

            val parts = listOfNotNull(
                a.subThoroughfare,
                a.thoroughfare,
                a.subLocality,
                a.locality,
                a.adminArea,
                a.postalCode
            )
            val cleanAddress = parts.joinToString(", ")
            binding.etCourtAddress.setText(cleanAddress)
            binding.etAccessibility.clearFocus()

        }
    }

    /**
     * Method to initialize places search
     */
    private fun initPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(
                requireContext(), getString(R.string.maps_api_key)
            )
        }
    }


    /*** add validation ***/
    private fun validate(
        courtName: String, courtAddress: String, accessibility: String, hoopsCount: String
    ): Boolean {
        if (courtName.isEmpty()) {
            showInfoToast("Please enter court name")
            return false
        } else if (courtAddress.isEmpty()) {
            showInfoToast("Please enter current address")
            return false
        } else if (accessibility.isEmpty()) {
            showInfoToast("Please pick accessibility")
            return false
        } else if (hoopsCount.isEmpty()) {
            showInfoToast("Please pick hoops count")
            return false
        }
        return true
    }

}