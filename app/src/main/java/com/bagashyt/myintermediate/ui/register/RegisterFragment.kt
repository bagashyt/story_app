package com.bagashyt.myintermediate.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.databinding.FragmentRegisterBinding
import com.bagashyt.myintermediate.utils.animateVisibility
import com.bagashyt.myintermediate.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var registerJob: Job = Job()
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnRegister.setOnClickListener {
                handleRegister()
            }
            btnGotoLogin.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        setLoadingState(true)

        lifecycleScope.launchWhenResumed {
            if (registerJob.isActive) registerJob.cancel()

            registerJob = launch {
                viewModel.userRegister(name, email, password).collect { result ->
                    result.onSuccess {
                        showToast(requireContext(), getString(R.string.register_success))

                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    result.onFailure {
                        showToast(requireContext(), getString(R.string.register_failed))
                        setLoadingState(false)
                    }
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            etName.isEnabled = !isLoading


            if (isLoading) {
                viewLoading.animateVisibility(true)
            } else {
                viewLoading.animateVisibility(false)
            }
        }
    }
}