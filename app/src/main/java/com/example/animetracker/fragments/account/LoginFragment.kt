package com.example.animetracker.fragments.account

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.animetracker.R
import com.example.animetracker.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var launcher: ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null)
                    firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException){
                Log.d("MyLog","Api exception")
            }
        }
        init()
        return binding.root
    }

private fun getClient(): GoogleSignInClient{
    val gso = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(requireActivity(), gso)
}

    private fun signInWithGoogle(){
        launcher.launch(getClient().signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful)
                userLogined()
             else
                Toast.makeText(context, "Google account login error", Toast.LENGTH_SHORT).show()
        }
    }


    private fun init() = with(binding){
        butLogin.setOnClickListener {
            var email = accEmail.text.toString()
            val pass = accPassword.text.toString()
            if (email != "" && pass != "") {
                email += "@gmail.com"
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful)  //якщо користувач успішно залогінився
                        userLogined()
                    else{
                        val networkInfo = (requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
                        if (networkInfo == null || !networkInfo.isConnected )
                            Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
                        else
                            Toast.makeText(context, "!!Incorrect password or email!!", Toast.LENGTH_SHORT).show()
                    }

                }
            } else
                Toast.makeText(context, "Some field is empty", Toast.LENGTH_SHORT).show()
        }
        butRegistration.setOnClickListener {
            var login = accEmail.text.toString()
            val pass = accPassword.text.toString()
            Toast.makeText(context, "$login,  $pass", Toast.LENGTH_SHORT).show()
            if (login != "" && pass != "") {
                login += "@gmail.com"
                auth.createUserWithEmailAndPassword(login, pass).addOnCompleteListener {
                    if (it.isSuccessful) //якщо користувач успішно зареєструвався
                        userLogined()
                    else
                        Toast.makeText(context, "Check the correctness of the entered data", Toast.LENGTH_SHORT).show()
                }
            } else
                Toast.makeText(context, "Some field is empty", Toast.LENGTH_SHORT).show()
        }
        butGoogleLogin.setOnClickListener{
            signInWithGoogle()
        }
    }

    private fun userLogined() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.aut, AccountFragment())
            .commit()
    }
}




