package com.example.moviesplanet.presentation.moviedetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesplanet.R
import com.example.moviesplanet.data.model.MovieExternalInfo
import com.example.moviesplanet.presentation.generic.BaseViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_movie_external_info.*

class MovieExternalInfoAdapter(private val onClick: (MovieExternalInfo) -> Unit) : RecyclerView.Adapter<BaseViewHolder>() {

    var list = listOf<MovieExternalInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater =  LayoutInflater.from(parent.context);
        return ExternalInfoViewHolder(inflater.inflate(R.layout.item_movie_external_info, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setData(items: List<MovieExternalInfo>) {
        list = items
        notifyDataSetChanged()
    }

    inner class ExternalInfoViewHolder(override val containerView: View) : BaseViewHolder(containerView), LayoutContainer {

        override fun bind(position: Int) {
            val item = list[position]
            infoTextView.text = item.name
            showImageButton.setOnClickListener { onClick(item) }
            externalInfoContainer.setOnClickListener { onClick(item) }
        }
    }
}