package com.example.shopingapp.view

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentMainBinding
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ TO‘G‘RI NavController (nested NavHost)
        val navController =
            childFragmentManager.findFragmentById(R.id.main_host_fragment)
                ?.findNavController()
                ?: return

        // BottomNavigation ↔ NavController
        binding.mainBottomNavigation.setupWithNavController(navController)

        // ✅ ANDROID 15/16 INSETS FIX (BottomNav yuqoriga chiqadi)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainBottomNavigation) { v, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bottomInset
            v.layoutParams = params

            insets
        }

        // 🔥 MUHIM QISM — DESTINATION LISTENER
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.detailFragment,
                R.id.loginFragment,
                R.id.registerFragment -> {
                    binding.mainBottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.mainBottomNavigation.visibility = View.VISIBLE
                }
            }

        }

        // Drawer menu
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }
    }
}
