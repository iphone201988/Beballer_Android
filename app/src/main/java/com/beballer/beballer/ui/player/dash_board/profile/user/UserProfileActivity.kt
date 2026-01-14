package com.beballer.beballer.ui.player.dash_board.profile.user

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseActivity
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.databinding.ActivityUserProfileBinding
import com.beballer.beballer.ui.player.dash_board.profile.ProfileFragmentVM
import com.beballer.beballer.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : BaseActivity<ActivityUserProfileBinding>() {
    private val viewModel: ProfileFragmentVM by viewModels()
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.userProfileNavigationHost) as NavHostFragment).navController
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_user_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // Status bar setup
        BindingUtils.statusBarStyle(this)
        BindingUtils.statusBarTextColor(this, true)
        setupNavigation()
    }

    /**
     * setup navigation
     */
    private fun setupNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val graph = navController.navInflater.inflate(R.navigation.user_profile)
                val fromWhere = intent.getStringExtra("userType")
                when (fromWhere) {
                    "EditProfile" -> {
                        graph.setStartDestination(R.id.editFragment)
                        navController.setGraph(graph, null)
                    }

                    "ShareProfile" -> {
                        graph.setStartDestination(R.id.shareProfileFragment)
                        navController.setGraph(graph, null)
                    }

                    "Position" -> {
                        graph.setStartDestination(R.id.positionFragment)
                        navController.setGraph(graph, null)
                    }

                    "Friend" -> {
                        graph.setStartDestination(R.id.friendFragment)
                        navController.setGraph(graph, null)
                    }

                    "team" -> {
                        graph.setStartDestination(R.id.teamFragment)
                        navController.setGraph(graph, null)
                    }

                    "share" -> {
                        graph.setStartDestination(R.id.shareProfileFragment)
                        navController.setGraph(graph, null)
                    }

                    "settings" -> {
                        graph.setStartDestination(R.id.settingsFragment)
                        navController.setGraph(graph, null)
                    }

                    "suggestion" -> {
                        graph.setStartDestination(R.id.suggestionFragment)
                        navController.setGraph(graph, null)
                    }

                    "policy" -> {
                        graph.setStartDestination(R.id.policyFragment)
                        navController.setGraph(graph, null)
                    }

                    "editImage" -> {
                        graph.setStartDestination(R.id.editImageFragment)
                        navController.setGraph(graph, null)
                    }

                    "courtFragment" -> {
                        graph.setStartDestination(R.id.courtFragment)
                        navController.setGraph(graph, null)
                    }

                    "courtDetailsFragment" -> {
                        val courtId = intent.getStringExtra("courtId")
                        val bundle = Bundle()
                        bundle.putString("courtId", courtId)
                        graph.setStartDestination(R.id.courtsDetailsFragment)
                        navController.setGraph(graph, bundle)
                    }

                    "notification" -> {
                        graph.setStartDestination(R.id.notificationFragment)
                        navController.setGraph(graph, null)
                    }

                    "gameFragment" -> {
                        graph.setStartDestination(R.id.myGameFragment)
                        navController.setGraph(graph, null)
                    }

                    "ticketFragment" -> {
                        graph.setStartDestination(R.id.ticketFragment)
                        navController.setGraph(graph, null)
                    }

                    "tournamentFragment" -> {
                        graph.setStartDestination(R.id.tournamentsFragment)
                        navController.setGraph(graph, null)
                    }

                    "campsFragment" -> {
                        graph.setStartDestination(R.id.campsFragment)
                        navController.setGraph(graph, null)
                    }


                    "campsDetails" -> {
                        graph.setStartDestination(R.id.campsDetailsFragment)
                        navController.setGraph(graph, null)
                    }

                    "createGame" -> {
                        graph.setStartDestination(R.id.createGameFragment)
                        navController.setGraph(graph, null)
                    }

                    "gameDetails" -> {
                        graph.setStartDestination(R.id.gameDetailsFragment)
                        navController.setGraph(graph, null)

                    }

                    "tournamentDetails" -> {
                        graph.setStartDestination(R.id.gameDetailsFragment)
                        navController.setGraph(graph, null)
                    }

                    "findGameFragment" -> {
                        graph.setStartDestination(R.id.findGameFragment)
                        navController.setGraph(graph, null)
                    }

                    "singleDataFragment" -> {
                        val bundle = intent.extras
                        graph.setStartDestination(R.id.singleDataFragment)
                        navController.setGraph(graph, bundle)
                    }

                    "showMapFragment" -> {
                        val bundle = intent.extras
                        graph.setStartDestination(R.id.showMapFragment)
                        navController.setGraph(graph, bundle)
                    }


                    else -> {
                        graph.setStartDestination(R.id.editFragment)
                        navController.setGraph(graph, null)
                    }
                }

            }
        }
    }

    /**
     * edge to edge margin
     */
    private fun enableEdgeToEdgePaddingTobBottom(rootView: View) {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}