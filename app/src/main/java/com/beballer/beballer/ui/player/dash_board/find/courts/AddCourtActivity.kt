package com.beballer.beballer.ui.player.dash_board.find.courts

import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.beballer.beballer.R
import com.beballer.beballer.base.BaseActivity
import com.beballer.beballer.base.BaseViewModel
import com.beballer.beballer.databinding.ActivityAddCourtBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCourtActivity : BaseActivity<ActivityAddCourtBinding>() {
    private val viewModel: CourtsFragmentVM by viewModels()

    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.courtNavigationHost) as NavHostFragment).navController
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_add_court
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        setupNavigation()
    }

    /**
     * setup navigation
     */
    private fun setupNavigation() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                navController.graph =
                    navController.navInflater.inflate(R.navigation.add_court).apply {
                        setStartDestination(R.id.addCourtFragment)
                    }
            }
        }
    }
}


