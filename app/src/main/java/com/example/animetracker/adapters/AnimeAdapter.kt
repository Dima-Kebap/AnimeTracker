package com.example.animetracker.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.animetracker.EditActivity
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.dataBase.MyAnime
import com.example.animetracker.R
import com.example.animetracker.databinding.ListAnimeBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeAdapter(private val listener: Listener?) :
    ListAdapter<AnimeModel, AnimeAdapter.Holder>(Comparator()) {
    class Holder(view: View, private val listener: Listener?, val context: Context) : RecyclerView.ViewHolder(view) {
        val binding = ListAnimeBinding.bind(view)
        private var itemTemp: AnimeModel? = null
        init {
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(it1,position) }
            }
            binding.editAnime.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    itemTemp?.let { it1 -> listener?.onButtonEditClicked(it1,position) }
            }
        }

        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun bind(item: AnimeModel) = with(binding) {

            val db = MainDb.getDb(context).getDao()
            var anime: MyAnime
            itemTemp = item
            TitleList.text = item.title
            RatingList.text = "⭐️" + item.averageRating
            Picasso.get().load(item.smallPoster).into(PosterList)
            CoroutineScope(Dispatchers.IO).launch {
                anime = db.getById(item.id.toInt())
                withContext(Dispatchers.Main) {
                    if(anime==null){
                        episodesProgress.visibility = View.INVISIBLE
                        progress.visibility = View.INVISIBLE
                        MyRating.visibility = View.INVISIBLE
                        addEpisode.visibility = View.INVISIBLE
                        editAnime.visibility = View.INVISIBLE
                    }
                    else{
                        episodesProgress.visibility = View.VISIBLE
                        progress.visibility = View.VISIBLE
                        MyRating.visibility = View.VISIBLE
                        editAnime.visibility = View.VISIBLE

                        val progressBarColor = when(anime.listName){
                            context.getString(R.string.Watching_list) -> context.getColorStateList(R.color.ForestGreen)
                            context.getString(R.string.Completed_list) -> context.getColorStateList(R.color.LawnGreen)
                            context.getString(R.string.On_Hold_list) -> context.getColorStateList(R.color.Yellow)
                            context.getString(R.string.Dropped_list) -> context.getColorStateList(R.color.OrangeRed)
                            else -> context.getColorStateList(R.color.LightSlateGray)//Plan_to_Watch_list
                        }

                        episodesProgress.progressTintList = progressBarColor

                        val myRating  = anime.rating
                        MyRating.text = if(myRating == 0) "" else "⭐️$myRating"

                        var totalEpisodes = item.episodeCount

                        totalEpisodes = if(totalEpisodes == "???") "???" else totalEpisodes
                        if(totalEpisodes == "???"){
                            progress.text = "${anime.episodes}/$totalEpisodes"
                            episodesProgress.max = anime.episodes * 2
                        }
                        else{
                            progress.text = "${anime.episodes}/$totalEpisodes"
                            episodesProgress.max = totalEpisodes.toInt()
                            addEpisode.visibility = if(anime.episodes < totalEpisodes.toInt()) View.VISIBLE else View.INVISIBLE
                        }
                        addEpisode.setOnClickListener {
                            var listName = anime.listName
                            if(totalEpisodes !="???"){
                                if( episodesProgress.progress < totalEpisodes.toInt()){//щоб при подвійному натисканні на мишку, якщо прогрес 11/12 то було в результаті не 13/12, а 12/12
                                    episodesProgress.progress +=1
                                    progress.text = "${episodesProgress.progress}/$totalEpisodes"
                                    if(episodesProgress.progress == totalEpisodes.toInt()){
                                        addEpisode.visibility = View.INVISIBLE
                                        listName = context.getString(R.string.Completed_list)
                                    }
                                }
                            }else{
                                episodesProgress.progress +=1
                                episodesProgress.max = episodesProgress.progress*2
                                progress.text = "${episodesProgress.progress}/$totalEpisodes"
                            }
                            Thread{db.replaceItem(MyAnime(anime.id,listName,anime.rating,episodesProgress.progress))}.start()
                        }

                        episodesProgress.progress = anime.episodes
                    }
                }
            }
        }
    }


    class Comparator : DiffUtil.ItemCallback<AnimeModel>() {
        override fun areItemsTheSame(oldItem: AnimeModel, newItem: AnimeModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AnimeModel, newItem: AnimeModel): Boolean {
            return oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_anime, parent, false)
        return Holder(view, listener, parent.context)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {
        fun onClick(item: AnimeModel, position:Int)
        fun onButtonEditClicked(item: AnimeModel, position: Int)
    }
}