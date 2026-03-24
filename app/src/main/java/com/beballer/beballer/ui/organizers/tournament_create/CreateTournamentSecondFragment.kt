package com.beballer.beballer.ui.organizers.tournament_create

import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.data.model.GameModeModel
import com.beballer.beballer.data.model.GameModes
import com.beballer.beballer.databinding.FragmentCreateTournamentSecondBinding
import com.beballer.beballer.databinding.PlayFormateBottomLayoutBinding
import com.beballer.beballer.databinding.RvGameModeItemBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class CreateTournamentSecondFragment : BaseFragment<FragmentCreateTournamentSecondBinding>() {
    private val viewModel: CommonTournamentVM by activityViewModels()
    private lateinit var playFormatSheet: BaseCustomBottomSheet<PlayFormateBottomLayoutBinding>
    private lateinit var gameModeAdapter: SimpleRecyclerViewAdapter<GameModes, RvGameModeItemBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_second
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnCLick()
    }
    /*** click handel ***/
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    findNavController().popBackStack()
                }

                R.id.etPlayFormat -> {
                    playFormatBottomSheet(1)
                }

                R.id.etStartDate -> {
                    openCalender()
                }

                R.id.clSkip -> {
                    playFormatBottomSheet(2)
                }

                R.id.btnNext -> {
                    if (validate()) {
                        viewModel.tournamentData.format = binding.etPlayFormat.text.toString().trim()
                        val price = binding.etPrice.text.toString().trim()
                        val currency = extractCurrencyCode(binding.etSkip.text.toString())

                        val formattedPrice = price.toDoubleOrNull()?.let {
                            "$it $currency"
                        } ?: ""

                        viewModel.tournamentData.priceRange = formattedPrice
                        BindingUtils.navigateWithSlide(
                            findNavController(), R.id.tournamentThird, null
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


        binding.etPlayFormat.addTextChangedListener(textWatcher)
        binding.etStartDate.addTextChangedListener(textWatcher)
        binding.etPrice.addTextChangedListener(textWatcher)
        binding.etSkip.addTextChangedListener(textWatcher)
    }

    private fun extractCurrencyCode(value: String): String {
        return value.split(" ")
            .lastOrNull()
            ?.lowercase() ?: ""
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

    // Function to check all fields
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty =
            binding.etPlayFormat.text?.isNotEmpty() == true && binding.etStartDate.text?.isNotEmpty() == true && binding.etPrice.text?.isNotEmpty() == true && binding.etSkip.text?.isNotEmpty() == true
        binding.buttonCheck = isAllNotEmpty
    }


    /** play format bottom sheet **/
    private fun playFormatBottomSheet(type: Int) {
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
        if (type == 1) {
            playFormatSheet.binding.tvTitle.text = "Choose an option"
        } else {
            playFormatSheet.binding.tvTitle.text = "Choose the currency"
        }


        playFormatSheet.create()
        playFormatSheet.show()

        initPlayFormatAdapter(type)
    }


    /** handle play format adapter **/
    private fun initPlayFormatAdapter(type: Int) {
        gameModeAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_game_mode_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clGame -> {
                        if (type == 1) {
                            playFormatSheet.dismiss()
                            binding.etPlayFormat.setText(m.title)
                        } else {
                            playFormatSheet.dismiss()
                            binding.etSkip.setText(m.title)
                        }

                    }
                }
            }
        if (type == 1) {
            gameModeAdapter.list = getListPlayFormat()
        } else {
            gameModeAdapter.list = getListPrice()
        }

        playFormatSheet.binding.rvGameModel.adapter = gameModeAdapter
    }

    // add list game mode
    private fun getListPlayFormat(): ArrayList<GameModes> {
        return arrayListOf(
            GameModes("1x1"),
            GameModes("2x2"),
            GameModes("3x3"),
            GameModes("4x4"),
            GameModes("5x5"),

            )
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

    /*** add validation ***/
    private fun validate(): Boolean {
        val name = binding.etPlayFormat.text.toString().trim()
        val address = binding.etStartDate.text.toString().trim()
        val range = binding.etPrice.text.toString().trim()
        val priceType = binding.etSkip.text.toString().trim()
        if (name.isEmpty()) {
            showInfoToast("Please pick play format")
            return false
        } else if (address.isEmpty()) {
            showInfoToast("Please enter address name")
            return false
        } else if (range.isEmpty()) {
            showInfoToast("Please enter age range name")
            return false
        } else if (priceType.isEmpty()) {
            showInfoToast("Please pick currency type")
            return false
        }
        binding.buttonCheck = true
        return true
    }

}