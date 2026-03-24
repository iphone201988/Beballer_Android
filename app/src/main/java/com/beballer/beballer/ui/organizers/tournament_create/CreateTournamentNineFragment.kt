package com.beballer.beballer.ui.organizers.tournament_create

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
import com.beballer.beballer.databinding.FragmentCreateTournamentNineBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class CreateTournamentNineFragment : BaseFragment<FragmentCreateTournamentNineBinding>() {

    private val viewModel: CommonTournamentVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_nine
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id)
            {
                R.id.ivBack ->{

                }
            }
        })
    }

}