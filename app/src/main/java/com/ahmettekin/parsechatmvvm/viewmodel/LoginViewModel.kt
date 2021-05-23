package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.ahmettekin.parsechatmvvm.util.senderId
import com.ahmettekin.parsechatmvvm.view.fragment.LoginFragmentDirections
import com.parse.ParseInstallation
import com.parse.ParseUser
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : BaseViewModel(application) {
    val isLogging = MutableLiveData<Boolean>()
    val loginStatus = MutableLiveData<Boolean>()

    fun loginWithEmailAndPassword(email: String?, password: String?) {
        launch {
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty() && email.contains("@")) {
                isLogging.value = true
                val userName = email.substring(0, email.indexOf("@"))
                ParseUser.logInInBackground(userName, password) { user, e ->
                    if (e != null) {
                        isLogging.value = false
                        Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        isLogging.value = false
                        loginStatus.value = true
                        Toast.makeText(getApplication(), "Welcome " + user.username.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(getApplication(), "Try Again..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun goToRegisterFragment(v: View) {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        Navigation.findNavController(v).navigate(action)
    }

    fun loginControl() {
        launch {
            ParseUser.getCurrentUser()?.let {
                loginStatus.value = it.isAuthenticated
            }
        }
    }

    fun goToRoomFragment(v:View) {
        val action = LoginFragmentDirections.actionLoginFragmentToRoomsFragment()
        Navigation.findNavController(v).navigate(action)
        val installation = ParseInstallation.getCurrentInstallation()
        //installation.put("GCMSenderId", senderId)
        installation.put("user", ParseUser.getCurrentUser())
        installation.saveInBackground()
    }

}