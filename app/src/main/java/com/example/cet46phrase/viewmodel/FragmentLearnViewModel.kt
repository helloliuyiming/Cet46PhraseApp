package com.example.cet46phrase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.cet46phrase.entity.Note
import com.example.cet46phrase.entity.Phrase
import java.util.*

class FragmentLearnViewModel(application: Application) : AndroidViewModel(application) {
    val viewTypeLiveData: MutableLiveData<Int> = MutableLiveData()
    val phraseLiveData: MutableLiveData<Phrase> = MutableLiveData()
    val notesLiveData: MutableLiveData<MutableList<Note>> = MutableLiveData()
    var done = false

    private val phraseQueue: Queue<Phrase> = LinkedList<Phrase>()
    private val activePhraseQueue: Queue<Phrase> = LinkedList<Phrase>()

    fun load() {

    }

    fun next() {

    }

    fun skip() {

    }

    private fun loadMore() {

    }

    companion object {
        const val VIEW_TYPE_SHOW = 1
        const val VIEW_TYPE_TEST = 2
        const val VIEW_TYPE_WRITE = 3
    }

}