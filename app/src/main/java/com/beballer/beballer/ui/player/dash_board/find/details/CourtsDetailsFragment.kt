package com.beballer.beballer.ui.player.dash_board.find.details

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CourtDataById
import com.beballer.beballer.data.model.GetCourtByIdResponse
import com.beballer.beballer.databinding.CourtDeleteDailogItemBinding
import com.beballer.beballer.databinding.CreateGameDialogBinding
import com.beballer.beballer.databinding.FavouritDailogItemBinding
import com.beballer.beballer.databinding.FragmentCourtsDetailsBinding
import com.beballer.beballer.ui.player.dash_board.find.player_profile.PlayerProfileActivity
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CourtsDetailsFragment : BaseFragment<FragmentCourtsDetailsBinding>(), OnMapReadyCallback {
    private val viewModel: CourtsDetailsFragmentVM by viewModels()
    private val translationYaxis = -100F
    private var isFabMenuVisible = false
    private var courtMarker: Marker? = null
    private val interpolator = OvershootInterpolator()
    private var googleMap: GoogleMap? = null
    private lateinit var createGameDialogItem: BaseCustomDialog<CreateGameDialogBinding>
    private lateinit var favCourtDialogItem: BaseCustomDialog<FavouritDailogItemBinding>
    private lateinit var deleteCourtDialogItem: BaseCustomDialog<CourtDeleteDailogItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_courts_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // fab button click
        initFabMenu()
        val courtDetails = arguments?.getString("courtId")
        courtDetails.let {
            // api call
            val put = HashMap<String, Any>()
            viewModel.getCourtById(Constants.GET_COURTS_BY_ID + "$it", put)
        }
        // observer
        initObserver()
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }

                R.id.tvCourtPicture -> {
                    val intent = Intent(requireContext(), PlayerProfileActivity::class.java)
                    intent.putExtra("playerProfile", binding.bean?.userInformation?._id)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        com.airbnb.lottie.R.anim.abc_slide_in_bottom,
                        com.airbnb.lottie.R.anim.abc_fade_out
                    )
                }

                R.id.tvCourtKing -> {
                    createGameDialogItem()
                }

                R.id.addCourtFavoriteFab -> {
                    favCourtDialogItem()
                }

                R.id.removeCourtFab -> {
                    deleteCourtDialogItem()
                }
            }
        }
    }


    /**** create game dialog item ****/
    private fun createGameDialogItem() {
        createGameDialogItem = BaseCustomDialog<CreateGameDialogBinding>(
            requireContext(), R.layout.create_game_dialog
        ) {
            when (it?.id) {
                R.id.btnCreateGame -> {
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "createGame")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }

        }
        createGameDialogItem.create()
        createGameDialogItem.show()
    }

    private fun favCourtDialogItem() {
        favCourtDialogItem = BaseCustomDialog<FavouritDailogItemBinding>(
            requireContext(), R.layout.favourit_dailog_item
        ) {
            when (it?.id) {
                R.id.btnCancel -> {
                    favCourtDialogItem.dismiss()
                }

                R.id.btnAdd -> {
                    favCourtDialogItem.dismiss()
                }
            }

        }
        favCourtDialogItem.create()
        favCourtDialogItem.show()
    }

    private fun deleteCourtDialogItem() {
        deleteCourtDialogItem = BaseCustomDialog<CourtDeleteDailogItemBinding>(
            requireContext(), R.layout.court_delete_dailog_item
        ) {
            when (it?.id) {
                R.id.deleteCourtDialogItem -> {
                    deleteCourtDialogItem.dismiss()
                }

                R.id.btnCancel -> {
                    deleteCourtDialogItem.dismiss()
                }
            }

        }
        deleteCourtDialogItem.create()
        deleteCourtDialogItem.show()
    }


    /*** view pager handel **/
    private fun initViewPager(imageList: List<String> = emptyList()) {
        val uriList = ArrayList<String>()
        for (i in imageList) {
            uriList.add(i)
        }


        val imagesPagerAdapter = ImagesPagerAdapter(requireContext(), uriList)
        binding.viewPager.adapter = imagesPagerAdapter
        binding.dotsIndicator.apply {
            setSliderColor(
                ContextCompat.getColor(requireContext(), R.color.beballer_grey),
                ContextCompat.getColor(requireContext(), R.color.beballer_orange)
            )
            setSliderWidth(
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
            )
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSlideMode(IndicatorSlideMode.NORMAL)
            setIndicatorStyle(IndicatorStyle.CIRCLE)

            binding.dotsIndicator.setupWithViewPager(binding.viewPager)
            setPageSize(imagesPagerAdapter.itemCount)
            notifyDataChanged()

            binding.noCourtPicture.isVisible = imagesPagerAdapter.itemCount == 0
        }
    }


    /*** fab button click handel  **/
    private fun initFabMenu() {
        binding.courtMenusLayout.alpha = 0F
        binding.courtMenusLayout.translationY = translationYaxis
        binding.courtMenusLayout.isVisible = false
        binding.courtMenuFab.setOnClickListener {
            when (isFabMenuVisible) {
                true -> {
                    binding.courtMenuFab.animate().rotation(0F).setInterpolator(interpolator)
                        .setDuration(150).start()
                    binding.courtMenusLayout.animate().translationY(translationYaxis).alpha(0F)
                        .setInterpolator(interpolator).setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                binding.courtMenusLayout.isVisible = false

                            }
                        }).start()
                }

                false -> {
                    binding.courtMenusLayout.isVisible = true

                    binding.courtMenuFab.animate().rotation(-90F).setInterpolator(interpolator)
                        .setDuration(150).start()

                    binding.courtMenusLayout.animate().translationY(0F).setListener(null).alpha(1F)
                        .setInterpolator(interpolator).setDuration(300).start()
                }
            }
            isFabMenuVisible = !isFabMenuVisible
        }
    }


    /**
     * handle api response
     */
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getCourtById" -> {
                            try {
                                val myDataModel: GetCourtByIdResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        if (myDataModel.data.court != null) {
                                            binding.bean = myDataModel.data.court
                                            updateMapLocation()
                                            if (myDataModel.data.court.lat != null && myDataModel.data.court.long != null) {
                                                val lat1 = myDataModel.data.court.lat
                                                val lon1 = myDataModel.data.court.long
                                                val lat2 = BindingUtils.lat
                                                val lon2 = BindingUtils.long
                                                val distance = BindingUtils.formattedDistance(
                                                    lat1, lon1, lat2, lon2
                                                )
                                                binding.tvCourtDistance.text = distance
                                            }

                                            // view pager
                                            initViewPager(myDataModel.data.court.photos as List<String>)

                                        }
                                    } else {
                                        showErrorToast(myDataModel.message.toString())
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
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
     * Method to initialize map
     */

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMapLocation()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun updateMapLocation() {
        val map = googleMap ?: return
        val latitude = binding.bean?.lat
        val longitude = binding.bean?.long

        if (latitude != null && longitude != null) {
            val latLng = LatLng(latitude, longitude)

            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val markerIcon = vectorToBitmapDescriptor(
                requireContext(), R.drawable.findcourticon, 72, 72
            )

            courtMarker = map.addMarker(
                MarkerOptions().position(latLng).icon(markerIcon)
            )



            map.uiSettings.apply {
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isTiltGesturesEnabled = false
                isRotateGesturesEnabled = false
                isMapToolbarEnabled = false
                isZoomControlsEnabled = false
                isCompassEnabled = false
            }
            map.setOnMarkerClickListener { marker ->
                if (marker == courtMarker) {
                    binding.bean?.let { court ->
                        openNextFragment(court)
                    }
                    true
                } else {
                    false
                }
            }
            map.isTrafficEnabled = false
            map.isBuildingsEnabled = false
        }
    }

    private fun openNextFragment(court: CourtDataById) {
        val bundle = Bundle().apply {
            putParcelable("courtData", court)
        }
        val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
            putExtra("userType", "singleDataFragment")
            putExtras(bundle)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )


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