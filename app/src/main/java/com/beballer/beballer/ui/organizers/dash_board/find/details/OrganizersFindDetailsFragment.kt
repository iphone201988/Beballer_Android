package com.beballer.beballer.ui.organizers.dash_board.find.details

import CategoryAdapter
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.Category
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.data.model.GetEventDetailsApiResponse
import com.beballer.beballer.data.model.GetEventDetailsCategory
import com.beballer.beballer.data.model.GetEventDetailsCourt
import com.beballer.beballer.data.model.GetGameDetailsApiResponse
import com.beballer.beballer.databinding.FragmentOrganizersFindDetailsBinding
import com.beballer.beballer.databinding.IHaveCodeDialogItemBinding
import com.beballer.beballer.databinding.PlayFormateBottomLayoutBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.ui.organizers.dash_board.OrganizersPagerAdapter
import com.beballer.beballer.ui.player.dash_board.find.details.ImagesPagerAdapter
import com.beballer.beballer.utils.BindingUtils.vectorToBitmapDescriptor
import com.beballer.beballer.utils.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrganizersFindDetailsFragment : BaseFragment<FragmentOrganizersFindDetailsBinding>(),
    OnMapReadyCallback {
    private val viewModel: OrganizersFindDetailsVM by viewModels()
    private lateinit var playFormatSheet: BaseCustomBottomSheet<PlayFormateBottomLayoutBinding>
    private lateinit var gameModeAdapter: SimpleRecyclerViewAdapter<GameModeModel, RvGameModeItemBinding>
    private lateinit var iHaveCodeDialogItem: BaseCustomDialog<IHaveCodeDialogItemBinding>

    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryList = mutableListOf<GetEventDetailsCategory>()
    private val translationYaxis = -100F
    private lateinit var imagesPagerAdapter: ImagesPagerAdapter
    private var gameMarker: Marker? = null
    private var isFabMenuVisible = false


    private var googleMap: GoogleMap? = null


    private val interpolator = OvershootInterpolator()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_organizers_find_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // set block pos
        binding.pos = 1
        binding.posSub = 1
        binding.first.visibility = View.INVISIBLE
        binding.second.visibility = View.VISIBLE
        binding.third.visibility = View.VISIBLE
        binding.tvFeed.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        binding.tvSubscriptions.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        binding.tvPost.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        binding.tvClassmates.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        binding.tvStatistiques.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        binding.tvInventaire.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        // fab button click
        initFabMenu()
        // view pager
        // observer
        initObserver()

        // click
        initOnClick()
        // adapter

        setupRecyclerView()
        val adapter = OrganizersPagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        binding.viewPagerProfile.adapter = adapter



    }

    private fun setupRecyclerView() {

        categoryAdapter = CategoryAdapter(
            list = categoryList,

            onAddClick = {

//                val bundle = Bundle().apply {
//                    putParcelable("data", tournamentData)
//                }
                BindingUtils.navigateWithSlide(
                    findNavController(), R.id.addTournamentDetail,null
                )
                Toast.makeText(requireContext(), "Add Category Clicked", Toast.LENGTH_SHORT).show()

            },

            onCategoryClick = { category ->

                BindingUtils.setFormattedGameDate(binding.tvGameStartDate, category.endDate)
                binding.tvGameStartTime.text = BindingUtils.getFormattedTime(category.endDate)
                binding.tvTournamentPrice.text = category.priceRange
                binding.etDescription.setText( category.description)
                binding.tvTournamentAgeRange.text =category.ageRange
                binding.tvTournamentLevel.text = category.level

            }
        )

        binding.rvCategories.adapter = categoryAdapter
    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                // back button click
                R.id.cancelImage -> {
                    requireActivity().onBackPressed()
                }

                // tvFeed button click
                R.id.tvFeed -> {
                    binding.pos = 1
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }
                // tvSubscriptions  button click
                R.id.tvSubscriptions -> {
                    binding.pos = 2
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                }

                R.id.tvPost -> {
                    binding.posSub = 1
                    binding.viewPagerProfile.currentItem = 0
                    binding.first.visibility = View.INVISIBLE
                    binding.second.visibility = View.VISIBLE
                    binding.third.visibility = View.VISIBLE

                    binding.tvPost.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvClassmates.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvStatistiques.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvInventaire.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.tvClassmates -> {
                    binding.posSub = 2
                    binding.viewPagerProfile.currentItem = 1
                    binding.first.visibility = View.INVISIBLE
                    binding.second.visibility = View.INVISIBLE
                    binding.third.visibility = View.VISIBLE

                    binding.tvPost.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvClassmates.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvStatistiques.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvInventaire.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.tvStatistiques -> {
                    binding.posSub = 3
                    binding.viewPagerProfile.currentItem = 2
                    binding.first.visibility = View.VISIBLE
                    binding.second.visibility = View.INVISIBLE
                    binding.third.visibility = View.INVISIBLE

                    binding.tvPost.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvClassmates.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvStatistiques.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvInventaire.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }

                R.id.tvInventaire -> {
                    binding.posSub = 4
                    binding.viewPagerProfile.currentItem = 3
                    binding.first.visibility = View.VISIBLE
                    binding.second.visibility = View.VISIBLE
                    binding.third.visibility = View.INVISIBLE

                    binding.tvPost.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvClassmates.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvStatistiques.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvInventaire.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                }

                R.id.btnNext -> {
                    playFormatBottomSheet()
                }

                R.id.removeCourtFab ->{

                }

            }
        }
    }

    /** play format bottom sheet **/
    private fun playFormatBottomSheet() {
        playFormatSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.play_formate_bottom_layout) {
                when (it?.id) {
                    R.id.tvCancel -> {
                        playFormatSheet.dismiss()
                    }
                }
            }
        playFormatSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        playFormatSheet.behavior.isDraggable = true
        playFormatSheet.binding.tvTitle.text = "Join an event"
        playFormatSheet.create()
        playFormatSheet.show()

        initPlayFormatAdapter()
    }

    /** handle play format adapter **/
    private fun initPlayFormatAdapter() {
        gameModeAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, pos ->
                when (pos) {
                    0 -> {
                        playFormatSheet.dismiss()
                        iHaveCodeDialogItem()
                    }

                    1 -> {
                        playFormatSheet.dismiss()
                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.campsViewFragment, null
                        )
                    }

                    2 -> {
                        playFormatSheet.dismiss()
                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.tournamentSeven, null
                        )
                    }
                }
            }
        gameModeAdapter.list = getListPrice()
        playFormatSheet.binding.rvGameModel.adapter = gameModeAdapter
    }

    // add list game mode
    private fun getListPrice(): ArrayList<GameModeModel> {
        return arrayListOf(
            GameModeModel("I have a code"),
            GameModeModel("I'm a spectator"),
            GameModeModel("I want to register"),

            )
    }


    /**** create profile dialog item ****/
    private fun iHaveCodeDialogItem() {
        iHaveCodeDialogItem = BaseCustomDialog<IHaveCodeDialogItemBinding>(
            requireContext(), R.layout.i_have_code_dialog_item
        ) {
            when (it?.id) {
                R.id.tvOk -> {
                    iHaveCodeDialogItem.dismiss()
                }
            }

        }
        iHaveCodeDialogItem.create()
        iHaveCodeDialogItem.show()
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

        Log.i("fdsfds", "updateMapLocation: $latitude , $longitude")

        if (latitude != null && longitude != null) {
            val latLng = LatLng(latitude , longitude )

            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val markerIcon = vectorToBitmapDescriptor(
                requireContext(), R.drawable.findcourticon, 72, 72
            )

            gameMarker = map.addMarker(
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
                if (marker == gameMarker) {
                    binding.bean?.let { court ->
                        //    openNextFragment(court)
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

    /** handle api response **/
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    hideLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()

                    val myDataModel: GetEventDetailsApiResponse? =
                        BindingUtils.parseJson(it.data.toString())

                    if (myDataModel?.data?.event != null) {

                        val event = myDataModel.data.event
                        binding.bean = event

                        // ViewPager
                        initViewPager(event.eventPhotos ?: emptyList())

                        val apiList = event.categories ?: emptyList()

                        categoryList.clear()
                        categoryList.addAll(apiList.take(6))   // limit to 6

                        categoryAdapter.notifyDataSetChanged()

                        val currentUserId = sharedPrefManager.getLoginData()?.data?.user?.id
                        val currentUserMongoId = sharedPrefManager.getLoginData()?.data?.user?._id

                        val isCurrentUserOrganizer = event.organizersInfo?.any {
                            it.id == currentUserId && it._id == currentUserMongoId
                        } ?: false

                        binding.removeCourtFab.visibility =
                            if (isCurrentUserOrganizer) View.VISIBLE else View.GONE
                    }
                }
                Status.ERROR ->  {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }
                else ->  {

                }
            }
        })

    }


    override fun onResume() {
        super.onResume()

        val id = requireActivity().intent?.getStringExtra("id")
        Log.i("fsdfd", "onResume: $id")

        if (id != null) {
            viewModel.getEventDetails(Constants.EVENT_DETAILS + id)
        }
    }

}