package com.beballer.beballer.ui.player.auth.onboarding

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.databinding.FragmentOnBoardingBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>() {
    private val viewModel: OnBoardingFragmentVM by viewModels()
    private var userRole = 0
    override fun getLayoutResource(): Int {
        return R.layout.fragment_on_boarding
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // observer
        initObserver()
        // click
        initOnClick()
        userRole = 0
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.clStart -> {
                    binding.ivTop.setImageResource(R.drawable.playeraccountillustration)
                    userRole = 1
                    binding.isCheckStart = true
                    binding.isCheckLeft = false
                    binding.buttonCheck = true
                }

                R.id.clEnd -> {
                    binding.ivTop.setImageResource(R.drawable.organizeraccountillustration)
                    userRole = 2
                    binding.isCheckStart = false
                    binding.isCheckLeft = true
                    binding.buttonCheck = true
                }

                R.id.btnNext -> {
                    if (userRole != 0) {
                        if (userRole == 1) {
                            BindingUtils.navigateWithSlide(
                                findNavController(), R.id.navigateSignupFragment, null
                            )
                        } else {
                            BindingUtils.navigateWithSlide(
                                findNavController(), R.id.navigateOrganisateurLoginFragment, null
                            )
                        }
                    } else {
                        showInfoToast("Please select user role")
                    }

                }
            }
        }
    }

    /** handle api response **/
    private fun initObserver() {


    }
}