package com.beballer.beballer.ui.organizers.camps_create

import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.Court
import com.beballer.beballer.data.model.CreateTournamentApiResponse
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.data.model.CreateTournamentModel
import com.beballer.beballer.data.model.UpdateCategoryApiResponse
import com.beballer.beballer.databinding.FragmentCreateCampsSevenBinding
import com.beballer.beballer.ui.organizers.tournament_create.CommonTournamentVM
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateCampsSevenFragment : BaseFragment<FragmentCreateCampsSevenBinding>() {

    private val viewModel: CommonTournamentVM by activityViewModels  ()

    private val editedCourtsList = ArrayList<CreateTournamentModel>()
    private lateinit var adapter: CouNameAdapter

    private var type = 0

    override fun getLayoutResource(): Int = R.layout.fragment_create_camps_seven

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {

        initOnCLick()

        val eventType = arguments?.getString("tournamentCount")
        val campsType = arguments?.getString("campsType")
        val courtList = arguments?.getParcelableArrayList<Court>("courtData")

        // ✅ Type handling
        type = if (campsType?.contains("camps") == true) 1 else 2

        adapter = CouNameAdapter(requireContext(), editedCourtsList)
        binding.rvCampsSeven.adapter = adapter

        // ✅ CASE 1: API DATA AVAILABLE
        if (!courtList.isNullOrEmpty()) {

            editedCourtsList.clear()

            courtList.forEach {
                editedCourtsList.add(
                    CreateTournamentModel(
                        id = it.id ?: "",       // ✅ KEEP ID
                        name = it.name ?: ""    // ✅ KEEP NAME
                    )
                )
            }

            adapter.notifyDataSetChanged()
        }

        // ✅ CASE 2: NO API → CREATE DEFAULT
        else if (!eventType.isNullOrEmpty()) {

            editedCourtsList.clear()

            repeat(eventType.toInt()) { index ->
                editedCourtsList.add(
                    CreateTournamentModel(
                        id = "", // no id yet
                        name = "Court ${index + 1}"
                    )
                )
            }

            adapter.notifyDataSetChanged()
        }

        initObserver()
    }


    private fun initObserver() {
            viewModel.commonObserver.observe(viewLifecycleOwner) {
                when (it?.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        when (it.message) {
                            "UPDATE_COURT" -> {
                                try {
                                    val myDataModel : UpdateCategoryApiResponse ?= BindingUtils.parseJson(it.data.toString())
                                    if (myDataModel != null){
                                        if (myDataModel.data.courts != null){
                                            BindingUtils.navigateWithSlide(
                                                findNavController(),
                                                R.id.tournamentNine,
                                                null
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


    /*** click handle ***/
    private fun initOnCLick() {

        viewModel.onClick.observe(viewLifecycleOwner) {

            when (it?.id) {

                R.id.cancelImage -> {
                    findNavController().popBackStack()
                }

                R.id.btnNext -> {

                    // ✅ Validation
                    if (editedCourtsList.any { it.name.isBlank() }) {
                        showInfoToast("Please fill all court names")
                        return@observe
                    }

                    // ✅ Build API Request
                    val request = buildCourtRequest()

                    // 🔥 DEBUG
                    Log.i("COURT_API", "Request: $request")

                     viewModel.updateCourt(Constants.UPDATE__EVENT_COURT +viewModel.tournamentData.eventId, request)

                    // Navigation
//                    if (type == 2) {
//                        BindingUtils.navigateWithSlide(
//                            findNavController(),
//                            R.id.OrgTournamentDetailsFragment,
//                            null
//                        )
//                    }
                }
            }
        }
    }

    // ✅ FINAL REQUEST BUILDER
    private fun buildCourtRequest(): HashMap<String, Any> {

        val courtsArray = editedCourtsList.map {
            hashMapOf(
                "id" to it.id,
                "name" to it.name
            )
        }

        return hashMapOf("courts" to courtsArray)
    }
}