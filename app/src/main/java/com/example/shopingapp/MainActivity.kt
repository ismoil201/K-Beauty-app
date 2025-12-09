package com.example.shopingapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shopingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { v, insets ->
//            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//            v.setPadding(0, 0, 0, bottomInset)
//            insets
//        }

        setContentView(binding.root)

    }
}