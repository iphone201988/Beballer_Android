package com.beballer.beballer.ui.organizers.tournament_create

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.Court
import com.beballer.beballer.data.model.CreateCategoryApiResponse
import com.beballer.beballer.data.model.CreateTournamentApiResponse
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.databinding.FragmentCreateTournamentSixBinding
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateTournamentSixFragment : BaseFragment<FragmentCreateTournamentSixBinding>() {
    private val viewModel: CommonTournamentVM by activityViewModels ()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_six
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // onclick
        initOnCLick()

        initObserver()
    }

    /*** click handel ***/
    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    findNavController().popBackStack()
                }


                R.id.btnNext -> {
                    if (validate()) {
                        val pools = binding.etNumberOfGroup.text.toString()
                        val court  = binding.etNumberOfCourts.text.toString()
                        val teams = binding.etNumberOfItem.text.toString()
                        viewModel.tournamentData.poolsCount = pools.toInt()
                        viewModel.tournamentData.courtsCount =  court.toInt()
                        viewModel.tournamentData.teamsCount = teams.toInt()


                        viewModel.createAdvancedTournament()

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

        binding.etNumberOfItem.addTextChangedListener(textWatcher)
        binding.etNumberOfGroup.addTextChangedListener(textWatcher)
        binding.etNumberOfCourts.addTextChangedListener(textWatcher)

    }


    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "ADVANCED_TOURNAMENT" -> {
                            try {
                                val myDataModel : CreateCategoryApiResponse ?= BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null){
                                    if (myDataModel.data != null){
                                        val bundle = Bundle().apply {
                                            putString("tournamentCount", binding.etNumberOfCourts.text.toString().trim())

                                            putParcelableArrayList(
                                                "courtData",
                                                ArrayList(myDataModel.data.category.courts) // ✅ correct
                                            )

                                            putString("campsType", "tournaments")
                                        }
                                        BindingUtils.navigateWithSlide(
                                            findNavController(), R.id.createCampsSevenFragment, bundle
                                        )
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("error", "commonLoginAPi: $e")
                            }finally {
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

    // Function to check all fields
    private fun checkAllFieldsNotEmpty() {
        val isAllNotEmpty = binding.etNumberOfItem.text?.isNotEmpty() == true && binding.etNumberOfGroup.text?.isNotEmpty() == true && binding.etNumberOfCourts.text?.isNotEmpty() == true

        binding.buttonCheck = isAllNotEmpty
    }

    /*** add validation ***/
    private fun validate(): Boolean {
        val numberOfItem = binding.etNumberOfItem.text.toString().trim()
        val numberOfGroup = binding.etNumberOfGroup.text.toString().trim()
        val numberOfCourt = binding.etNumberOfCourts.text.toString().trim()

        if (numberOfItem.isEmpty()) {
            showInfoToast("Please enter number of item")
            return false

        } else if (numberOfGroup.isEmpty()) {
            showInfoToast("Please enter number of group")
            return false

        } else if (numberOfCourt.isEmpty()) {
            showInfoToast("Please enter number of court")
            return false
        }

        // ✅ Convert to Int
        val item = numberOfItem.toIntOrNull()
        val group = numberOfGroup.toIntOrNull()

        if (item == null || group == null) {
            showInfoToast("Please enter valid numbers")
            return false
        }

        // ✅ Your condition: items > groups
        if (item <= group) {
            showInfoToast("Number of items must be greater than number of groups")
            return false
        }

        return true
    }
}