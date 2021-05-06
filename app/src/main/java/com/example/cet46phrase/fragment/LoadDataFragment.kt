package com.example.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentLoadDataBinding
import com.example.cet46phrase.viewmodel.FragmentLoadDataViewModel


class LoadDataFragment : Fragment() {

    lateinit var dataBinding: FragmentLoadDataBinding
    lateinit var viewModel: FragmentLoadDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLoadDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_load_data, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        onSubscribe()
        viewModel.load().subscribe({
            dataBinding.progressBar.progress = it
            dataBinding.tvProgress.text = "已加载 ${it * 100 / viewModel.count}%"
        }, {
            Log.e("main", "load ERROR:${it.message}")
            it.printStackTrace()
        }, {
            val sharedPreferences =
                requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
            val edit = sharedPreferences.edit()
            edit.putBoolean("init", true)
            edit.apply()
            dataBinding.viewLoad.visibility = View.GONE
            dataBinding.viewDone.visibility = View.VISIBLE
        })
    }

    private fun initView() {
        dataBinding.progressBar.max = viewModel.count
    }

    private fun initListener() {
        dataBinding.btnReturn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onSubscribe() {

    }
}