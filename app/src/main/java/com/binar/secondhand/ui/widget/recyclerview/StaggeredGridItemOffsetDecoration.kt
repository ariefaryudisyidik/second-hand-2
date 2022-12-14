package com.binar.secondhand.ui.widget.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StaggeredGridItemOffsetDecoration(
    private val spacing: Int,
    private val spanCount: Int = 2
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.apply {
            left = spacing / (spanCount * 2)
            right = spacing / (spanCount * 2)
            top = spacing / (spanCount * 2)
            bottom = spacing / (spanCount * 2)
        }
    }
}