package com.lixiangya.cet46phrase.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentLearnBinding
import com.lixiangya.cet46phrase.databinding.ItemPhraseExplainBinding
import com.lixiangya.cet46phrase.databinding.ItemPhraseNoteBinding
import com.lixiangya.cet46phrase.entity.Note
import com.lixiangya.cet46phrase.entity.Phrase
import com.lixiangya.cet46phrase.util.SpacesItemDecoration
import com.lixiangya.cet46phrase.viewmodel.FragmentLearnViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random

class LearnFragment : Fragment(), View.OnClickListener {

    lateinit var viewModel: FragmentLearnViewModel
    lateinit var dataBinding: FragmentLearnBinding
    lateinit var noteAdapter: RecyclerView.Adapter<ItemNoteViewHolder>
    lateinit var explainAdapter: RecyclerView.Adapter<ItemExplainViewHolder>
    lateinit var actionBar: ActionBar
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    val random = Random

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentLearnViewModel::class.java)
        setHasOptionsMenu(true)
        if (arguments != null) {
            viewModel.reviewModeSignal = arguments!!.getBoolean("isReviewMode", false)
            if (arguments!!.getSerializable("phrase") != null) {
                viewModel.phraseLiveData.value = arguments!!.getSerializable("phrase") as Phrase
                viewModel.deepLinkSignal = true
            }else if (arguments!!.getString("phraseWord") != null) {
                viewModel.loadPhrase(arguments!!.getString("phraseWord")!!)
                viewModel.deepLinkSignal = true
            }
        }
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
        initToolbar()
        initListener()
        onSubscribe()
        viewModel.load()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        bottomSheetBehavior = BottomSheetBehavior.from(dataBinding.viewBottomSheet)
        bottomSheetBehavior.isDraggable = false
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
                if (viewModel.notesLiveData.value == null) return
                val note = viewModel.notesLiveData.value!![position]
                val dataBinding = holder.dataBinding
                dataBinding.tvNoteContent.text = note.content
                dataBinding.btnNoteDelete.setOnClickListener {
                    viewModel.deleteNote(note)
                }
                dataBinding.btnNoteEdit.setOnClickListener {
                    viewModel.editNoteLiveData.value = note
                }
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
                    dataBinding.tvExampleEn.text = ""
                    dataBinding.tvExampleCn.text = ""
                } else {
                    dataBinding.tvExampleEn.text = explain.examples[0].en
                    dataBinding.tvExampleCn.text = explain.examples[0].cn
                }
                dataBinding.tvNumber.text = (position + 1).toString()
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
        dataBinding.rvNote.addItemDecoration(
            SpacesItemDecoration(
                SpacesItemDecoration.vertical,
                10
            )
        )

    }

    private fun initToolbar() {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        actionBar = activity.supportActionBar!!
        if (viewModel.deepLinkSignal) {
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = viewModel.phraseLiveData.value?.phrase
        }
    }

    private fun initListener() {
        dataBinding.actionOk.setOnClickListener(this)
        dataBinding.actionSkip.setOnClickListener(this)
        dataBinding.actionUnsure.setOnClickListener(this)
        dataBinding.btnTestDontKnow.setOnClickListener(this)
        dataBinding.btnTestKnow.setOnClickListener(this)
        dataBinding.btnWriteDont.setOnClickListener(this)
        dataBinding.btnWriteDo.setOnClickListener(this)
        dataBinding.floatingActionButton.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            viewModel.editNoteLiveData.value = null
            dataBinding.floatingActionButton.hide()
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (!dataBinding.floatingActionButton.isShown) {
                        dataBinding.floatingActionButton.show()
                        dataBinding.etNote.clearFocus()
                        hideKeyboard(requireActivity() as AppCompatActivity)
                    }
                } else {
                    if (dataBinding.floatingActionButton.isShown) {
                        dataBinding.floatingActionButton.hide()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })

        dataBinding.etNote.addTextChangedListener {
            var count = 0
            if (it != null) {
                count = it.toString().length
            }
            dataBinding.tvNoteCount.text = "已输入：$count / 800"
        }

        dataBinding.btnCommitBtn.setOnClickListener {
            if (viewModel.editNoteLiveData.value == null && dataBinding.etNote.text == null) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                return@setOnClickListener
            }
            if (viewModel.editNoteLiveData.value == null) {
                val note = Note()
                note.phrase = viewModel.phraseLiveData.value!!.phrase
                note.content = dataBinding.etNote.text.toString()
                viewModel.saveNote(note)
            } else {
                viewModel.editNoteLiveData.value!!.content = dataBinding.etNote.text.toString()
                viewModel.updateNote(viewModel.editNoteLiveData.value!!)
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        dataBinding.etNote.setOnDragListener { v, event ->
            Log.d("main", " dataBinding.etNote.setOnDragListener() called")
            true
        }
        dataBinding.btnNoteClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        dataBinding.btnTestPhrase.setOnClickListener {
            dataBinding.tvTestAnswer.visibility = View.VISIBLE
        }
        dataBinding.btnWriteExample.setOnClickListener {
            dataBinding.tvWriteAnswer.visibility = View.VISIBLE
            dataBinding.tvWritePhrase.visibility = View.VISIBLE
        }
    }

    private fun onSubscribe() {

        viewModel.phraseLiveData.observe(viewLifecycleOwner) {
            if (it == null && viewModel.done) {
                Toast.makeText(context, "已全部学习完，自动退出", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return@observe
            }
            if (it == null) {
                Toast.makeText(context, "正在加载中，请稍等...", Toast.LENGTH_LONG).show()
                return@observe
            }

            viewModel.loadNote(it.phrase)
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
                    dataBinding.blockAction.visibility = View.VISIBLE
                    dataBinding.tvPhrase.text = phrase.phrase
                    if (!dataBinding.floatingActionButton.isShown) {
                        dataBinding.floatingActionButton.show()
                    }
                    showFab(fab = dataBinding.floatingActionButton, true)
                    if (viewModel.deepLinkSignal) {
                        dataBinding.blockAction.visibility = View.GONE
                    }
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
                    dataBinding.floatingActionButton.hide()
                    dataBinding.viewShow.visibility = View.GONE
                    dataBinding.viewTest.visibility = View.VISIBLE
                    dataBinding.viewWrite.visibility = View.GONE
                    dataBinding.blockAction.visibility = View.GONE
                    dataBinding.tvTestAnswer.visibility = View.GONE
                    dataBinding.btnTestPhrase.text = phrase.phrase
                    val stringBuilder = StringBuilder()
                    phrase.explains.forEach {
                        stringBuilder.append(it.explain)
                        stringBuilder.append("\n")
                    }
                    stringBuilder.trim()
                    dataBinding.tvTestAnswer.text = stringBuilder
                }
                FragmentLearnViewModel.VIEW_TYPE_WRITE -> {
                    dataBinding.floatingActionButton.hide()
                    dataBinding.viewShow.visibility = View.GONE
                    dataBinding.viewTest.visibility = View.GONE
                    dataBinding.viewWrite.visibility = View.VISIBLE
                    dataBinding.blockAction.visibility = View.GONE
                    dataBinding.tvWritePhrase.visibility = View.GONE
                    dataBinding.tvWriteAnswer.visibility = View.GONE
                    dataBinding.tvWritePhrase.text = phrase.phrase

                    val explainPosition = random.nextInt(phrase.explains.size)
                    if (phrase.explains[explainPosition].examples.isNotEmpty()) {
                        dataBinding.btnWriteExample.text =
                            phrase.explains[explainPosition].examples[0].en
                        dataBinding.tvWriteAnswer.text =
                            phrase.explains[explainPosition].examples[0].cn
                    }
                }
                else -> {
                    Toast.makeText(context, "ViewType Value$it wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.completedLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (viewModel.deepLinkSignal) {
                initToolbar()
                return@observe
            }
            actionBar.title = "$it / ${viewModel.count}"
        }

        viewModel.editNoteLiveData.observe(viewLifecycleOwner) {
            if (it == null) {
                dataBinding.etNote.text = null
                return@observe
            }
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            dataBinding.etNote.setText(it.content)
        }

        viewModel.notesLiveData.observe(viewLifecycleOwner) {
            if (it == null || it.isEmpty()) {
                dataBinding.blockNote.visibility = View.GONE
                return@observe
            }
            dataBinding.blockNote.visibility = View.VISIBLE
            noteAdapter.notifyDataSetChanged()
        }

    }

    fun hideKeyboard(activity: Activity) {
        val inputManager = activity
            .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // check if no view has focus:
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    inner class ItemExplainViewHolder(val dataBinding: ItemPhraseExplainBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    inner class ItemNoteViewHolder(val dataBinding: ItemPhraseNoteBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    override fun onClick(v: View?) {
        if (viewModel.phraseLiveData.value == null) return
        if (v == null) return
        when (v.id) {
            dataBinding.actionOk.id -> {
                viewModel.phraseLiveData.value!!.score = 3
                viewModel.phraseLiveData.value!!.last = false
                viewModel.next()
            }
            dataBinding.actionUnsure.id -> {
                viewModel.phraseLiveData.value!!.score = viewModel.phraseLiveData.value!!.score + 1
                viewModel.phraseLiveData.value!!.last = false
                viewModel.next()
            }
            dataBinding.actionSkip.id -> {
                viewModel.phraseLiveData.value!!.score = 3
                viewModel.phraseLiveData.value!!.last = true
                viewModel.next()
            }
            dataBinding.btnTestDontKnow.id -> {
                viewModel.phraseLiveData.value!!.score = viewModel.phraseLiveData.value!!.score - 2
                viewModel.phraseLiveData.value!!.last = false
                viewModel.viewTypeLiveData.value = FragmentLearnViewModel.VIEW_TYPE_SHOW
            }
            dataBinding.btnTestKnow.id -> {
                viewModel.phraseLiveData.value!!.score = 3
                viewModel.phraseLiveData.value!!.last = true
                viewModel.next()
            }
            dataBinding.btnWriteDont.id -> {
                viewModel.phraseLiveData.value!!.score = 1
                viewModel.phraseLiveData.value!!.last = false
                viewModel.viewTypeLiveData.value = FragmentLearnViewModel.VIEW_TYPE_SHOW
            }
            dataBinding.btnWriteDo.id -> {
                viewModel.pass()
            }

        }
    }

    private fun showFab(fab: FloatingActionButton, isVisible: Boolean) {
        val layoutParams: ViewGroup.LayoutParams = fab.layoutParams
        if (layoutParams is CoordinatorLayout.LayoutParams) {
            val behavior = layoutParams.behavior
            if (behavior is HideBottomViewOnScrollBehavior) {
                if (isVisible) {
                    behavior.slideUp(fab)
                } else {
                    behavior.slideDown(fab)
                }
            }
        }
    }
}