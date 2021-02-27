package org.pettersson.locationtester.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.pettersson.locationtester.BR
import org.pettersson.locationtester.R
import org.pettersson.locationtester.helper.RecyclerItem

class DetailsTextRowViewModel (val title: String,
                               val content : LiveData<String>,
                               val asError : Boolean){

    companion object{
        fun recyclerItem(title: String, content : LiveData<String>, asError : Boolean = false) =
            RecyclerItem(DetailsTextRowViewModel(title, content, asError),
                R.layout.fragment_details_text_row, BR.viewModel)

        fun recyclerItem(title: String, content : String, asError : Boolean = false) =
               recyclerItem(title, MutableLiveData(content), asError)

    }
}