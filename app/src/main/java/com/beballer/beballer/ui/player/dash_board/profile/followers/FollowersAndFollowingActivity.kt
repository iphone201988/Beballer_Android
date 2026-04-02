package com.beballer.beballer.ui.player.dash_board.profile.followers

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import com.beballer.beballer.BR
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseActivity
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.base.SimpleRecyclerViewAdapter
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.FollowerUser
import com.beballer.beballer.data.model.FollowersResponse
import com.beballer.beballer.data.model.FollowingResponse
import com.beballer.beballer.data.model.FollowingUser
import com.beballer.beballer.databinding.ActivityFollowersAndFollowingBinding
import com.beballer.beballer.databinding.RvFollowersItemBinding
import com.beballer.beballer.databinding.RvFollowingItemBinding
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowersAndFollowingActivity : BaseActivity<ActivityFollowersAndFollowingBinding>() {

    private val viewModel: FollowersAndFollowingActivityVM by viewModels()

    private lateinit var followingAdapter: SimpleRecyclerViewAdapter<FollowingUser, RvFollowingItemBinding>
    private lateinit var followersAdapter: SimpleRecyclerViewAdapter<FollowerUser, RvFollowersItemBinding>
    private lateinit var fullListFollowing: List<FollowingUser>

    private var isFollowers = true
    private var playerIId: String? = null
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var fullListFollowers: List<FollowerUser>

    private val currentPage = 1

    override fun getLayoutResource(): Int {
        return R.layout.activity_followers_and_following
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // intent data
        val followType = intent.getStringExtra("FollowersType")
        playerIId = intent.getStringExtra("profileId")
        followType?.let {
            isFollowers = it.equals("Followers", ignoreCase = true)
            // Toggle visibility
            binding.svFollowersSearchView.visibility = if (isFollowers) View.VISIBLE else View.GONE
            binding.svFollowingsSearchView.visibility = if (isFollowers) View.GONE else View.VISIBLE
            binding.rvFollowers.visibility = if (isFollowers) View.VISIBLE else View.GONE
            binding.rvFollowing.visibility = if (isFollowers) View.GONE else View.VISIBLE
            // Update title
            binding.tvFollowers.text =
                getString(if (isFollowers) R.string.followers else R.string.following)
            playerIId?.let {
                callFollowersFollowingApi("", playerIId!!)
            }


        }
        // click
        initOnClick()
        // adapter
        initFollowingAdapter()
        initFollowersAdapter()
        // observer
        initObserver()
        // system ui
        setupSystemUI()
        // search
        setupSearch(binding.svFollowersSearchView)
        setupSearch(binding.svFollowingsSearchView)
    }

    /**
     * setup system ui
     */
    private fun setupSystemUI() {
        BindingUtils.applySystemBarMargins(binding.consMain)
        BindingUtils.statusBarStyleWhite(this@FollowersAndFollowingActivity)

    }

    /**
     * setup search
     */
    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                playerIId?.let {
                    callFollowersFollowingApi(query.orEmpty(), playerIId!!)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // cancel previous search
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    playerIId?.let {
                        callFollowersFollowingApi(newText.orEmpty(), playerIId!!)
                    }
                }
                // delay API call (500ms)
                searchHandler.postDelayed(searchRunnable!!, 500)
                return true
            }
        })
    }

    /**
     * call followers following api
     */
    private fun callFollowersFollowingApi(query: String, id: String) {
        val data = hashMapOf<String, Any>(
            "type" to if (isFollowers) "followers" else "following",
            "page" to currentPage,
            "limit" to 50,
            "id" to id
        )

        if (query.isNotEmpty()) {
            data["search"] = query
        }

        if (isFollowers) {
            viewModel.getFollowersFollowingApi(Constants.GET_FOLLOWERS_FOLLOWING, data)
        } else {
            viewModel.getFollowingsFollowingApi(Constants.GET_FOLLOWERS_FOLLOWING, data)
        }
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(this@FollowersAndFollowingActivity) {
            when (it?.id) {
                R.id.ivBack -> {
                    finish()
                }
            }
        }
    }

    /**  api response observer  **/
    private fun initObserver() {
        viewModel.commonObserver.observe(this@FollowersAndFollowingActivity) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getFollowingsFollowingApi" -> {
                            try {
                                val myDataModel: FollowingResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        fullListFollowing =
                                            myDataModel.data.followingUser as List<FollowingUser>
                                        followingAdapter.list = fullListFollowing
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("error", "getFollowersFollowingApi: $e")
                            } finally {
                                hideLoading()
                            }
                        }

                        "getFollowersFollowingApi" -> {
                            try {
                                val myDataModel: FollowersResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        fullListFollowers =
                                            myDataModel.data.followerUser as List<FollowerUser>
                                        followersAdapter.list = fullListFollowers
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("error", "getFollowersFollowingApi: $e")
                            } finally {
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

    /** handle following adapter **/
    private fun initFollowingAdapter() {
        followingAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_following_item, BR.bean) { _, _, _ ->

            }

        binding.rvFollowing.adapter = followingAdapter

    }

    /**
     * handle followers adapter
     */
    private fun initFollowersAdapter() {
        followersAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_followers_item, BR.bean) { _, _, _ ->

            }

        binding.rvFollowers.adapter = followersAdapter
    }


}
