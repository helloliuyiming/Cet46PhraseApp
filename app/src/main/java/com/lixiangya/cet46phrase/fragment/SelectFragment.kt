package com.lixiangya.cet46phrase.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentSelectBinding
import com.lixiangya.cet46phrase.viewmodel.FragmentSelectViewModel
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
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        dataBinding.tvFollowInfo.text = "?????????${viewModel.followCount}??????"
        dataBinding.rvUnit.layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<PhraseUnitViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PhraseUnitViewHolder {
                if (viewType == 1) {
                    val inflate =
                        layoutInflater.inflate(R.layout.item_phrase_category_title, parent, false)
                    return PhraseUnitViewHolder(inflate, 1)
                }
                val inflate = layoutInflater.inflate(R.layout.item_phrase_unit, parent, false)
                return PhraseUnitViewHolder(inflate, 2)
            }

            override fun onBindViewHolder(holder: PhraseUnitViewHolder, position: Int) {
                val itemMap:MutableMap<String,String> = mutableMapOf<String,String>()
                var mPosition = position
                var itemType: String? = null
                var itemUnit: String = ""
                var type:String = ""

                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPosition <= viewModel.verbUnitList!!.size) {
                        itemType = "????????????"
                        type = "verb_phrase"
                        if (getItemViewType(position) == 2) {
                            itemUnit = viewModel.verbUnitList!![mPosition - 1]
                        }
                    } else {
                        mPosition -= (viewModel.verbUnitList!!.size + 1)
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty() && itemType == null) {
                    if (mPosition <= viewModel.prepUnitList!!.size) {
                        itemType = "????????????"
                        type = "prep_phrase"
                        if (getItemViewType(position) == 2) {
                            try {
                                itemUnit = viewModel.prepUnitList!![mPosition - 1]
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }

                    } else {
                        mPosition -= (viewModel.prepUnitList!!.size + 1)
                    }
                }
                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty() && itemType == null) {
                    if (mPosition <= viewModel.otherUnitList!!.size) {
                        itemType = "????????????"
                        type="other_phrase"
                        if (getItemViewType(position) == 2) {
                            itemUnit = viewModel.otherUnitList!![mPosition - 1]
                        }
                    }
                }

                if (getItemViewType(position) == 1) {
                    if (itemType == null) {
                        itemType = "??????"
                    }
                    holder.title?.text = itemType
                    return
                }
                if (viewModel.actionModeLiveData.value == FragmentSelectViewModel.ACTION_MODE_SELECT_SINGLE) {
                    holder.checkBox?.visibility = View.GONE
                } else {
                    holder.checkBox?.visibility = View.VISIBLE
                }
                holder.unit?.text = itemUnit

                itemMap["unit"] = itemUnit
                itemMap["type"] = type

                holder.itemView.setOnClickListener {
                    if (viewModel.actionModeLiveData.value == FragmentSelectViewModel.ACTION_MODE_SELECT_MULTIPLE) {
                        if (holder.checkBox?.isChecked!!) {
                            holder.checkBox?.isChecked = false

                            viewModel.followSet.remove(itemMap)

                        } else {
                            holder.checkBox?.isChecked = true

                            viewModel.followSet.add(itemMap)
                        }
                        dataBinding.tvFollowInfo.text = "?????????${viewModel.followSet.size}??????"
                        return@setOnClickListener
                    }

                    viewModel.followSet.clear()
                    viewModel.followSet.add(itemMap)
                    viewModel.rebuild(false)
                }
                holder.itemView.setOnLongClickListener {
                    viewModel.actionModeLiveData.value =
                        FragmentSelectViewModel.ACTION_MODE_SELECT_MULTIPLE
                    true
                }
            }

            override fun getItemCount(): Int {
                var count = 0
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    count += viewModel.verbUnitList!!.size + 1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    count += viewModel.prepUnitList!!.size + 1
                }
                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()) {
                    count += viewModel.otherUnitList!!.size + 1
                }
                return count
            }

            override fun getItemViewType(position: Int): Int {
                var mPositon = position
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPositon == 0) {
                        return 1
                    }
                    mPositon = mPositon - viewModel.verbUnitList!!.size - 1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    if (mPositon == 0) {
                        return 1
                    }
                    mPositon = mPositon - viewModel.prepUnitList!!.size - 1
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

    private fun initToolbar() {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "????????????????????????"
    }

    private fun initListener() {
        dataBinding.btnSubmit.setOnClickListener {
            Log.i("main",Gson().toJson(viewModel.followSet))
            val dialog = AlertDialog.Builder(requireContext())
                .setMessage("?????????????????????????????????")
                .setNegativeButton(
                    "????????????"
                ) { p0, p1 ->
                    viewModel.rebuild(true)
                }
                .setPositiveButton("????????????"
                ) { p0, p1 -> viewModel.rebuild(false) }.create()

            dialog.show()

        }
    }

    private fun onSubscribe() {
        viewModel.actionModeLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                FragmentSelectViewModel.ACTION_MODE_SELECT_SINGLE -> {
                    dataBinding.blockSelect.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
                FragmentSelectViewModel.ACTION_MODE_SELECT_MULTIPLE -> {
                    dataBinding.blockSelect.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.signalLiveData.observe(viewLifecycleOwner){
            if (it == null) {
                return@observe
            }
            if (it == FragmentSelectViewModel.SIGNAL_SAVE_DONE) {
                findNavController().popBackStack()
            }
        }
    }


    inner class PhraseUnitViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        var type: TextView? = null
        var unit: TextView? = null
        var progress: ProgressBar? = null
        var flag: ImageView? = null
        var title: TextView? = null
        var checkBox: CheckBox? = null

        init {
            if (viewType == 2) {
                type = itemView.findViewById(R.id.tv_type)
                unit = itemView.findViewById(R.id.tv_unit)
                progress = itemView.findViewById(R.id.progressBar_unit)
                flag = itemView.findViewById(R.id.iv_unit_flag)
                checkBox = itemView.findViewById(R.id.checkbox)
            } else if (viewType == 1) {
                title = itemView.findViewById(R.id.tv_title)
            }
        }
    }
}