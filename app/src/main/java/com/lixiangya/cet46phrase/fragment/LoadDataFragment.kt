package com.lixiangya.cet46phrase.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentLoadDataBinding
import com.lixiangya.cet46phrase.util.PreferencesUtil
import com.lixiangya.cet46phrase.viewmodel.FragmentLoadDataViewModel

class LoadDataFragment : Fragment() {

    lateinit var dataBinding: FragmentLoadDataBinding
    lateinit var viewModel: FragmentLoadDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[FragmentLoadDataViewModel::class.java]
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
                PreferencesUtil.getSharePreferences(requireContext())
            val edit = sharedPreferences.edit()
            edit.putBoolean("init", true)
            edit.apply()
            dataBinding.viewLoad.visibility = View.GONE
            dataBinding.viewDone.visibility = View.VISIBLE
            dataBinding.tvLoadCount.text = "加载已完成，共加载${viewModel.count}条记录"
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