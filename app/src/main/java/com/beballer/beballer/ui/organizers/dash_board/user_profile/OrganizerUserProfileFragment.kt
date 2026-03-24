package com.beballer.beballer.ui.organizers.dash_board.user_profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseFragment
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.CommonResponse
import com.beballer.beballer.data.model.PlayerData
import com.beballer.beballer.data.model.PlayerProfileResponse
import com.beballer.beballer.data.model.UserProfile
import com.beballer.beballer.databinding.FragmentOrganizerUserProfileBinding
import com.beballer.beballer.databinding.PlayerPostRvItemBinding
import com.beballer.beballer.ui.player.dash_board.DashboardActivity
import com.beballer.beballer.ui.player.dash_board.profile.followers.FollowersAndFollowingActivity
import com.beballer.beballer.ui.player.dash_board.profile.user.UserProfileActivity
import com.beballer.beballer.ui.player.post_details.PlayerPostDetailsActivity
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Resource
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OrganizerUserProfileFragment : BaseFragment<FragmentOrganizerUserProfileBinding>() {

    private val  viewModel : OrganizerVm by  viewModels()
    private lateinit var playerPostAdapter: SimpleRecyclerViewAdapter<PlayerData, PlayerPostRvItemBinding>

    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    override fun getLayoutResource(): Int {
        return R.layout.fragment_organizer_user_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        initObserver()

        initAvatarAdapter()
        // api call
        val data = HashMap<String, Any>()
        var userId = sharedPrefManager.getLoginData()?.data?.user?.id
        data["page"] = currentPage
        data["limit"] = 20
        data["type"] = "players"
        viewModel.postPublisherId(Constants.POST_PUBLISHER_ID + "$userId", data)


        paginationHandel()

    }



    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.ivSettings ->{
                    val intent = Intent(requireContext(), UserProfileActivity::class.java)
                    intent.putExtra("userType", "settings")
                    intent.putExtra("side","Organizer")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }


                R.id.tvTotalFollowersCount , R.id.tvTotalFollowers->{
                    val intent = Intent(requireContext(), FollowersAndFollowingActivity::class.java)
                    intent.putExtra("FollowersType", "Followers")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
                R.id.tvTotalFollowing , R.id.tvNbTotalFollowing->{
                    val intent = Intent(requireContext(), FollowersAndFollowingActivity::class.java)
                    intent.putExtra("FollowersType", "Following")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }


            }
        })
    }


    /**  api response observer  **/
    private fun initObserver() {
        viewModel.commonObserver.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()

                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getProfileApi" -> {
                            try {
                                val myDataModel: UserProfile? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        binding.bean = myDataModel.data.user
                                        sharedPrefManager.setProfileData(myDataModel)

                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("error", "getProfileApi: $e")
                            } finally {
                                hideLoading()
                            }
                        }
                        "postPublisherId" -> {
                            val myDataModel: PlayerProfileResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel?.data != null) {
                                if (currentPage == 1) {
                                    myDataModel.data.let {
                                        playerPostAdapter.setList(it)
                                    }
                                } else {
                                    playerPostAdapter.addToList(myDataModel.data)
                                }

                                isLastPage = currentPage == myDataModel.pagination?.totalPages


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

    private fun paginationHandel() {
        binding.rvPost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }
        })

    }


    /** handle adapter **/
    private fun initAvatarAdapter() {
        playerPostAdapter =
            SimpleRecyclerViewAdapter(R.layout.player_post_rv_item, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.clPlayerPost -> {
                        val intent =
                            Intent(requireActivity(), PlayerPostDetailsActivity::class.java)
                        intent.putExtra("playerPost", m)
                        startActivity(intent)
                    }
                }
            }

        binding.rvPost.adapter = playerPostAdapter
    }

    /**
     *  load more function call
     **/
    private fun loadMoreItems() {
        isLoading = true
        currentPage++
        val data = HashMap<String, Any>()
        var userId = sharedPrefManager.getLoginData()?.data?.user?.id
        data["page"] = currentPage
        data["limit"] = 20
        data["type"] = "players"
        viewModel.postPublisherId(Constants.POST_PUBLISHER_ID + "$userId", data)
    }





    override fun onResume() {
        super.onResume()
        // api call
        val data  = HashMap<String, Any>()
        viewModel.getProfileApi(Constants.USER_PROFILE,data)
    }
}