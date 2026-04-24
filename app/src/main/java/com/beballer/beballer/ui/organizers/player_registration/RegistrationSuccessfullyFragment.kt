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
import com.beballer.beballer.databinding.FragmentRegistrationSuccessfullyBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegistrationSuccessfullyFragment : BaseFragment<FragmentRegistrationSuccessfullyBinding>() {

    private val viewModel: PlayerRegistrationVm by viewModels()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_registration_successfully
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.cancelImage ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.btnNext ->{

                }
            }
        })
    }

}