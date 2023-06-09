package com.example.animetracker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteDatabase("anime.db")
        // Відкриваємо з'єднання з базою даних
        Room.databaseBuilder(this, MainDb::class.java, "anime.db").build().openHelper.writableDatabase

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val isDarkTheme = getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE).getBoolean("IS_DARK_THEME", false)
        if (isDarkTheme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
         else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val networkInfo = (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected) {
            AlertDialog.Builder(this)
            .setTitle("No internet")
            .setCancelable(false)
            .setPositiveButton("try again") { _, _ ->
                startActivity(Intent(this, this::class.java))
                finish()
            }
            .setNegativeButton("exit") { _, _ -> finish() }
            .create().show()
        }
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

    }
}