package com.example.shopingapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.shopingapp.R
import com.example.shopingapp.adapter.OnboardingAdapter
import com.example.shopingapp.adapter.OnboardingData
import com.example.shopingapp.databinding.FragmentOnBoardingBinding
class OnboardingFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingBinding
    private lateinit var adapter: OnboardingAdapter
    private val dots = ArrayList<ImageView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ ANDROID 15/16 FIX — BUTTON MARGIN
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnGetStart) { v, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin =
                bottomInset + resources.getDimensionPixelSize(R.dimen.onboarding_button_margin)

            v.layoutParams = params
            insets
        }

        setupViewPager()
        setupDots()

        binding.btnGetStart.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_mainFragment)
        }
    }


    private fun setupViewPager() {
        val data = listOf(
            OnboardingData(
                R.drawable.splash11,
                "Various Collections Of The Latest Products",
                "Urna amet, suspendisse ullamcorper ac elit diam facilisis cursus vestibulum."
            ),
            OnboardingData(
                R.drawable.splash22,
                "Complete Collection Of Colors And Sizes",
                "Urna amet, suspendisse ullamcorper ac elit diam facilisis cursus vestibulum."
            ),
            OnboardingData(
                R.drawable.splash5,
                "Find The Most Suitable Outfit For You",
                "Urna amet, suspendisse ullamcorper ac elit diam facilisis cursus vestibulum."
            )
        )

        adapter = OnboardingAdapter(data)
        binding.onboardingViewPager.adapter = adapter

        binding.onboardingViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position)
                }
            })
    }

    private fun setupDots() {
        val size = 3
        for (i in 0 until size) {
            val img = ImageView(requireContext())
            img.setImageResource(R.drawable.dot_inactive)

            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            img.layoutParams = params

            binding.dotIndicator.addView(img)
            dots.add(img)
        }

        dots[0].setImageResource(R.drawable.dot_active)
    }

    private fun updateDots(position: Int) {
        for (i in dots.indices) {
            dots[i].setImageResource(
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }
}
