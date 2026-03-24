package com.beballer.beballer.ui.organizers.tournament_create

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.PlaceDetails
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.databinding.FragmentCreateTournamentEightBinding
import com.beballer.beballer.ui.player.dash_board.find.tournament.PlaceAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.forEach

@AndroidEntryPoint
class CreateTournamentEightFragment : BaseFragment<FragmentCreateTournamentEightBinding>() {
    private val viewModel: CommonTournamentVM by activityViewModels()
    private val places = ArrayList<PlaceDetails>()
    private lateinit var apiKey: String
    private var location: LatLng? = null
    private var address : String ? = null

    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteSessionToken: AutocompleteSessionToken
    private lateinit var placeAdapter: PlaceAdapter

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_eight
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(view: View) {
        // click
        initOnCLick()
        locationAdapter()
        searchLocation()


        apiKey = getString(R.string.maps_api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        placesClient = Places.createClient(requireContext())
        autocompleteSessionToken = AutocompleteSessionToken.newInstance()
    }


    /** handle click **/
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.btnNext -> {
                    if (validate()) {

                        viewModel.tournamentData.name = binding.etTournamentName.text.toString()
                        viewModel.tournamentData.city = places[0].city
                        viewModel.tournamentData.region =  places[0].region
                        viewModel.tournamentData.country =  places[0].country
                        viewModel.tournamentData.address = places[0].address
                        viewModel.tournamentData.lat = location?.latitude
                        viewModel.tournamentData.long = location?.longitude

                        val bundle = Bundle().apply {
                            putString("type","Multiple tournaments")
                        }
                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.tournamentThird, bundle
                        )
                    }
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

        binding.etTournamentName.addTextChangedListener(textWatcher)
        binding.etTournamentAddress.addTextChangedListener(textWatcher)

    }


    // Function to check all fields
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etTournamentName.text?.isNotEmpty() == true && binding.etTournamentAddress.text?.isNotEmpty() == true

        binding.buttonCheck = isAllNotEmpty
    }


    /*** add validation ***/
    private fun validate(): Boolean {
        val name = binding.etTournamentName.text.toString().trim()
        val address = binding.etTournamentAddress.text.toString().trim()
        if (name.isEmpty()) {
            showInfoToast("Please enter tournament name")
            return false
        } else if (address.isEmpty()) {
            showInfoToast("Please enter address name")
            return false
        }
        return true
    }




    private fun locationAdapter() {
        placeAdapter = PlaceAdapter(emptyList()) { places ->

            Log.i("dadadda", "initAdapter: $places ")

            setPlaceToEditText(places)

        }
        binding.rvEnterLocation.adapter = placeAdapter
        placeAdapter.notifyDataSetChanged()
    }

    private fun searchLocation() {

        binding.etTournamentAddress.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

                if (places.isNotEmpty()) {
                    binding.locationCard.visibility = View.VISIBLE
                } else {
                    binding.locationCard.visibility = View.GONE
                }

            } else {
                binding.locationCard.visibility = View.GONE
            }

        }

        binding.etTournamentAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    getPlacePredictions(s.toString())
                    binding.locationCard.visibility = View.VISIBLE
                } else {
                    placeAdapter.updatePlaces(emptyList())
                    binding.locationCard.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun getPlacePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val predictions = response.autocompletePredictions
            //  binding.cards.visibility = View.VISIBLE // Ensure card is visible when there's text
            fetchPlaceDetails(predictions)

        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            Log.i("dsaas", "getPlacePredictions: $exception")
        }
    }
    // function to show full address details of location search

    private fun fetchPlaceDetails(predictions: List<AutocompletePrediction>) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS_COMPONENTS   // 🔥 ADD THIS
        )

        places.clear()
        predictions.forEach { prediction ->
            val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                val locationData = extractLocationData(place)


                val placeDetails = PlaceDetails(
                    name = locationData.name,
                    address = place.address ?: "",
                    location = place.latLng,
                    city = locationData.city,
                    region = locationData.region,
                    country = locationData.country
                )
                places.add(placeDetails)



                if (places.size == predictions.size) {
                    placeAdapter.updatePlaces(places)
                }

                Log.i("places", "fetchPlaceDetails: ${placeDetails.location} ,${placeDetails.address}")
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }
    }

    data class LocationData(
        val name: String,
        val city: String,
        val region: String,
        val country: String
    )

    private fun extractLocationData(place: Place): LocationData {

        val components = place.addressComponents?.asList()

        val city = components?.firstOrNull {
            it.types.contains("locality")
        }?.name ?: ""

        val region = components?.firstOrNull {
            it.types.contains("administrative_area_level_1")
        }?.name ?: ""

        val country = components?.firstOrNull {
            it.types.contains("country")
        }?.name ?: ""

        return LocationData(
            name = place.name ?: "",
            city = city,
            region = region,
            country = country
        )
    }
    private fun setPlaceToEditText(places: PlaceDetails) {
        val combinedAddress = "${places.name}, ${places.address}".trim(',').trim()
        binding.etTournamentAddress.setText(combinedAddress)
        location = places.location
        address = combinedAddress
        Log.i("dsadasda", "setPlaceToEditText: ${location!!.latitude} , ${location!!.longitude}")

        Log.i("dasd", "setPlaceToEditText: ${places.address} ")

        val latLng = LatLng(places.location.latitude, places.location.longitude)



        binding.locationCard.visibility = View.GONE
    }

}