package com.beballer.beballer.ui.organizers.tournament_create

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.model.GameModes
import com.beballer.beballer.data.model.TournamentCategory
import com.beballer.beballer.databinding.FragmentAddTournamentDetialsBinding
import com.beballer.beballer.databinding.PlayFormateBottomLayoutBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BindingUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


@AndroidEntryPoint
class AddTournamentDetialsFragment : BaseFragment<FragmentAddTournamentDetialsBinding>(){

    private val viewModel  : CommonTournamentVM by activityViewModels()


    private var tournamentData : TournamentCategory ? = null
    private lateinit var playFormatSheet: BaseCustomBottomSheet<PlayFormateBottomLayoutBinding>
    private lateinit var gameModeAdapter: SimpleRecyclerViewAdapter<GameModes, RvGameModeItemBinding>


    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_tournament_detials
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView(view: View) {
        initData()
        initOnClick()
    }

    private fun initData() {
        tournamentData = arguments?.getParcelable("data")
        if (tournamentData != null){
            binding.bean = tournamentData
        }
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.etStartDate ->{
                    openCalender()
                }
                R.id.clSkip ->{
                    playFormatBottomSheet()

                }
                R.id.btnNext ->{
                    if (validate()){
                        viewModel.tournamentData.ageRange = binding.etAgeRange.text.toString().trim()
                        viewModel.tournamentData.priceRange = binding.etPrice.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("type","Several tournaments")
                        }
                        BindingUtils.navigateWithSlide(
                            findNavController(),
                            R.id.tournamentFour,
                            bundle
                        )
                    }

                }
            }
        })


        // Add TextWatchers
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsNotEmpty()
            }
        }


        binding.etPlayFormat.addTextChangedListener(textWatcher)
        binding.etStartDate.addTextChangedListener(textWatcher)
        binding.etPrice.addTextChangedListener(textWatcher)
        binding.etSkip.addTextChangedListener(textWatcher)
        binding.etAgeRange.addTextChangedListener(textWatcher)



    }


    private fun openCalender() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)

                    // Optional: set fixed time OR current time
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 12)
                    set(Calendar.SECOND, 17)
                    set(Calendar.MILLISECOND, 206)
                }

                // ✅ 1. UI Format
                val uiFormat = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale.US)
                val displayDate = uiFormat.format(selectedCalendar.time)
                binding.etStartDate.setText(displayDate)

                // ✅ 2. ISO Format (for API)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")

                val apiDate = isoFormat.format(selectedCalendar.time)

                // ✅ Store in ViewModel
                viewModel.tournamentData.endDate = apiDate

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }



    /** play format bottom sheet **/
    private fun playFormatBottomSheet(  ) {
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


        playFormatSheet.binding.tvTitle.text = "Choose the currency"



        playFormatSheet.create()
        playFormatSheet.show()

        initPlayFormatAdapter()
    }


    /** handle play format adapter **/
    private fun initPlayFormatAdapter() {
        gameModeAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clGame -> {

                        playFormatSheet.dismiss()
                        binding.etSkip.setText(m.title)


                    }
                }
            }

            gameModeAdapter.list = getListPrice()


        playFormatSheet.binding.rvGameModel.adapter = gameModeAdapter
    }

    private fun getListPrice(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("€ EUR"),
            GameModes("$ USD"),
            GameModes("£ GBP"),
            GameModes("Fr. CHF"),
            GameModes("$ AUD"),
            GameModes("$ CAD"),
            GameModes("kr DKK"),
            GameModes("kr SEK"),
            GameModes("¥ JPY"),

            )
    }


    private fun validate(): Boolean {
        val name = binding.etPlayFormat.text.toString().trim()
        val date = binding.etStartDate.text.toString().trim()
        val priceType = binding.etPrice.text.toString().trim()
        if (name.isEmpty()) {
            showInfoToast("Please enter tournament name")
            return false
        } else if (date.isEmpty()) {
            showInfoToast("Please select date")
            return false
        } else if (priceType.isEmpty()) {
            showInfoToast("Please enter price")
            return false
        }
        return true
    }


    // Function to check all fields
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etPlayFormat.text?.isNotEmpty() == true && binding.etStartDate.text?.isNotEmpty() == true && binding.etPrice.text?.isNotEmpty() == true && binding.etSkip.text?.isNotEmpty() == true && binding.etAgeRange.text?.isNotEmpty() == true
         binding.buttonCheck = isAllNotEmpty
    }

}