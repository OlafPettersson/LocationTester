package org.pettersson.locationtester.viewModels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.pettersson.locationtester.BR
import org.pettersson.locationtester.R
import org.pettersson.locationtester.helper.RecyclerItem

class DetailsButtonRowViewModel (val title: String,
                                 val intent: LiveData<Intent?>
){
    val isEnabled = intent.map { it != null }

    fun onClick(ctx: Context){
        if(intent.value != null)
            ctx.startActivity(intent.value)
    }

    companion object{
        fun recyclerItem(title: String, intent: LiveData<Intent?>) =
            RecyclerItem(DetailsButtonRowViewModel(title, intent),
                R.layout.fragment_details_button_row, BR.viewModel)

        fun recyclerItem(title: String, intent: Intent) =
            recyclerItem(title, MutableLiveData(intent))
    }
}