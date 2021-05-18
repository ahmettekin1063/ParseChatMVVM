package com.ahmettekin.parsechatmvvm.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.databinding.FragmentRegisterBinding
import com.ahmettekin.parsechatmvvm.viewmodel.RegisterViewModel

class RegisterFragment : Fragment() {
    private lateinit var viewModel :RegisterViewModel
    private lateinit var dataBinding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_register,container,false)
        dataBinding.fragment = this
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        observeLiveData()
    }

    fun onRegisterClicked(v:View){
        val email = dataBinding.etRegisterEmail.text.toString()
        val password = dataBinding.etRegisterPassword.text.toString()
        val passwordAgain = dataBinding.etRegisterPasswordAgain.text.toString()
        viewModel.registerWithEmailAndPassword(email, password, passwordAgain, v)
    }

    private fun observeLiveData() {
        viewModel.registerStatus.observe(viewLifecycleOwner,{ registering->
            registering?.let {
                if (it){
                    dataBinding.pbRegister.visibility = View.VISIBLE
                }else{
                    dataBinding.pbRegister.visibility = View.GONE
                }
            }
        })
    }
}