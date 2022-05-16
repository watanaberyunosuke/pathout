package com.monash.pathout.ui.login

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.monash.pathout.databinding.UserProfileFragmentBinding
import com.monash.pathout.model.User
import com.monash.pathout.viewmodel.FirebaseViewModel

class UserProfileFragment : Fragment() {
    private lateinit var binding: UserProfileFragmentBinding
    private lateinit var firebaseViewModel: FirebaseViewModel
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the View for this fragment
        binding = UserProfileFragmentBinding.inflate(inflater, container, false)

        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]
        user = firebaseViewModel.user.value

        fillInfo()

        return binding!!.root
    }

    private fun fillInfo() {
        binding.profileUsername.text = SpannableStringBuilder(user?.username)
        binding.profileEmail.text = SpannableStringBuilder(user?.email)
        binding.profileFirstName.text = SpannableStringBuilder(user?.firstName)
        binding.profileLastName.text = SpannableStringBuilder(user?.lastName)
        binding.profilePhone.text = SpannableStringBuilder(user?.phone)
        binding.profileAddress.text = SpannableStringBuilder(user?.address)
    }

}