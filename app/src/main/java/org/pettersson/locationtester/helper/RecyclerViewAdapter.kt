package org.pettersson.locationtester.helper

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

private fun Context.lifecycleOwner(): LifecycleOwner? {
    var curContext = this
    var maxDepth = 20
    while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
        curContext = (curContext as ContextWrapper).baseContext
    }
    return if (curContext is LifecycleOwner) {
        curContext
    } else {
        null
    }
}

class RecyclerViewAdapter : RecyclerView.Adapter<BindingViewHolder>() {

    private val items = mutableListOf<RecyclerItem>()


    override fun onViewAttachedToWindow(holder: BindingViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.binding.lifecycleOwner = holder.itemView.context.lifecycleOwner()
    }

    override fun onViewDetachedFromWindow(holder: BindingViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.binding.lifecycleOwner = null
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutId
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, viewType, parent, false)

        val holder =  BindingViewHolder(binding)
        return holder
    }

    override fun onBindViewHolder(
        holder: BindingViewHolder,
        position: Int
    ) {
        getItem(position).bind(holder.binding)
        holder.binding.executePendingBindings()
    }

    fun updateData(newItems: List<RecyclerItem>) {
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun getItem(position: Int): RecyclerItem {
        return items[position]
    }
}

class BindingViewHolder(val binding: ViewDataBinding)
             : RecyclerView.ViewHolder(binding.root)
{
}