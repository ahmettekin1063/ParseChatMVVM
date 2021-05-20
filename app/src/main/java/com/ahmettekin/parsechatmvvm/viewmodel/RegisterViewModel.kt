package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.ahmettekin.parsechatmvvm.view.fragment.RegisterFragmentDirections
import com.parse.ParseUser
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : BaseViewModel(application) {
    val registerStatus = MutableLiveData<Boolean>()

    fun registerWithEmailAndPassword(email: String?, password: String?,passwordAgain:String?, v:View){
        if (!email.isNullOrEmpty() && !password.isNullOrEmpty() && !passwordAgain.isNullOrEmpty() && email.contains("@")) {
            if (password == passwordAgain){
                registerStatus.value = true
                newUserAccount(email,password, v)
            }else{
                registerStatus.value = false
                Toast.makeText(getApplication(),"Passwords are different", Toast.LENGTH_SHORT).show()
            }
        }else{
            registerStatus.value = false
            Toast.makeText(getApplication(),"Try Again..", Toast.LENGTH_SHORT).show()
        }
    }

    private fun newUserAccount(email: String, password: String, v:View) {
        launch {
            val user = ParseUser()
            user.username = email.substring(0, email.indexOf("@"))
            user.setPassword(password)
            user.email = email
            user.signUpInBackground {
                if (it != null) {
                    registerStatus.value = false
                    Toast.makeText(getApplication(), it.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    registerStatus.value = false
                    goToLoginFragment(v)
                }
            }
        }
    }

    private fun goToLoginFragment(v: View) {
        val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        Navigation.findNavController(v).navigate(action)
    }

}