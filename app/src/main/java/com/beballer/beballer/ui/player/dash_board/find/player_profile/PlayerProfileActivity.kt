package com.beballer.beballer.ui.player.dash_board.find.player_profile

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseActivity
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.api.Constants.profileId
import com.beballer.beballer.data.api.Constants.userPostId
import com.beballer.beballer.data.model.CommonResponse
import com.beballer.beballer.data.model.PlayerProfileByIdResponse
import com.beballer.beballer.databinding.ActivityPlayerProfileBinding
import com.beballer.beballer.databinding.SubscribeBotomItemBinding
import com.beballer.beballer.ui.player.dash_board.profile.followers.FollowersAndFollowingActivity
import com.beballer.beballer.utils.BaseCustomBottomSheet
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.utils.Status
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerProfileActivity : BaseActivity<ActivityPlayerProfileBinding>() {
    private val viewModel: PlayerProfileActivityVM by viewModels()
    var isSubscribed: Boolean = false
    var userProfileId: String? = null


    private lateinit var subscribeBottomItem: BaseCustomBottomSheet<SubscribeBotomItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.activity_player_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        enableEdgeToEdgePaddingTobBottom(binding.root)
        // Status bar setup
        BindingUtils.statusBarStyle(this)
        BindingUtils.statusBarTextColor(this, true)
        // check
        binding.pos = 1
        binding.first.visibility = View.INVISIBLE
        binding.second.visibility = View.VISIBLE
        binding.third.visibility = View.VISIBLE
        initClick()

        // api call
        val userId = intent.getStringExtra("playerProfile")
        userId?.let {
            userProfileId = it
            val useId: String =
                sharedPrefManager.getLoginData()?.data?.user?._id.takeIf { id -> !id.isNullOrEmpty() }
                    ?: ""
            if (useId == it) {
                binding.tvSubscribe.visibility = View.GONE
            } else {
                binding.tvSubscribe.visibility = View.VISIBLE
            }

            val hashMap = HashMap<String, Any>()
            hashMap["id"] = it
            viewModel.getUserById(Constants.USER_GET_USER_BY_ID, hashMap)
        }
        // observer
        initObserver()
    }

    /***
     * all click listener
     */
    private fun initClick() {
        viewModel.onClick.observe(this@PlayerProfileActivity) {
            when (it?.id) {
                R.id.cancelImage -> {
                    finish()
                }

                R.id.tvPost -> {
                    binding.pos = 1
                    binding.viewPagerProfile.currentItem = 0
                    binding.first.visibility = View.INVISIBLE
                    binding.second.visibility = View.VISIBLE
                    binding.third.visibility = View.VISIBLE
                }

                R.id.tvClassmates -> {
                    binding.pos = 2
                    binding.viewPagerProfile.currentItem = 1
                    binding.first.visibility = View.INVISIBLE
                    binding.second.visibility = View.INVISIBLE
                    binding.third.visibility = View.VISIBLE
                }

                R.id.tvStatistiques -> {
                    binding.pos = 3
                    binding.viewPagerProfile.currentItem = 2
                    binding.first.visibility = View.VISIBLE
                    binding.second.visibility = View.INVISIBLE
                    binding.third.visibility = View.INVISIBLE
                }

                R.id.tvInventaire -> {
                    binding.pos = 4
                    binding.viewPagerProfile.currentItem = 3
                    binding.first.visibility = View.VISIBLE
                    binding.second.visibility = View.VISIBLE
                    binding.third.visibility = View.INVISIBLE
                }

                R.id.cardView -> {
                    subscribeBottomSheet(isSubscribed, userProfileId)

                }

                R.id.tvTotalFollowersCount, R.id.tvTotalFollowers -> {
                    val intent = Intent(
                        this@PlayerProfileActivity, FollowersAndFollowingActivity::class.java
                    )
                    intent.putExtra("FollowersType", "Followers")
                    intent.putExtra("profileId", profileId)
                    startActivity(intent)
                }

                R.id.tvTotalFollowing, R.id.tvNbTotalFollowing -> {
                    val intent = Intent(
                        this@PlayerProfileActivity,
                        FollowersAndFollowingActivity::class.java
                    )
                    intent.putExtra("FollowersType", "Following")
                    intent.putExtra("profileId", profileId)
                    startActivity(intent)
                }


            }
        }
    }


    /**
     * api response observer
     */
    private fun initObserver() {
        viewModel.commonObserver.observe(this@PlayerProfileActivity) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()

                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getUserById" -> {
                            try {
                                val myDataModel: PlayerProfileByIdResponse? =
                                    BindingUtils.parseJson(it.data.toString())

                                val user = myDataModel?.data?.user ?: return@observe

                                isSubscribed = user.isSubscribed == true
                                binding.bean = user
                                profileId = user._id
                                userPostId = user.id

                                // Height
                                val heightCm = user.height ?: 0
                                binding.tvPlayerHeight.text =
                                    BindingUtils.convertCmToFeetInchesFormatted(heightCm)
                                // Age
                                val age = user.birthDate?.let { date ->
                                    BindingUtils.calculateAgeFromIsoLegacy(date)
                                }
                                binding.tvPlayerAge.text = "$age"

                                // adapter
                                val adapter =
                                    PlayerProfilePagerAdapter(supportFragmentManager, lifecycle)
                                binding.viewPagerProfile.adapter = adapter

                            } catch (e: Exception) {
                                Log.e("error", "getUserById: $e")
                            } finally {
                                hideLoading()
                            }
                        }

                        "postSubscribeApi" -> {
                            try {
                                val myDataModel: CommonResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel?.success == true) {
                                    showSuccessToast(myDataModel.message.toString())
                                    // toggle value
                                    isSubscribed = !isSubscribed
                                    if (isSubscribed) {
                                        binding.tvSubscribe.text = getString(R.string.unsubscribe)
                                        binding.tvSubscribe.setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@PlayerProfileActivity, R.color.text_light
                                            )
                                        )
                                    } else {
                                        binding.tvSubscribe.text = getString(R.string.subscribe)
                                        binding.tvSubscribe.setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@PlayerProfileActivity, R.color.beballer_blue
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("error", "postSubscribeApi: $e")
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

    /**
     * subscribe bottom sheet
     */
    private fun subscribeBottomSheet(subscribe: Boolean?, userId: String?) {
        subscribeBottomItem =
            BaseCustomBottomSheet(this@PlayerProfileActivity, R.layout.subscribe_botom_item) {
                when (it?.id) {
                    R.id.tvCancel -> {
                        subscribeBottomItem.dismiss()
                    }

                    R.id.tvSubscribe -> {
                        subscribeBottomItem.dismiss()
                        val subscribeUser = subscribe == true
                        val data = HashMap<String, Any>()
                        userId?.let { id ->
                            data["subscribed"] = !subscribeUser
                            viewModel.postSubscribeApi(Constants.USER_SUBSCRIBE + "?id=$id", data)
                        }
                    }
                }
            }
        subscribeBottomItem.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        subscribeBottomItem.behavior.isDraggable = true
        subscribeBottomItem.create()
        subscribeBottomItem.show()

        if (subscribe == true) {
            subscribeBottomItem.binding.tvSubscribe.text = getString(R.string.unsubscribe)
            subscribeBottomItem.binding.tvSubscribe.setTextColor(
                ContextCompat.getColor(
                    this@PlayerProfileActivity, R.color.red_F27070
                )
            )
        } else {
            subscribeBottomItem.binding.tvSubscribe.setTextColor(
                ContextCompat.getColor(
                    this@PlayerProfileActivity, R.color.blue
                )
            )
        }

    }

    /**
     * edge to edge margin
     */
    private fun enableEdgeToEdgePaddingTobBottom(rootView: View) {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0,systemBars.top, 0, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}