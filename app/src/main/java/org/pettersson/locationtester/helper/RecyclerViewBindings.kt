package org.pettersson.locationtester.helper

import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingComponent
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("items")
fun setRecyclerViewItems(
    recyclerView: RecyclerView,
    items: List<RecyclerItem>?
) {
    var adapter = (recyclerView.adapter as? RecyclerViewAdapter)
    if (adapter == null) {
        adapter = RecyclerViewAdapter()
        recyclerView.adapter = adapter
    }

    adapter.updateData(items.orEmpty())
}