package com.ahmettekin.parsechatmvvm.view.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.databinding.FragmentLoginBinding
import com.ahmettekin.parsechatmvvm.then
import com.ahmettekin.parsechatmvvm.viewmodel.LoginViewModel

class LoginFragment : Fragment() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var dataBinding: FragmentLoginBinding
    private lateinit var mView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater ,R.layout.fragment_login,container,false)
        dataBinding.fragment = this
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        observeLiveData()
    }

    fun onLoginClicked(){
        viewModel.loginWithEmailAndPassword(dataBinding.etLoginEmail.text.toString(), dataBinding.etLoginPassword.text.toString())
    }

    fun onRegisterClicked(v:View){
        viewModel.goToRegisterFragment(v)
    }

    private fun observeLiveData(){
        viewModel.isLogging.observe(viewLifecycleOwner,{ logging->
            logging?.let{
                dataBinding.pbLogin.visibility = it then View.VISIBLE ?: View.GONE
            }
        })

        viewModel.loginStatus.observe(viewLifecycleOwner,{ loginControl->
            loginControl?.let {
                if (it){
                    viewModel.goToRoomFragment(mView)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loginControl()
    }

}