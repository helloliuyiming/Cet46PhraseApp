package com.example.cet46phrase.fragment

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
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentPhraseListBinding
import com.example.cet46phrase.databinding.ItemPhraseListBinding
import com.example.cet46phrase.util.SpacesItemDecoration
import com.example.cet46phrase.viewmodel.FragmentLearnViewModel

class PhraseListFragment : Fragment() {

    lateinit var viewModel: FragmentLearnViewModel
    lateinit var dataBinding: FragmentPhraseListBinding
    lateinit var adapter:RecyclerView.Adapter<ItemPhraseListViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLearnViewModel::class.java)
        setHasOptionsMenu(true)
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

    private fun initView(){
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
                stringBuild.deleteCharAt(stringBuild.length-1)
                dataBinding.tvExplains.text = stringBuild
            }

            override fun getItemCount(): Int {
                return if (viewModel.phraseListLiveData.value==null) 0 else viewModel.phraseListLiveData.value!!.size
            }
        }
        dataBinding.rvPhrase.addItemDecoration(SpacesItemDecoration(10))
        dataBinding.rvPhrase.adapter = adapter
    }

    private fun initToolbar(){
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "词组列表"
        actionBar?.setDisplayShowTitleEnabled(true)

    }
    private fun initListener(){

    }

    private fun onSubscribe() {
        viewModel.phraseListLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe
            Log.i("main","adapter.notifyDataSetChanged()")
            adapter.notifyDataSetChanged()
        }
    }


   inner class ItemPhraseListViewHolder(val dataBinding:ItemPhraseListBinding):RecyclerView.ViewHolder(dataBinding.root)

}