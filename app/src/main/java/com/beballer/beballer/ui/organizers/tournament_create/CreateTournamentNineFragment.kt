package com.beballer.beballer.ui.organizers.tournament_create

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.model.TournamentCategory
import com.beballer.beballer.databinding.FragmentCreateTournamentNineBinding
import com.beballer.beballer.databinding.ItemLayoutAddTournamentBinding
import com.beballer.beballer.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class CreateTournamentNineFragment : BaseFragment<FragmentCreateTournamentNineBinding>() {

    private val viewModel: CommonTournamentVM by viewModels()

    private var tournamentData : TournamentCategory ? = null
    private lateinit var tournamentAdapter  : SimpleRecyclerViewAdapter<TournamentCategory, ItemLayoutAddTournamentBinding>

    private var tournamentList = arrayListOf<TournamentCategory>()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_tournament_nine
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        getTournamentList()
        initAdapter()

    }

    private fun getTournamentList() {
        tournamentList.clear()
        tournamentList.add(TournamentCategory("Tournament 1", "1",true))
        tournamentList.add(TournamentCategory("Tournament 2", "2"))

    }

    private fun initAdapter() {
        tournamentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_add_tournament , BR.bean){v,m,pos ->
            when(v.id){
                R.id.clCreate ->{
                    tournamentList.forEachIndexed { index, tournament ->
                        tournament.isSelected = index == pos

                    }
                    tournamentData  = m
                    tournamentAdapter.notifyDataSetChanged()
                }
            }
        }
        binding.rvTournament.adapter = tournamentAdapter
        tournamentAdapter.list = tournamentList

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id)
            {
                R.id.ivBack ->{

                }
                R.id.btnNext ->{
                    val bundle = Bundle().apply {
                        putParcelable("data", tournamentData)
                    }
                    BindingUtils.navigateWithSlide(
                        findNavController(), R.id.addTournamentDetail, bundle
                    )
                }

                R.id.tvAddTournament ->{
                    BindingUtils.navigateWithSlide(
                        findNavController(), R.id.addTournamentDetail, null
                    )
                }
            }
        })
    }




}