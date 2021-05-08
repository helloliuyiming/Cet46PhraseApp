package com.example.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        setHasOptionsMenu(true)
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
        initToolbar()
        initListener()
        onSubscribe()
        viewModel.load()
        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("main", "onOptionsItemSelected() called with: item = $item")
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return false
        }
        return super.onOptionsItemSelected(item)
    }
    private fun initView() {
        dataBinding.rvUnit.layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<PhraseUnitViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PhraseUnitViewHolder {
                if (viewType == 1) {
                    val inflate = layoutInflater.inflate(R.layout.item_phrase_category_title, parent, false)
                    return PhraseUnitViewHolder(inflate,1)
                }
                val inflate = layoutInflater.inflate(R.layout.item_phrase_unit, parent, false)
                return PhraseUnitViewHolder(inflate, 2)
            }

            override fun onBindViewHolder(holder: PhraseUnitViewHolder, position: Int) {
                var mPosition = position
                var itemType:String? = null
                var itemUnit:String? = null

                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPosition <= viewModel.verbUnitList!!.size) {
                        itemType = "动词词组"
                        if (getItemViewType(position) == 2) {
                            itemUnit = viewModel.verbUnitList!![mPosition-1]
                        }
                    }else{
                        mPosition-=(viewModel.verbUnitList!!.size+1)
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()&&itemType==null) {
                    if (mPosition <= viewModel.prepUnitList!!.size) {
                        itemType = "介词词组"
                        if (getItemViewType(position) == 2) {
                            try {
                                itemUnit = viewModel.prepUnitList!![mPosition-1]
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }

                    }else{
                        mPosition -=(viewModel.prepUnitList!!.size+1)
                    }
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()&&itemType==null) {
                    if (mPosition <= viewModel.otherUnitList!!.size) {
                        itemType = "其他词组"
                        if (getItemViewType(position) == 2) {
                            itemUnit = viewModel.otherUnitList!![mPosition-1]
                        }
                    }
                }

                if (getItemViewType(position) == 1) {
                    if (itemType == null) {
                        itemType = "词组"
                    }
                    holder.title?.text = itemType
                    return
                }
                mPosition = position

//                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
//                    mPosition--
//                    if (mPosition < viewModel.verbUnitList!!.size) {
//                      itemUnit =   viewModel.verbUnitList!![mPosition]
//                    }else{
//                        mPosition -=viewModel.verbUnitList!!.size
//                    }
//                }
//                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()&&itemUnit==null) {
//                    mPosition--
//                    try {
//                        if (mPosition < viewModel.prepUnitList!!.size) {
//                            itemUnit =   viewModel.prepUnitList!![mPosition]
//                        }else{
//                            mPosition -=viewModel.prepUnitList!!.size
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()&&itemUnit==null) {
//                    mPosition--
//                    if (mPosition < viewModel.otherUnitList!!.size) {
//                        itemUnit =   viewModel.otherUnitList!![mPosition]
//                    }else{
//                        mPosition -=viewModel.otherUnitList!!.size
//                    }
//                }

                holder.unit?.text = itemUnit
                holder.itemView.setOnClickListener {
                    var saveType:String? = null
                    if ("动词词组" == itemType) {
                        saveType = "verb_phrase"
                    }else if ("介词词组" == itemType) {
                        saveType = "prep_phrase"
                    }else if ("其他词组"==itemType){
                        saveType = "other_phrase"
                    }
                    val gson = Gson()
                    val followsUnit: MutableMap<String, MutableList<String>> = mutableMapOf()
                    val preferences =
                        requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
                    val edit = preferences.edit()
                    val list = mutableListOf<String>()
                    list.add(itemUnit!!)
                    try {
                        followsUnit[saveType!!] = list
                        edit.putString("follows", gson.toJson(followsUnit))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    edit.apply()
                    Toast.makeText(context, "切换到$itemType $itemUnit", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                holder.itemView.setOnLongClickListener {
                    Toast.makeText(context,"进入多选模式",Toast.LENGTH_SHORT).show()
                    viewModel.actionModeLiveData.value =FragmentSelectViewModel.ACTION_MODE_SELECT_MULTIPLE
                    true
                }
            }

            override fun getItemCount(): Int {
                var count = 0
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    count+=viewModel.verbUnitList!!.size+1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    count+=viewModel.prepUnitList!!.size+1
                }
                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()) {
                    count+=viewModel.otherUnitList!!.size+1
                }
                return count
            }

            override fun getItemViewType(position: Int): Int {
                var mPositon = position
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPositon == 0) {
                        return 1
                    }
                    mPositon = mPositon-viewModel.verbUnitList!!.size-1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    if (mPositon == 0) {
                        return 1
                    }
                    mPositon = mPositon-viewModel.prepUnitList!!.size-1
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()) {
                    if (mPositon == 0) {
                        return 1
                    }
                }

                return 2
            }
        }
        dataBinding.rvUnit.adapter = adapter
    }

    private fun initToolbar(){
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "选择要学习的单元"
    }

    private fun initListener() {
        dataBinding.btnSubmit.setOnClickListener {

        }
    }

    private fun onSubscribe() {
        viewModel.actionModeLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe
            when (it) {
                FragmentSelectViewModel.ACTION_MODE_SELECT_SINGLE->{
                    dataBinding.blockSelect.visibility =View.GONE
                    adapter.notifyDataSetChanged()
                }
                FragmentSelectViewModel.ACTION_MODE_SELECT_MULTIPLE->{
                    dataBinding.blockSelect.visibility =View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }


    inner class PhraseUnitViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        var type: TextView? = null
        var unit: TextView? = null
        var progress: ProgressBar? = null
        var flag: ImageView? = null
        var title:TextView? = null

        init {
            if (viewType == 2) {
                type = itemView.findViewById(R.id.tv_type)
                unit = itemView.findViewById(R.id.tv_unit)
                progress = itemView.findViewById(R.id.progressBar_unit)
                flag = itemView.findViewById(R.id.iv_unit_flag)
            }else if (viewType == 1) {
                title = itemView.findViewById(R.id.tv_title)
            }
        }
    }
}