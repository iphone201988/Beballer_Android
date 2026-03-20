package com.beballer.beballer.ui.organizers.dash_board

import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseActivity
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.data.api.Constants
import com.beballer.beballer.data.model.AccountState
import com.beballer.beballer.data.model.AccountType
import com.beballer.beballer.data.model.LoginApiResponse
import com.beballer.beballer.data.model.User
import com.beballer.beballer.utils.BindingUtils
import com.beballer.beballer.databinding.ActivityOrganizersDashBoardBinding
import com.beballer.beballer.databinding.ItemPopupSwitchAccountBinding
import com.beballer.beballer.ui.player.dash_board.DashboardActivity
import com.beballer.beballer.utils.BaseCustomDialog
import com.beballer.beballer.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrganizersDashBoardActivity : BaseActivity<ActivityOrganizersDashBoardBinding>() {
    private val viewModel: OrganizersDashBoardActivityVM by viewModels()
    private lateinit var switchAccountPopup : BaseCustomDialog<ItemPopupSwitchAccountBinding>

    override fun getLayoutResource(): Int {
        return R.layout.activity_organizers_dash_board
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // set status bar color
        BindingUtils.statusBarStyle(this@OrganizersDashBoardActivity)
        BindingUtils.statusBarTextColor(this@OrganizersDashBoardActivity, true)
        // view
        initView()
        initPopup()
        // click
        initOnClick()
        setupLongPress()
        setObserver()
    }

    private fun initPopup() {
        switchAccountPopup = BaseCustomDialog(this, R.layout.item_popup_switch_account){

        }
    }

    /*** all click handel function ***/
    private fun initOnClick() {
        viewModel.onClick.observe(this@OrganizersDashBoardActivity) {
            when (it?.id) {

            }
        }
    }


    /** handle view **/
    private fun initView() {
        // set bottom nav bar
        binding.organizersBottomNavigation.itemIconTintList = null
        val navController = findNavController(R.id.navHostOrganizersFragment)
        binding.organizersBottomNavigation.setupWithNavController(navController)
        binding.organizersBottomNavigation.setOnNavigationItemSelectedListener { item ->
            val currentDestinationId = navController.currentDestination?.id
            when (item.itemId) {
                R.id.organizersFindFragment -> {
                    if (currentDestinationId != R.id.organizersFindFragment) {
                        navController.popBackStack(R.id.organizersFindFragment, true)
                        navController.navigate(R.id.organizersFindFragment)
                    }
                    true
                }

                R.id.socialFragment -> {
                    if (currentDestinationId != R.id.socialFragment) {
                        navController.popBackStack(R.id.socialFragment, true)
                        navController.navigate(R.id.socialFragment)
                    }
                    true
                }

                R.id.organizersGameFragment -> {
                    if (currentDestinationId != R.id.organizersGameFragment) {
                        navController.popBackStack(R.id.organizersGameFragment, true)
                        navController.navigate(R.id.organizersGameFragment)
                    }
                    true
                }

                R.id.profileFragment -> {
                    if (currentDestinationId != R.id.fragmentOrganizerUserProfile) {
                        navController.popBackStack(R.id.fragmentOrganizerUserProfile, true)
                        navController.navigate(R.id.fragmentOrganizerUserProfile)
                    }
                    true
                }


                else -> false
            }
        }
    }

    private fun setupLongPress() {

        val menuView = binding.organizersBottomNavigation.getChildAt(0) as ViewGroup

        for (i in 0 until menuView.childCount) {
            val itemView = menuView.getChildAt(i)

            itemView.setOnLongClickListener {
                val itemId = binding.organizersBottomNavigation.menu.getItem(i).itemId

                if (itemId == R.id.profileFragment) {
                    showAccountPopup(sharedPrefManager.getLoginData()?.data?.user)
                    true
                } else {
                    false
                }
            }
        }
    }


    private fun showAccountPopup(user: User?) {

        val currentAccountType = AccountType.ORGANIZER
        // ideally get from SharedPreferences

        val state = getAccountState(user, currentAccountType) // 👈 HERE



        switchAccountPopup.binding.tvNotification.text = state.titleText()
        switchAccountPopup.binding.btnConfirm.text = state.buttonTitle()

        switchAccountPopup.binding.btnConfirm.setOnClickListener {

            when (state) {

                AccountState.NO_PLAYER_ACCOUNT -> {
                    // create player account
                }

                AccountState.NO_ORGANIZER_ACCOUNT -> {
                    // create organizer account
                }

                AccountState.SWITCH_TO_PLAYER -> {
                    switchToAccount("player")
                }

                AccountState.SWITCH_TO_ORGANIZER -> {
                    switchToAccount("organizer")


                }
            }

            switchAccountPopup.dismiss()
        }

        switchAccountPopup.show()
    }
    fun getAccountState(
        user: User?,
        currentAccountType: AccountType
    ): AccountState {

        val hasPlayer = user?.hasPlayerAccount ?: false
        val hasOrganizer = user?.hasOrganizerAccount ?: false

        return when (currentAccountType) {

            AccountType.PLAYER -> {
                if (hasOrganizer) {
                    AccountState.SWITCH_TO_ORGANIZER
                } else {
                    AccountState.NO_ORGANIZER_ACCOUNT
                }
            }

            AccountType.ORGANIZER -> {
                if (hasPlayer) {
                    AccountState.SWITCH_TO_PLAYER
                } else {
                    AccountState.NO_PLAYER_ACCOUNT
                }
            }
        }
    }
    fun switchToAccount(accountType: String) {
        val token  = sharedPrefManager.getToken()
        val data: HashMap<String, Any> = hashMapOf(
            "id" to sharedPrefManager.getLoginData()?.data?.user?.id.toString(),
            "longitude" to BindingUtils.long,
            "latitude" to BindingUtils.lat,
            "type" to accountType,
            "deviceToken" to token.toString() ,
            "deviceType" to 2
        )
        viewModel.commonLoginAPi(data, Constants.MOBILE_LOGIN)

    }


    private fun setObserver() {
        viewModel.commonObserver.observe(this) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "commonLoginAPi" -> {
                            try {
                                val myDataModel: LoginApiResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    myDataModel.data?.token?.let { it1 ->
                                        sharedPrefManager.saveToken(
                                            it1
                                        )
                                    }
                                    sharedPrefManager.setLoginData(myDataModel)
                                    if (myDataModel.data?.user?.hasPlayerAccount == true){
                                        val intent =
                                            Intent(this, OrganizersDashBoardActivity::class.java)
                                        startActivity(intent)
                                        finishAffinity()

                                    }
                                    else{
                                        val intent =
                                            Intent(this, DashboardActivity::class.java)
                                        startActivity(intent)
                                        finishAffinity()
                                    }



                                }

                            } catch (e: Exception) {
                                Log.e("error", "commonLoginAPi: $e")
                            }finally {
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


}