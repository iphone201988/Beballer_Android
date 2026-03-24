package com.beballer.beballer.ui.player.dash_board.notification

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.databinding.FragmentNotificationsBinding
import com.beballer.beballer.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : BaseFragment<FragmentNotificationsBinding>() {
    private val viewModel: NotificationsFragmentVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_notifications
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // observer
        initObserver()
        // click
        initOnClick()

        setupSystemUI()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.cancelImage -> {
                    requireActivity().finish()
                }
            }
        }
    }
    private fun setupSystemUI() {
        BindingUtils.applySystemBarMargins(binding.consMain)
        BindingUtils.statusBarStyleWhite(requireActivity())

    }

    /** handle api response **/
    private fun initObserver() {


    }
}

