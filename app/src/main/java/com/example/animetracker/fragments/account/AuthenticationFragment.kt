package com.example.animetracker.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.animetracker.R
import com.example.animetracker.databinding.FragmentAuthenticationBinding
import com.google.firebase.auth.FirebaseAuth


class AuthenticationFragment : Fragment() {

    private lateinit var binding: FragmentAuthenticationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
            //перевірка чи користувач залогінений(якщо він вимикав та  вмикав додаток)
            if(FirebaseAuth.getInstance().currentUser==null)//якщо він не залогінений то показується фрагмент для логіну
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.aut, LoginFragment()).commit()
            else//якщо залогінений, то сторінка акаунту
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.aut, AccountFragment()).commit()

            return binding.root
    }

}