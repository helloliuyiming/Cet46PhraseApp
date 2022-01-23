package com.lixiangya.cet46phrase.fragment

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentNoteListBinding
import com.lixiangya.cet46phrase.databinding.ItemNoteBinding
import com.lixiangya.cet46phrase.entity.Note
import com.lixiangya.cet46phrase.util.SpacesItemDecoration
import com.google.android.gms.ads.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm
import io.realm.Sort


class NoteListFragment : Fragment() {

    lateinit var dataBinding:FragmentNoteListBinding
    private lateinit var noteAdapter:RecyclerView.Adapter<NoteViewHolder>
    private val noteData:MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_note_list, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()

        loadNote()

        dataBinding.rvNote.layoutManager = LinearLayoutManager(context)
        noteAdapter = object : RecyclerView.Adapter<NoteViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
                val dataBinding:ItemNoteBinding = DataBindingUtil.inflate<ItemNoteBinding>(layoutInflater,R.layout.item_note,null,false)
                return NoteViewHolder(dataBinding)
            }

            override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
                val note = noteData[position]
                val dataBinding = holder.dataBinding
                dataBinding.phrase.text = note.phrase
                dataBinding.noteOutline.text = note.content
                dataBinding.container.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("phraseWord", note.phrase)
                    findNavController().navigate(R.id.learnFragment, bundle)
                }
            }

            override fun getItemCount(): Int {
                return noteData.size
            }

        }
        dataBinding.rvNote.addItemDecoration(SpacesItemDecoration(0,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, requireContext().resources.displayMetrics)
                .toInt()
        ))

        dataBinding.rvNote.adapter = noteAdapter
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar!!
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadNote(){
        Single.create<MutableList<Note>> {
            val realm = Realm.getDefaultInstance()
            val findAll = realm.where(Note::class.java)
                .sort("createdTime", Sort.DESCENDING)
                .findAll()
            val copyFromRealm = realm.copyFromRealm(findAll)
            realm.close()
            it.onSuccess(copyFromRealm)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                noteData.clear()
                noteData.addAll(it)
                noteAdapter.notifyDataSetChanged()
            }, {
                Log.e("main", "loadNote() ERROR:${it.message}")
                Toast.makeText(context,"ERROR:${it.message}",Toast.LENGTH_SHORT).show()
                it.printStackTrace()
            })
    }

    inner class NoteViewHolder(val dataBinding:ItemNoteBinding):RecyclerView.ViewHolder(dataBinding.root)
}
