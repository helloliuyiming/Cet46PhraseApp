package com.example.cet46phrase.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cet46phrase.R
import com.example.cet46phrase.databinding.FragmentLearnBinding
import com.example.cet46phrase.databinding.ItemPhraseExplainBinding
import com.example.cet46phrase.databinding.ItemPhraseNoteBinding
import com.example.cet46phrase.viewmodel.FragmentLearnViewModel

class LearnFragment : Fragment() {

    lateinit var viewModel: FragmentLearnViewModel
    lateinit var dataBinding: FragmentLearnBinding
    lateinit var noteAdapter: RecyclerView.Adapter<ItemNoteViewHolder>
    lateinit var explainAdapter: RecyclerView.Adapter<ItemExplainViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLearnViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_learn, container, false)
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
        dataBinding.rvExplain.layoutManager = LinearLayoutManager(context)
        dataBinding.rvNote.layoutManager = LinearLayoutManager(context)
        noteAdapter = object : RecyclerView.Adapter<ItemNoteViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemNoteViewHolder {
                val dataBinding = DataBindingUtil.inflate<ItemPhraseNoteBinding>(
                    layoutInflater,
                    R.layout.item_phrase_note,
                    parent,
                    false
                )
                return ItemNoteViewHolder(dataBinding)
            }

            override fun onBindViewHolder(holder: ItemNoteViewHolder, position: Int) {

            }

            override fun getItemCount(): Int {
                return if (viewModel.notesLiveData.value == null) 0 else viewModel.notesLiveData.value!!.size
            }

        }
        explainAdapter = object : RecyclerView.Adapter<ItemExplainViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ItemExplainViewHolder {
                val dataBinding = DataBindingUtil.inflate<ItemPhraseExplainBinding>(
                    layoutInflater,
                    R.layout.item_phrase_explain,
                    parent,
                    false
                )
                return ItemExplainViewHolder(dataBinding)
            }

            override fun onBindViewHolder(holder: ItemExplainViewHolder, position: Int) {
                if (viewModel.phraseLiveData.value == null) {
                    return
                }
                val explain = viewModel.phraseLiveData.value!!.explains[position]
                val dataBinding = holder.dataBinding
                if (explain.examples.isNullOrEmpty()) {
                    dataBinding.tvExample.text = ""
                } else {
                    dataBinding.tvExample.text = explain.examples[0]
                }
                dataBinding.tvNumber.text = (position+1).toString()
                dataBinding.tvExplain.text = explain.explain
            }

            override fun getItemCount(): Int {
                if (viewModel.phraseLiveData.value == null) {
                    return 0
                }
                return viewModel.phraseLiveData.value!!.explains.size
            }

        }

        dataBinding.rvNote.adapter = noteAdapter
        dataBinding.rvExplain.adapter = explainAdapter
    }

    private fun initListener() {
        dataBinding.actionOk.setOnClickListener {
            if (viewModel.phraseLiveData.value == null) return@setOnClickListener
            viewModel.phraseLiveData.value!!.score = 3
            viewModel.next()
        }
        dataBinding.actionSkip.setOnClickListener {
            if (viewModel.phraseLiveData.value == null) return@setOnClickListener
            viewModel.phraseLiveData.value!!.last = true
            viewModel.pass()
        }
        dataBinding.actionUnsure.setOnClickListener {
            if (viewModel.phraseLiveData.value == null) return@setOnClickListener
            viewModel.phraseLiveData.value!!.score = viewModel.phraseLiveData.value!!.score + 1
            viewModel.next()
        }

        dataBinding.btnTestDontKnow.setOnClickListener {

        }
        dataBinding.btnTestKnow.setOnClickListener {

        }
        dataBinding.btnWriteDont.setOnClickListener {

        }
        dataBinding.btnWriteDo.setOnClickListener {

        }
    }

    private fun onSubscribe() {
        viewModel.phraseLiveData.observe(viewLifecycleOwner) {
            Log.i("main"," viewModel.phraseLiveData.observe():phrase=${it.phrase}")
            if (it == null && viewModel.done) {
                Toast.makeText(context, "已全部学习完，自动退出", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return@observe
            }
            if (it == null) {
                Toast.makeText(context, "正在加载中，请稍等...", Toast.LENGTH_LONG).show()
                return@observe
            }
            if (it.last) {
                viewModel.viewTypeLiveData.value = FragmentLearnViewModel.VIEW_TYPE_WRITE
            } else if (it.score < 3) {
                viewModel.viewTypeLiveData.value = FragmentLearnViewModel.VIEW_TYPE_SHOW
            } else {
                viewModel.viewTypeLiveData.value = FragmentLearnViewModel.VIEW_TYPE_TEST
            }
        }

        viewModel.viewTypeLiveData.observe(viewLifecycleOwner) {
            if (viewModel.phraseLiveData.value == null) return@observe
            val phrase = viewModel.phraseLiveData.value!!
            when (it) {
                FragmentLearnViewModel.VIEW_TYPE_SHOW -> {
                    dataBinding.viewShow.visibility = View.VISIBLE
                    dataBinding.viewTest.visibility = View.GONE
                    dataBinding.viewWrite.visibility = View.GONE
                    dataBinding.tvPhrase.text = phrase.phrase
                    if (phrase.notice == null) {
                        dataBinding.tvNotice.visibility = View.GONE
                    } else {
                        dataBinding.tvNotice.text = phrase.notice
                    }
                    if (phrase.synonym == null) {
                        dataBinding.tvSynonym.visibility = View.GONE
                    } else {
                        dataBinding.tvSynonym.text = phrase.synonym
                    }
                    explainAdapter.notifyDataSetChanged()
                }
                FragmentLearnViewModel.VIEW_TYPE_TEST -> {
                    dataBinding.viewShow.visibility = View.GONE
                    dataBinding.viewTest.visibility = View.VISIBLE
                    dataBinding.viewWrite.visibility = View.GONE
                    dataBinding.tvTestPhrase.text = phrase.phrase
                }
                FragmentLearnViewModel.VIEW_TYPE_WRITE -> {
                    dataBinding.viewShow.visibility = View.GONE
                    dataBinding.viewTest.visibility = View.GONE
                    dataBinding.viewWrite.visibility = View.VISIBLE
                    dataBinding.tvWriteExample.text = "How are you?"
                }
                else -> {
                    Toast.makeText(context, "ViewType Value$it wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class ItemExplainViewHolder(val dataBinding: ItemPhraseExplainBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    inner class ItemNoteViewHolder(val dataBinding: ItemPhraseNoteBinding) :
        RecyclerView.ViewHolder(dataBinding.root)
}