package com.beballer.beballer.ui.organizers.player_registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.model.SimpleApiResponse
import com.beballer.beballer.databinding.FragmentPlayerRegistrationBinding
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.facebook.appevents.codeless.internal.Constants
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PlayerRegistrationFragment : BaseFragment<FragmentPlayerRegistrationBinding>() {

    private val viewModel : PlayerRegistrationVm by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_player_registration
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView(view: View) {

        initOnCLick()

        initObserver()

    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()
                    val myDataModel  : SimpleApiResponse  ?= BindingUtils.parseJson(it.data.toString())
                    if (myDataModel != null){

                    }
                }
                Status.ERROR -> {
                    hideLoading()
                }
                else -> {

                }
            }
        })
    }

    private fun initOnCLick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.cancelImage ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.btnNext ->{
                    val firstName = binding.etFirstName.text.toString().trim()
                    val lastName = binding.etLastName.text.toString().trim()
                    val playerNumber = binding.etPhoneNo.text.toString().trim()
                    if (validate(firstName,lastName,playerNumber)){
                        val data = HashMap<String , Any>()
                        data["firstName"] = firstName
                        data["lastName"] = lastName
                        data["phoneNumber"] = playerNumber
                        viewModel.registerPlayer(com.beballer.beballer.data.api.Constants.REGISTER_PLAYER,data)
                    }
                }

            }
        })
    }


    /*** add validation ***/
    private fun validate(firstName: String,lastName: String,playerNumber: String): Boolean {
        if (firstName.isEmpty()) {
            showInfoToast("Please enter first name")
            return false
        } else if (lastName.isEmpty()) {
            showInfoToast("Please enter last name")
            return false
        } else if (playerNumber.isEmpty()) {
            showInfoToast("Please enter number of player")
            return false
        }
        return true
    }
}