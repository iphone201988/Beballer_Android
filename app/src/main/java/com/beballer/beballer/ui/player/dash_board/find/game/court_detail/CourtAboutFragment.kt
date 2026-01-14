package com.beballer.beballer.ui.player.dash_board.find.game.court_detail

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.databinding.AccessibilityDialogItemBinding
import com.beballer.beballer.databinding.FragmentCourtAboutBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.DummyList.getListBoardType
import com.beballer.beballer.utils.DummyList.getListFloorType
import com.beballer.beballer.utils.DummyList.getListLineType
import com.beballer.beballer.utils.DummyList.getListNetType
import com.beballer.beballer.utils.DummyList.getListWaterPointType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourtAboutFragment : BaseFragment<FragmentCourtAboutBinding>() {
    private val viewModel: CourtAboutFragmentVM by viewModels()
    private lateinit var accessibilityDialog: BaseCustomBottomSheet<AccessibilityDialogItemBinding>
    private lateinit var accessibilityAdapter: SimpleRecyclerViewAdapter<GameModeModel, RvGameModeItemBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_court_about
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnclick()
    }

    /*** click event handel **/
    private fun initOnclick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage ->   requireActivity().finish()
                R.id.btnNext -> {
                    val board = binding.etCourtName.text.toString().trim()
                    val net = binding.etCourtAddress.text.toString().trim()
                    val floor = binding.etAccessibility.text.toString().trim()
                    val lines = binding.etHoopsCount.text.toString().trim()
                    val water = binding.etWaterPoint.text.toString().trim()
                    if (!validate(board, net, floor, lines, water)) {
                        val courtName = arguments?.getString("courtName").orEmpty()
                        val courtAddress = arguments?.getString("courtAddress").orEmpty()
                        val accessibility = arguments?.getString("accessibility").orEmpty()
                        val hoopsCount = arguments?.getString("hoopsCount").orEmpty()
                        val lat = arguments?.getString("lat").orEmpty()
                        val long = arguments?.getString("long").orEmpty()
                        val city = arguments?.getString("city").orEmpty()
                        val country = arguments?.getString("country").orEmpty()
                        val region = arguments?.getString("region").orEmpty()
                        val zipCode = arguments?.getString("zipCode").orEmpty()
                        val bundle = Bundle().apply {
                            putString("courtName", courtName)
                            putString("courtAddress", courtAddress)
                            putString("accessibility", accessibility)
                            putString("hoopsCount", hoopsCount)
                            putString("lat", lat)
                            putString("long", long)
                            putString("city", city)
                            putString("country", country)
                            putString("region", region)
                            putString("zipCode", zipCode)
                            putString("board", board)
                            putString("net", net)
                            putString("floor", floor)
                            putString("lines", lines)
                            putString("water", water)
                        }
                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.navigateAddPhotoFragment, bundle
                        )

                    }
                }

                R.id.etCourtName -> accessibilityBottomSheet(1)
                R.id.etCourtAddress -> accessibilityBottomSheet(2)
                R.id.etAccessibility -> accessibilityBottomSheet(3)
                R.id.etHoopsCount -> accessibilityBottomSheet(4)
                R.id.etWaterPoint -> accessibilityBottomSheet(5)
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
        binding.etWaterPoint.addTextChangedListener(textWatcher)
    }


    /*** Function to check all fields  ***/
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etCourtName.text?.isNotEmpty() == true && binding.etCourtAddress.text?.isNotEmpty() == true && binding.etAccessibility.text?.isNotEmpty() == true && binding.etHoopsCount.text?.isNotEmpty() == true && binding.etWaterPoint.text?.isNotEmpty() == true

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
        when (type) {
            1 -> accessibilityDialog.binding.tvTitle.text = getString(R.string.board_type)
            2 -> accessibilityDialog.binding.tvTitle.text = getString(R.string.net_type)
            3 -> accessibilityDialog.binding.tvTitle.text = getString(R.string.floor_type)
            4 -> accessibilityDialog.binding.tvTitle.text = getString(R.string.lines_and_dimensions)
            5 -> accessibilityDialog.binding.tvTitle.text = getString(R.string.water_point)
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
                        when (type) {
                            1 -> binding.etCourtName.setText(m.title)
                            2 -> binding.etCourtAddress.setText(m.title)
                            3 -> binding.etAccessibility.setText(m.title)
                            4 -> binding.etHoopsCount.setText(m.title)
                            5 -> binding.etWaterPoint.setText(m.title)
                        }

                    }
                }
            }
        when (type) {
            1 -> accessibilityAdapter.list = getListBoardType()
            2 -> accessibilityAdapter.list = getListNetType()
            3 -> accessibilityAdapter.list = getListFloorType()
            4 -> accessibilityAdapter.list = getListLineType()
            5 -> accessibilityAdapter.list = getListWaterPointType()
        }
        accessibilityDialog.binding.rvGameModel.adapter = accessibilityAdapter
    }


    /*** add validation ***/
    private fun validate(
        board: String, net: String, floor: String, lines: String, water: String
    ): Boolean {
        if (board.isEmpty()) {
            showInfoToast("Please pick board type")
            return false
        } else if (net.isEmpty()) {
            showInfoToast("Please pick net type")
            return false
        } else if (floor.isEmpty()) {
            showInfoToast("Please pick floor type")
            return false
        } else if (lines.isEmpty()) {
            showInfoToast("Please pick lines and dimensions")
            return false
        } else if (water.isEmpty()) {
            showInfoToast("Please pick water point")
            return false
        }
        return true
    }

}