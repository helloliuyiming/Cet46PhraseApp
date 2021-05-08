package com.example.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
        setHasOptionsMenu(true)
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
        checkInit()
        initView()
        initToolbar()
        initListener()
        onSubscribe()
        viewModel.load()
        checkFollow()
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        checkInit()
        checkFollow()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_main,menu)
    }
    private fun initView() {
        dataBinding.rvBoard.layoutManager = LinearLayoutManager(context)
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
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPosition < viewModel.verbUnitList!!.size) {
                        itemType = "动词词组"
                    }else{
                        mPosition-=viewModel.verbUnitList!!.size
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()&&itemType==null) {
                    if (mPosition < viewModel.prepUnitList!!.size) {
                        itemType = "介词词组"
                    }else{
                        mPosition -=viewModel.prepUnitList!!.size
                    }
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()&&itemType==null) {
                    if (mPosition < viewModel.otherUnitList!!.size) {
                        itemType = "其他词组"
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
                var itemUnit:String? = null
                holder.itemView.isClickable = false
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    mPosition--
                    if (mPosition < viewModel.verbUnitList!!.size) {
                        itemUnit =   viewModel.verbUnitList!![mPosition]
                    }else{
                        mPosition -=viewModel.verbUnitList!!.size
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()&&itemUnit==null) {
                    mPosition--
                    if (mPosition < viewModel.prepUnitList!!.size) {
                        itemUnit =   viewModel.prepUnitList!![mPosition]
                    }else{
                        mPosition -=viewModel.prepUnitList!!.size
                    }
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()&&itemUnit==null) {
                    mPosition--
                    if (mPosition < viewModel.otherUnitList!!.size) {
                        itemUnit =   viewModel.otherUnitList!![mPosition]
                    }else{
                        mPosition -=viewModel.otherUnitList!!.size
                    }
                }

                holder.unit?.text = itemUnit
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
                    mPositon = mPositon-viewModel.otherUnitList!!.size-1
                }

                return 2
            }
        }
        dataBinding.rvBoard.adapter = adapter

    }

    private fun initToolbar(){
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val mDrawerToggle = ActionBarDrawerToggle(activity,dataBinding.drawerLayout,dataBinding.toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        dataBinding.drawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
    }
    private fun initListener() {
        dataBinding.btnEdit.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_selectFragment) }
        dataBinding.btnAction.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_learnFragment) }
    }

    private fun onSubscribe() {
    }

    private fun checkInit() {
        Log.d("main", "check() called")
        val sharedPreferences =
            requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        val init = sharedPreferences.getBoolean("init", false)
        if (!init) {
            findNavController().navigate(R.id.action_mainFragment_to_loadDataFragment)
        }
    }
    private fun checkFollow(){
        if ((viewModel.verbUnitList == null || viewModel.verbUnitList!!.isEmpty()) && (viewModel.prepUnitList == null || viewModel.prepUnitList!!.isEmpty()) && (viewModel.otherUnitList == null || viewModel.otherUnitList!!.isEmpty())) {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                findNavController().navigate(R.id.action_mainFragment_to_selectFragment)
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