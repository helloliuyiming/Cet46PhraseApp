package com.example.cet46phrase.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentMainBinding
import com.example.cet46phrase.viewmodel.FragmentMainViewModel


class MainFragment : Fragment() {

    lateinit var dataBinding: FragmentMainBinding
    lateinit var viewModel: FragmentMainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentMainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)
        return dataBinding.root
    }

}