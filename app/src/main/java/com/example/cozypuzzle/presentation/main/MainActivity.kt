package com.example.cozypuzzle.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cozypuzzle.R
import com.example.cozypuzzle.domain.repository.DailyTasksRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dailyTasksRepository: DailyTasksRepository

    private var walkingCatView: com.example.cozypuzzle.core.customview.WalkingCatView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val walkingCatView = findViewById<com.example.cozypuzzle.core.customview.WalkingCatView>(R.id.walking_cat_view)
        this.walkingCatView = walkingCatView

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in topLevelTabs) {
                bottomNav.visibility = android.view.View.VISIBLE
                walkingCatView?.visibility = android.view.View.VISIBLE
                walkingCatView?.resumeWalking()
                if (bottomNav.selectedItemId != destination.id) {
                    bottomNav.menu.findItem(destination.id)?.isChecked = true
                }
            } else {
                bottomNav.visibility = android.view.View.GONE
                walkingCatView?.visibility = android.view.View.GONE
                walkingCatView?.pauseWalking()
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(R.id.homeFragment, inclusive = false, saveState = true)
                    .setEnterAnim(0)
                    .setExitAnim(0)
                    .setPopEnterAnim(0)
                    .setPopExitAnim(0)
                    .build()
                try {
                    navController.navigate(item.itemId, null, navOptions)
                    true
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }
        }

        bottomNav.setOnItemReselectedListener {
            // No-op to avoid reloading or stuttering when already on the tab
        }

        val dailyBadge = bottomNav.getOrCreateBadge(R.id.dailyTasksFragment)
        dailyBadge.backgroundColor = ContextCompat.getColor(this, R.color.cozy_badge_red)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dailyTasksRepository.observeUnclaimedCount().collect { unclaimedCount ->
                    if (unclaimedCount > 0) {
                        dailyBadge.isVisible = true
                        dailyBadge.number = unclaimedCount
                    } else {
                        dailyBadge.isVisible = false
                        dailyBadge.clearNumber()
                    }
                }
            }
        }
    }

    override fun onPause() {
        walkingCatView?.pauseWalking()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val destId = navHostFragment?.navController?.currentDestination?.id
        if (destId in topLevelTabs) {
            walkingCatView?.resumeWalking()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            walkingCatView?.pauseWalking()
        } else {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val destId = navHostFragment?.navController?.currentDestination?.id
            if (destId in topLevelTabs) {
                walkingCatView?.resumeWalking()
            }
        }
    }

    private companion object {
        val topLevelTabs = setOf(
            R.id.homeFragment,
            R.id.myCatsFragment,
            R.id.dailyTasksFragment,
            R.id.profileFragment
        )
    }
}
