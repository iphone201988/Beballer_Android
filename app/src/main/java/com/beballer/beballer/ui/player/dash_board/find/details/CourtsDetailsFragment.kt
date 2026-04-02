package com.beballer.beballer.ui.player.dash_board.find.details

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CommonResponse
import com.beballer.beballer.data.model.CourtDataById
import com.beballer.beballer.data.model.GetCourtByIdResponse
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.CourtDeleteDailogItemBinding
import com.beballer.beballer.databinding.CreateGameDialogBinding
import com.beballer.beballer.databinding.FavouritDailogItemBinding
import com.beballer.beballer.databinding.FragmentCourtsDetailsBinding
import com.beballer.beballer.databinding.OpenMapDialogBinding
import com.beballer.beballer.databinding.RatingDialogBinding
import com.beballer.beballer.databinding.ThanksDailogBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.AddCourtActivity
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

@Suppress("DEPRECATION")
@SuppressLint("DefaultLocale", "SetTextI18n", "UseKtx")
@AndroidEntryPoint
class CourtsDetailsFragment : BaseFragment<FragmentCourtsDetailsBinding>(), OnMapReadyCallback {
    private val viewModel: CourtsDetailsFragmentVM by viewModels()
    private val translationYaxis = -100F
    private var isFabMenuVisible = false
    private var courtMarker: Marker? = null
    private val interpolator = OvershootInterpolator()
    private var googleMap: GoogleMap? = null

    private var courtDetail: String? = null
    private lateinit var createGameDialogItem: BaseCustomDialog<CreateGameDialogBinding>
    private lateinit var favCourtDialogItem: BaseCustomDialog<FavouritDailogItemBinding>
    private lateinit var thanksDialogItem: BaseCustomDialog<ThanksDailogBinding>
    private lateinit var deleteCourtDialogItem: BaseCustomDialog<CourtDeleteDailogItemBinding>
    private lateinit var openMapApp: BaseCustomDialog<OpenMapDialogBinding>

    private lateinit var ratingPopup: BaseCustomDialog<RatingDialogBinding>
    private var courtLat: Double? = null
    private var courtLong: Double? = null
    private var formattedRating: String? = null

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
        initData()
        initPopup()

        // observer
        initObserver()
        // map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Method to initialize data
     */
    private fun initData() {
        courtDetail = arguments?.getString("courtId")
        courtDetail.let {
            // api call
            val put = HashMap<String, Any>()
            viewModel.getCourtById(Constants.GET_COURTS_BY_ID + "$it", put)
        }
    }


    /**
     * Method to initialize popup
     */
    private fun initPopup() {
        openMapApp = BaseCustomDialog(requireContext(), R.layout.open_map_dialog) {
            when (it.id) {

                R.id.btnConfirm -> {

                    if (courtLat != null && courtLong != null) {

                        val uri = Uri.parse("geo:$courtLat,$courtLong?q=$courtLat,$courtLong")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps") // Open directly in Google Maps (optional)

                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(), "Location not available", Toast.LENGTH_SHORT
                        ).show()
                    }

                    openMapApp.dismiss()
                }

                R.id.btnCancel -> {
                    openMapApp.dismiss()
                }
            }
        }
    }

    /**
     * Method to rate us
     */
    private fun initRateUsPopup() {
        ratingPopup = BaseCustomDialog(requireContext(), R.layout.rating_dialog) {
            when (it.id) {
                R.id.btnCreateGame -> {
                    if (!formattedRating.isNullOrEmpty()) {
                        val data = HashMap<String, Any>()
                        data["rating"] = formattedRating.toString()
                        data["courtId"] = courtDetail.toString()
                        viewModel.addRatingApi(data, Constants.ADD_RATING)
                        ratingPopup.dismiss()
                    }
                }

                R.id.btnCancel -> {
                    ratingPopup.dismiss()
                }

            }

        }
        ratingPopup.create()
        ratingPopup.show()
        // rating
        setupRating()

    }

    /**
     * Method to set up rating
     */
    private fun setupRating() {
        val rating = binding.bean?.rating?.toFloat() ?: 0.0f

        ratingPopup.binding.courtRatingBar.rating = rating
        ratingPopup.binding.tvYourRating.text = rating.toString()

        ratingPopup.binding.courtRatingBar.setOnRatingBarChangeListener = { newRating, fromUser ->
            if (fromUser) {
                formattedRating = String.format("%.1f", newRating)
                ratingPopup.binding.tvYourRating.text = newRating.toString()
            }
        }
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

                R.id.tvCourtAddress, R.id.tvCourtDistance -> {
                    openMapApp.show()
                }

                R.id.courtRating -> {
                    initRateUsPopup()
                }

                R.id.tvCourtKing -> {
                    createGameDialogItem(2)
                }

                R.id.tvCourtLevel -> {
                    createGameDialogItem(1)
                }

                R.id.addCourtFavoriteFab -> {
                    toggleFabMenu(false)
                    favCourtDialogItem()
                }

                R.id.removeCourtFab -> {
                    toggleFabMenu(false)
                    deleteCourtDialogItem()
                }

                R.id.editCourtInfoFab -> {
                    toggleFabMenu(false)
                    val intent = Intent(requireContext(), AddCourtActivity::class.java)
                    intent.putExtra("courtType","updateCourt")
                    intent.putExtra("courtData", binding.bean)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
                R.id.addFab->{
                    val intent = Intent(requireContext(), AddCourtActivity::class.java)
                    intent.putExtra("courtType","updateImage")
                    intent.putExtra("courtData", binding.bean)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }

    }


    /**** create game dialog item ****/
    private fun createGameDialogItem(type: Int) {
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

        if (type == 1) {
            createGameDialogItem.binding.apply {
                tvCrown.visibility = View.INVISIBLE
                ivCrown.visibility = View.VISIBLE
                tvTitle.text = "Level practiced on this court"
                tvDetail1.text =
                    "Thanks to BEBALLER ranking system, we will soon be able to determine the level practiced on this court."
                tvDetail2.text = "How does it work"
                tvDetail3.text = "On BEBALLER you can create public or\nprivate games."
                tvDetail4.text =
                    "Invite other users and once the game played, enter the score to validate the game. You will earn points for the BEBALLER individual ranking"
                tvDetail5.text = "So win games to prove your worth!"
            }
        } else {
            createGameDialogItem.binding.apply {
                tvCrown.visibility = View.VISIBLE
                ivCrown.visibility = View.GONE
                tvTitle.text = "Become King of the court"
                tvDetail1.text = "The court is your kingdom!\nWin games to wear the crown."
                tvDetail2.text = "How does it work"
                tvDetail3.text = "On BEBALLER you can create public or\nprivate games."
                tvDetail4.text =
                    "Invite other users and once the game played, enter the score to validate the game. You will earn points for the BEBALLER individual ranking"
                tvDetail5.text =
                    "The player who earns the most points thanks to the games played on that court becomes King of the court."
            }
        }

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

    /**
     * thanks dialog item
     */
    private fun thanksDialogItem(type: Int) {
        thanksDialogItem = BaseCustomDialog<ThanksDailogBinding>(
            requireContext(), R.layout.thanks_dailog
        ) {
            when (it?.id) {
                R.id.btnAdd -> {
                    thanksDialogItem.dismiss()
                }

            }

        }
        thanksDialogItem.create()
        thanksDialogItem.show()

        if (type == 1) {
            thanksDialogItem.binding.apply {
                tvApply.text = "Rating Saved"
                tvNotification.text = "Thank you for rating this court."
            }
        } else {
            thanksDialogItem.binding.apply {
                tvApply.text = "Thank you for reporting!"
                tvNotification.text = "We will remove this court after\nverification"
            }
        }

        thanksDialogItem.setOnDismissListener {
            if (type == 1) {
                initData()
            }
        }
    }

    private fun deleteCourtDialogItem() {
        deleteCourtDialogItem = BaseCustomDialog<CourtDeleteDailogItemBinding>(
            requireContext(), R.layout.court_delete_dailog_item
        ) {
            when (it?.id) {
                R.id.deleteCourtDialogItem -> {
                    val data = HashMap<String, Any>()
                    viewModel.courtReportAPi(data, Constants.DELETE_COURT + "/${binding.bean?.id}")
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
            toggleFabMenu(!isFabMenuVisible)
        }
    }

    /**
     * method to toggle fab menu
     */
    private fun toggleFabMenu(show: Boolean) {
        if (show) {
            binding.courtMenusLayout.isVisible = true
            binding.courtMenuFab.animate().rotation(-90F).setInterpolator(interpolator)
                .setDuration(150).start()
            binding.courtMenusLayout.animate().translationY(0F).setListener(null).alpha(1F)
                .setInterpolator(interpolator).setDuration(300).start()
        } else {
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
        isFabMenuVisible = show
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

                                                courtLat = myDataModel.data.court.lat
                                                courtLong = myDataModel.data.court.long


                                                binding.tvCourtDistance.text = String.format(
                                                    "%.1f km", myDataModel.data.court.distance
                                                )

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

                        "addRatingApi" -> {
                            val myDataModel: SimpleApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                hideLoading()
                                thanksDialogItem(1)
                            }
                        }

                        "courtReportAPi" -> {
                            try {
                                val myDataModel: CommonResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel?.success == true) {
                                    showSuccessToast(myDataModel.message.toString())
                                    thanksDialogItem(2)
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
                requireContext(), R.drawable.findcourticon
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
        context: Context, drawableId: Int
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: throw IllegalArgumentException("Drawable not found")

        drawable.setBounds(0, 0, 72, 72)

        val bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}
