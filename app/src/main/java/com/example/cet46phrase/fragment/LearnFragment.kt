package com.example.cet46phrase.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentLearnBinding
import com.example.cet46phrase.viewmodel.FragmentLearnViewModel

class LearnFragment : Fragment() {

    lateinit var viewModel: FragmentLearnViewModel
    lateinit var dataBinding: FragmentLearnBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLearnViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_learn, container, false)
        return dataBinding.root
    }

}