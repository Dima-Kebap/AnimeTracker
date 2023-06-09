package com.example.animetracker.fragments.account

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.animetracker.R
import com.example.animetracker.dataBase.Dao
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.dataBase.MyAnime
import com.example.animetracker.databinding.FragmentAccountBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class AccountFragment : Fragment() {

    private lateinit var userKey: String
    private lateinit var binding: FragmentAccountBinding
    private lateinit var db: DatabaseReference
    private lateinit var myDB: Dao


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance().getReference("users")
        db.keepSynced(true)
        userKey = FirebaseAuth.getInstance().uid.toString()
        myDB = MainDb.getDb(requireActivity()).getDao()
        optionsDB()
        return binding.root

    }


    private fun optionsDB() = with(binding) {//можливості користувача якщо він залогінений
        butTheme.setOnClickListener {
            val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Встановити нову тему
            if (currentMode == Configuration.UI_MODE_NIGHT_YES){
                butTheme.setImageResource(R.drawable.day_theme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
             else{
                butTheme.setImageResource(R.drawable.night_theme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
             }
            requireActivity()
                .getSharedPreferences("APP_SETTINGS", AppCompatActivity.MODE_PRIVATE)
                .edit()
                .putBoolean("IS_DARK_THEME", currentMode != Configuration.UI_MODE_NIGHT_YES)
                .apply()
            requireActivity().recreate()
        }
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES)
            butTheme.setImageResource(R.drawable.night_theme)


        butDownload.setOnClickListener {//скачати дані з хмари
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.confirmation))
                .setMessage("It will replace your current data")
                .setCancelable(false)
                .setPositiveButton("confirm") { _, _ ->
                    if (checkInternet())
                        downloadFromDB()
                }
                .setNegativeButton("cancel") { _, _ -> }
                .create().show()
        }

        butDelData.setOnClickListener {//очистити дані додатку(не хмари)
            AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirmation))
            .setMessage("This will delete all data about your anime(list, rating, progress) from this gadget")
            .setCancelable(false)
            .setPositiveButton("confirm") { _, _ ->
                Thread{myDB.deleteAll()}.start()
            }
            .setNegativeButton("cancel") { _, _ ->
            }
            .create().show()
        }

        butDelDb.setOnClickListener {//очистити дані хмари для цього користувача
            if (checkInternet()) {
                AlertDialog.Builder(context)
                .setTitle(getString(R.string.confirmation))
                .setMessage("This will delete all your data from the server")
                .setCancelable(false)
                .setPositiveButton("confirm") { _, _ ->
                    db.child(userKey).removeValue()
                }
                .setNegativeButton("cancel") { _, _ -> }
                .create().show()
            }
        }

        butUpload.setOnClickListener {//завантажити дані на хмару
            if (checkInternet())
                saveDB()
        }

        butLogOut.setOnClickListener {//вийти з акаунту
            GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.Builder().build())
                .signOut()
            Firebase.auth.signOut()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.aut, LoginFragment()).commit()
        }
    }


    private fun saveDB() = with(binding) {
        db.child(userKey).removeValue()
        val userVALUES = db.child(userKey)
        Thread{
            for (value in myDB.getAll())
                userVALUES.child("anime").child(value.id.toString()).setValue(Gson().toJson(value))
        }.start()

        Toast.makeText(context, "The data is uploaded to the cloud", Toast.LENGTH_SHORT).show()
    }

    private fun checkInternet(): Boolean {
        val networkInfo = (requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        return if (networkInfo == null || !networkInfo.isConnected){
            Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
            false
        }
        else
            true
    }

    private fun downloadFromDB() {
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(userKey)) {//перевірка на наявність даних користувача у хмарі
                   Thread{myDB.deleteAll()}.start()
                    db.child("$userKey/anime")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {//зчитуваня даних з хмари
                                Thread{
                                    for (obj in dataSnapshot.value as ArrayList<*>)
                                        if(obj!=null){//чомусь перше значення = null
                                            val value = Gson().fromJson(obj.toString(), MyAnime::class.java)//запис даних на пристрій у БД
                                            myDB.replaceItem(MyAnime(value.id,value.listName,value.rating,value.episodes))
                                    }
                                }.start()
                                Toast.makeText(context, "The data is downloaded from the cloud", Toast.LENGTH_SHORT).show()
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })
                } else
                    Toast.makeText(context, "No data in the cloud", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обробляємо помилку
            }
        })
    }

}