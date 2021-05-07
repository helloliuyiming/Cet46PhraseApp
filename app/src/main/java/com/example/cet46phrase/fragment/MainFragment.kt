package com.example.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentMainBinding
import com.example.cet46phrase.viewmodel.FragmentMainViewModel


class MainFragment : Fragment() {

    lateinit var dataBinding: FragmentMainBinding
    lateinit var viewModel: FragmentMainViewModel
    lateinit var adapter: RecyclerView.Adapter<PhraseUnitViewHolder>

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        check()
        initView()
        initListener()
        onSubscribe()
        viewModel.load()
    }

    private fun initView() {
        dataBinding.rvBoard.layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<PhraseUnitViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PhraseUnitViewHolder {
                val inflate = layoutInflater.inflate(R.layout.item_phrase_unit, parent, false)
                return PhraseUnitViewHolder(inflate, 1)
            }

            override fun onBindViewHolder(holder: PhraseUnitViewHolder, position: Int) {
                if (viewModel.followUnitLiveData.value == null) {
                    return
                }
                var mPosition = position
                var itemType = ""
                var itemUnit = ""
                viewModel.followUnitLiveData.value!!.forEach {
                    if (mPosition < it.value.size) {
                        itemUnit = it.value[mPosition]
                        itemType = it.key
                    } else {
                        mPosition -= it.value.size
                    }
                }

                holder.type?.text = itemType.replace("_phrase", "")
                holder.unit?.text = itemUnit
            }

            override fun getItemCount(): Int {
                if (viewModel.followUnitLiveData.value == null) {
                    return 0
                }
                var count = 0
                viewModel.followUnitLiveData.value!!.forEach {
                    count += it.value.size
                }
                return count
            }

        }
        dataBinding.rvBoard.adapter = adapter

    }

    private fun initListener() {
        dataBinding.btnEdit.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_selectFragment) }
        dataBinding.btnAction.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_learnFragment) }
    }

    private fun onSubscribe() {
        viewModel.followUnitLiveData.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().navigate(R.id.action_mainFragment_to_selectFragment)
                return@observe
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun check() {
        val sharedPreferences =
            requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        val init = sharedPreferences.getBoolean("init", false)
        if (!init) {
            findNavController().navigate(R.id.action_mainFragment_to_loadDataFragment)
        }
    }

    inner class PhraseUnitViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        var type: TextView? = null
        var unit: TextView? = null
        var progress: ProgressBar? = null
        var flag: ImageView? = null

        init {
            if (viewType == 1) {
                type = itemView.findViewById(R.id.tv_type)
                unit = itemView.findViewById(R.id.tv_unit)
                progress = itemView.findViewById(R.id.progressBar_unit)
                flag = itemView.findViewById(R.id.iv_unit_flag)
            }
        }
    }
}