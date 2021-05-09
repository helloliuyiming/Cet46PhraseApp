package com.example.cet46phrase.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(left: Int, right: Int, bottom: Int, top: Int, orientation: Int) :
    RecyclerView.ItemDecoration() {
    private var left = 0
    private var right: Int = 0
    private var bottom: Int = 0
    private var top: Int = 0
    private var orientation: Int

    init {
        this.left = left
        this.right = right
        this.bottom = bottom
        this.top = top
        this.orientation = orientation
    }


    constructor(space: Int) : this(space, space, space, space, vertical)
    constructor(horizontal: Int, vertical: Int) : this(
        horizontal,
        horizontal,
        vertical,
        vertical,
        vertical
    )

    constructor(horizontal: Int, vertical: Int, orientation: Int) : this(
        horizontal,
        horizontal,
        vertical,
        vertical,
        orientation
    )


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = left
        outRect.right = right
        outRect.bottom = bottom
        // Add top margin only for the first item to avoid double space between items
        if (orientation.equals(horizontal)) {
            if (parent.getChildAdapterPosition(view) == 0) outRect.left = 0
        } else {
            if (parent.getChildAdapterPosition(view) == 0) outRect.top = 0
        }

    }

    companion object {
        const val vertical: Int = 0
        const val horizontal = 1

    }
}