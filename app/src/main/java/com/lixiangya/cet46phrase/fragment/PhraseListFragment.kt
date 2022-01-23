package com.lixiangya.cet46phrase.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentPhraseListBinding
import com.lixiangya.cet46phrase.databinding.ItemPhraseListBinding
import com.lixiangya.cet46phrase.util.PreferencesUtil
import com.lixiangya.cet46phrase.util.SpacesItemDecoration
import com.lixiangya.cet46phrase.viewmodel.FragmentLearnViewModel

class PhraseListFragment : Fragment() {

    lateinit var viewModel: FragmentLearnViewModel
    lateinit var dataBinding: FragmentPhraseListBinding
    lateinit var adapter: RecyclerView.Adapter<ItemPhraseListViewHolder>
    private var lastOffset = 0
    private var lastPosition = 0

    private val statusSpinnerItem:MutableList<String> = mutableListOf()
    private val unitSpinnerItem:MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLearnViewModel::class.java)
        setHasOptionsMenu(true)

        statusSpinnerItem.addAll(arrayListOf("全部","学过","未学"))
        unitSpinnerItem.add("全部")

        val sharePreferences = PreferencesUtil.getSharePreferences(requireContext())
        val json = sharePreferences.getString("follows", "")
        val gson = Gson()
        val jsonObject = gson.fromJson<JsonObject>(json, JsonObject::class.java)
        jsonObject.get("verb_phrase")?.apply {
            unitSpinnerItem.add("动组-全部")
            this.asJsonArray.toMutableList().forEach {
                unitSpinnerItem.add("动组-"+it.asString)
            }
        }

        jsonObject.get("prep_phrase")?.apply {
            unitSpinnerItem.add("介组-全部")
            this.asJsonArray.toMutableList().forEach {
                unitSpinnerItem.add("介组-"+it.asString)
            }
        }

        jsonObject.get("other_phrase")?.apply {
            unitSpinnerItem.add("其他-全部")
            this.asJsonArray.toMutableList().forEach {
                unitSpinnerItem.add("其他-"+it.asString)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_phrase_list, container, false)
        return dataBinding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initToolbar()
        initListener()
        onSubscribe()
        viewModel.load()
    }

    override fun onStart() {
        super.onStart()
        if (dataBinding.rvPhrase.layoutManager != null && lastPosition >= 0) {
            val linearLayoutManager = dataBinding.rvPhrase.layoutManager as LinearLayoutManager
            linearLayoutManager.scrollToPositionWithOffset(lastPosition, lastOffset)
        }
    }

    override fun onStop() {
        super.onStop()
        if (dataBinding.rvPhrase.layoutManager != null) {
            val linearLayoutManager = dataBinding.rvPhrase.layoutManager as LinearLayoutManager
            val topView = linearLayoutManager.getChildAt(0)
            if (topView != null) {
                lastOffset = topView.top
                lastPosition = linearLayoutManager.getPosition(topView)
            }
        }
        Log.i("main", "onStop():lastPosition:${lastPosition}; lastOffset:${lastOffset}")
    }

    private fun initView() {



        dataBinding.spinnerUnit.setItems(unitSpinnerItem)
        dataBinding.spinnerStatus.setItems(statusSpinnerItem)

        dataBinding.spinnerUnit.setOnItemSelectedListener { view, position, id, item ->
            reload()
        }

        dataBinding.spinnerStatus.setOnItemSelectedListener { view, position, id, item ->
            reload()
        }


        dataBinding.rvPhrase.layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<ItemPhraseListViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ItemPhraseListViewHolder {
                val dataBinding = DataBindingUtil.inflate<ItemPhraseListBinding>(
                    layoutInflater,
                    R.layout.item_phrase_list,
                    parent,
                    false
                )
                return ItemPhraseListViewHolder(dataBinding)
            }

            override fun onBindViewHolder(holder: ItemPhraseListViewHolder, position: Int) {
                val dataBinding = holder.dataBinding
                val phrase = viewModel.phraseListLiveData.value!![position]
                dataBinding.tvPhrase.text = phrase.phrase
                val stringBuild = StringBuilder()
                phrase.explains.forEach {
                    stringBuild.append(it.explain)
                    stringBuild.append("\n")
                }
                stringBuild.deleteCharAt(stringBuild.length - 1)
                dataBinding.tvExplains.text = stringBuild
                dataBinding.root.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putSerializable("phrase", phrase)
                    findNavController().navigate(
                        R.id.action_phraseListFragment_to_learnFragment,
                        bundle
                    )
                }
            }

            override fun getItemCount(): Int {
                return if (viewModel.phraseListLiveData.value == null) 0 else viewModel.phraseListLiveData.value!!.size
            }
        }
        dataBinding.rvPhrase.addItemDecoration(SpacesItemDecoration(10))
        dataBinding.rvPhrase.adapter = adapter
    }

    private fun initToolbar() {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "词组列表"
        actionBar?.setDisplayShowTitleEnabled(true)

    }

    private fun initListener() {

    }

    private fun reload(){
        val unitSelectedIndex = dataBinding.spinnerUnit.selectedIndex
        val statusSelectedIndex = dataBinding.spinnerStatus.selectedIndex
        val unitStr = unitSpinnerItem[unitSelectedIndex]
        val statusStr = statusSpinnerItem[statusSelectedIndex]
        val status = when (statusStr) {
            "全部" -> {
                0
            }
            "未学" -> {
                1
            }
            else -> {
                2
            }
        }
        var type:String?
        var unit:String?
        if (unitStr == "全部") {
            type=null
            unit=null
        }else{
            val split = unitStr.split("-")
            type = when (split[0]) {
                "动组"->{
                    "verb_phrase"
                }
                "介组"->{
                    "prep_phrase"
                }
                else->{
                    "other_phrase"
                }
            }
            if (split[1] == "全部") {
                unit = null
            }else{
                unit = split[1]
            }
        }

        viewModel.reload(status = status, type = type, unit = unit)
    }

    private fun onSubscribe() {
        Log.d("main", "onSubscribe() called:${this}")
        viewModel.phraseListLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            Log.i("main", "adapter.notifyDataSetChanged()")
            adapter.notifyDataSetChanged()
        }
    }


    inner class ItemPhraseListViewHolder(val dataBinding: ItemPhraseListBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

}