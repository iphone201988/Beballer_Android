package com.beballer.beballer.ui.organizers.dash_board.find

import android.content.Intent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.FindModel
import com.beballer.beballer.data.model.GetEventsApiResponse
import com.beballer.beballer.data.model.GetEventsDataEvent
import com.beballer.beballer.databinding.FragmentOrganizersFindBinding
import com.beballer.beballer.databinding.RvOrganizersFindItemBinding
import com.beballer.beballer.ui.organizers.dash_board.tournament.TournamentsActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrganizersFindFragment : BaseFragment<FragmentOrganizersFindBinding>() {
    private val viewModel: OrganizersFindFragmentVM by viewModels()
    private lateinit var findAdapter: SimpleRecyclerViewAdapter<GetEventsDataEvent, RvOrganizersFindItemBinding>
    private lateinit var allAdapter: SimpleRecyclerViewAdapter<GetEventsDataEvent, RvOrganizersFindItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_organizers_find
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // set block pos
        binding.pos = 1
        binding.tvFeed.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        binding.tvSubscriptions.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
        // click
        initOnClick()
        // adapter
        initFindAdapter()


        initObserver()

        getMyEvent()


    }

    private fun getAllEvent() {
        val data = HashMap<String, Any>()
        data["type"] = "all"
        data["page"] = 1
        data["limit"] = 10

        viewModel.getAllEvents(Constants.GET_EVENT, data)
    }

    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING ->  {
                    showLoading()
                }
                Status.SUCCESS ->  {
                    hideLoading()
                    when(it.message){
                        "getEvents" ->{
                            val myDataModel : GetEventsApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    findAdapter.list = myDataModel.data.events
                                }
                            }
                        }

                        "getAllEvents" ->{
                            val myDataModel : GetEventsApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    allAdapter.list = myDataModel.data.events
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->  {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }

    private fun getMyEvent() {
        val data = HashMap<String, Any>()
        data["type"] = "my_events"
        data["page"] = 1
        data["limit"] = 10

        viewModel.getEvents(Constants.GET_EVENT, data)
    }


    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                // iv notifications
                R.id.ivNotification -> {

                }
                // tvFeed button click
                R.id.tvFeed -> {
                    binding.pos = 1
                    getMyEvent()
                    binding.rvAllEvent.visibility = View.GONE
                    binding.rvFind.visibility = View.VISIBLE
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                }
                // tvSubscriptions  button click
                R.id.tvSubscriptions -> {
                    binding.pos = 2
                    getAllEvent()
                    binding.rvAllEvent.visibility = View.VISIBLE
                    binding.rvFind.visibility = View.GONE
                    binding.tvFeed.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
                    binding.tvSubscriptions.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                }
            }
        }
    }


    /** handle adapter **/
    private lateinit var fullList: List<FindModel>
    private fun initFindAdapter() {
        findAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_organizers_find_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clCardView -> {
                        val intent = Intent(requireContext(), TournamentsActivity::class.java)
                        intent.putExtra("organizersPathType", "organizersDetails")
                        intent.putExtra("id", m.id)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                }
            }
        binding.rvFind.adapter = findAdapter



        allAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_organizers_find_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clCardView -> {
                        val intent = Intent(requireContext(), TournamentsActivity::class.java)
                        intent.putExtra("organizersPathType", "organizersDetails")
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                    }
                }
            }
        binding.rvAllEvent.adapter = allAdapter
    }

    // add List in data
    private fun getList(): ArrayList<FindModel> {
        return arrayListOf(
            FindModel(R.drawable.ic_court_24, "Courts", 1),
            FindModel(R.drawable.ic_workout_24, "Workouts", 2),
            FindModel(R.drawable.ic_game_24, "Games", 3),
            FindModel(R.drawable.ic_pro_game_24, "Ticketing", 4),
            FindModel(R.drawable.ic_tournament_24, "Tournaments", 5),
            FindModel(R.drawable.ic_camp_24, "Camps", 6),


            )
    }


    override fun onResume() {
        super.onResume()

    }
}