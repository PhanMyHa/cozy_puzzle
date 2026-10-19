package com.example.cozypuzzle.presentation.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import com.example.cozypuzzle.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private var progressAnimator: ValueAnimator? = null
    private var hasNavigated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                gameRemoteDataSource.syncRemoteData()
            } catch (_: Exception) {
            }
        }

        binding.root.setOnClickListener {
            // Tap to fast-forward splash
            proceedToHome()
        }

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        progressAnimator?.cancel()
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = SPLASH_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val progress = va.animatedValue as Int
                binding.pbSplashProgress.progress = progress
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    proceedToHome()
                }
            })
            start()
        }
    }

    private fun proceedToHome() {
        if (hasNavigated) return
        hasNavigated = true
        progressAnimator?.cancel()
        progressAnimator = null

        if (isAdded && view != null) {
            val nav = findNavController()
            if (nav.currentDestination?.id == R.id.splashFragment) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .setEnterAnim(android.R.anim.fade_in)
                    .setExitAnim(android.R.anim.fade_out)
                    .build()
                try {
                    nav.navigate(R.id.action_splash_to_home, null, navOptions)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onDestroyView() {
        progressAnimator?.cancel()
        progressAnimator = null
        super.onDestroyView()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1400L
    }
}
