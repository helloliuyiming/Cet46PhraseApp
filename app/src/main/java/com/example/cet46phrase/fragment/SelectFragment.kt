package com.example.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentSelectBinding
import com.example.cet46phrase.viewmodel.FragmentSelectViewModel
import com.google.gson.Gson

class SelectFragment : Fragment() {

    lateinit var viewModel: FragmentSelectViewModel
    lateinit var dataBinding: FragmentSelectBinding
    lateinit var adapter: RecyclerView.Adapter<PhraseUnitViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentSelectViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_select, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        onSubscribe()
        viewModel.load()
    }

    private fun initView() {
        dataBinding.rvUnit.layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<PhraseUnitViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PhraseUnitViewHolder {
                val inflate = layoutInflater.inflate(R.layout.item_phrase_unit, parent, false)
                return PhraseUnitViewHolder(inflate, 1)
            }

            override fun onBindViewHolder(holder: PhraseUnitViewHolder, position: Int) {
                if (viewModel.unitListLiveData.value == null) {
                    return
                }
                var mPosition = position
                var itemType = ""
                var itemUnit = ""
                viewModel.unitListLiveData.value!!.forEach {
                    try {
                        if (mPosition < it.value.size) {
                            itemUnit = it.value[mPosition]
                            itemType = it.key
                        } else {
                            mPosition -= it.value.size
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                holder.type?.text = itemType.replace("_phrase", "")
                holder.unit?.text = itemUnit
                holder.itemView.setOnClickListener {
                    val gson = Gson()
                    val followsUnit: MutableMap<String, MutableList<String>> = mutableMapOf()
                    val preferences =
                        requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
                    val edit = preferences.edit()
                    val list = mutableListOf<String>()
                    list.add(itemUnit)
                    followsUnit[itemType] = list
                    edit.putString("follows", gson.toJson(followsUnit))
                    edit.apply()
                    Toast.makeText(context, "切换到$itemType $itemUnit", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }

            override fun getItemCount(): Int {
                if (viewModel.unitListLiveData.value == null) {
                    return 0
                }
                var count = 0
                viewModel.unitListLiveData.value!!.forEach {
                    count += it.value.size
                }
                return count
            }

        }
        dataBinding.rvUnit.adapter = adapter
    }

    private fun initListener() {

    }

    private fun onSubscribe() {
        viewModel.unitListLiveData.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
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