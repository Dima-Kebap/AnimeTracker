package com.example.animetracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.dataBase.MyAnime
import com.example.animetracker.databinding.ActivityEditBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditActivity : AppCompatActivity() {
    private lateinit var modelId: String
    private lateinit var modelTitle: String
    private lateinit var modelEpCount: String
    private lateinit var binding: ActivityEditBinding
    private var lastCheckedChip: Chip? = null
    private var listName: String = "null"
    private var rating: Int = 0
    private var episode: Int = 0
    private lateinit var db: MainDb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = MainDb.getDb(this)
        modelId = intent.getStringExtra("anime_id").toString()
        modelTitle = intent.getStringExtra("anime_title").toString()
        modelEpCount = intent.getStringExtra("anime_ep_count").toString()
        // викликаємо suspend функцію
        CoroutineScope(Dispatchers.IO).launch {
            val anime = db.getDao().getById(modelId.toInt())
            if (anime != null) {
                listName = anime.listName
                rating = anime.rating
                episode = anime.episodes
            }
            withContext(Dispatchers.Main) {
                init()
            }
        }


    }


    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun init() = with(binding) {
        //idEdit.text = modelId
        butBack.setOnClickListener { finish() }
        scoreDescription.text = scoreDescription(rating)
        animeTitle.text = modelTitle

        if (listName != "null") {//перевірка чи додане аніме в якийсь список
            val chip = getChip(listName)
            chip.isChecked = true // встановлюємо обраним
            chip.setTextColor(getColor(R.color.Black))
            lastCheckedChip = chip
            setChipProgressColor(chip)
        } else {//якщо немає в списку то автоматично виставляєця "в планах"
            val defaultChip = getChip(getString(R.string.Plan_to_Watch_list))
            defaultChip.isChecked = true // встановлюємо обраним
            defaultChip.setChipBackgroundColorResource(R.color.LightSlateGray)
            lastCheckedChip = defaultChip
            listName = getString(R.string.Plan_to_Watch_list)//по дефолту додаєця у список(у планах подивитися)
        }
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != (-1)) {
                val chip: Chip = group.findViewById(checkedId)
                lastCheckedChip?.setTextColor(chip.currentTextColor)
                lastCheckedChip?.chipBackgroundColor = chip.chipBackgroundColor// Повертаємо колір попереднього вибраного Chip до стандартного
                lastCheckedChip = chip// Зберігаємо поточний вибраний Chip
                listName = chip.text.toString()
                setChipProgressColor(chip)
                chip.setTextColor(getColor(R.color.Black))
                when (listName) {
                    getString(R.string.Completed_list) -> {
                        if (modelEpCount != "???") {
                            episode = modelEpCount.toInt()
                            episodesProgress.progress = episode
                            episodePicker.value = episode
                            progr.text = "$episode/$episode"
                        }
                    }
                    getString(R.string.Plan_to_Watch_list) -> {
                        episode = 0
                        episodesProgress.progress = 0
                        episodePicker.value = 0
                        progr.text = "0/$modelEpCount"
                    }
                }
            }
        }

        //Picker-и та Progress
        ratingPicker.maxValue = 10
        ratingPicker.minValue = 0
        ratingPicker.value = rating
        ratingPicker.setOnValueChangedListener { _, _, newVal ->
            rating = newVal
            scoreDescription.text = scoreDescription(newVal)
        }
        if (modelEpCount != "???") {
            episodePicker.maxValue = modelEpCount.toInt()
            episodesProgress.max = modelEpCount.toInt()
            progr.text = "$episode/$modelEpCount"
        } else {
            //якщо кількість епізодів в аніме невідома, то його неможна додати до списку "завершено"
            val chip = getChip(getString(R.string.Completed_list))
            chip.isClickable = false
            chip.setTextColor(getColor(R.color.Gray))
            chip.setChipStrokeColorResource(R.color.Gray)
            episodesProgress.max = episode * 2
            episodePicker.maxValue = 10000
            progr.text = "$episode/???"
        }
        episodesProgress.progress = episode
        episodePicker.minValue = 0
        episodePicker.value = episode
        episodePicker.wrapSelectorWheel = false//щоб з 0 неможна було переключитися одразу на максимальне
        episodePicker.setOnValueChangedListener { _, _, newVal ->
            episode = newVal
            episodesProgress.progress = newVal
            if (modelEpCount != "???")
                progr.text = "$newVal/${modelEpCount}"
            else {
                episodesProgress.max = newVal * 2
                progr.text = "$newVal/???"
            }

            if (newVal < episodesProgress.max && newVal!=0)
                getChip(getString(R.string.Watching_list)).isChecked = true // встановлюємо обраним
            else if (newVal == 0)
                getChip(getString(R.string.Plan_to_Watch_list)).isChecked = true // встановлюємо обраним
            else if (modelEpCount != "???")
                getChip(getString(R.string.Completed_list)).isChecked = true // встановлюємо обраним
        }
    }

private fun scoreDescription(score:Int):String{
    return when(score){
        1-> "Appalling"
        2-> "Horrible"
        3-> "Very Bad"
        4-> "Bad"
        5-> "Average"
        6-> "Fine"
        7-> "Good"
        8-> "Very Good"
        9-> "Great"
        10-> "Masterpiece"
        else-> "Not Yet Scored"
    }
}

    private fun setChipProgressColor(chip: Chip){
        val color = when (listName) {
            getString(R.string.Watching_list) -> R.color.ForestGreen
            getString(R.string.Completed_list) -> R.color.LawnGreen
            getString(R.string.On_Hold_list) -> R.color.Yellow
            getString(R.string.Dropped_list) -> R.color.OrangeRed
            else ->R.color.LightSlateGray
        }
        chip.setChipBackgroundColorResource(color)
        binding.episodesProgress.progressTintList = getColorStateList(color)
    }

//    fun add33Dropped(view: View) {
//        Thread{
//            for (i in 1..66)
//                db.getDao().replaceItem(MyAnime(i,getString(R.string.Dropped_list),5,1))
//            for (i in 67..88)
//                db.getDao().replaceItem(MyAnime(i,getString(R.string.On_Hold_list),8,1))
//        }.start()
//    }

    fun deleteFromList(view: View) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirmation))
            .setCancelable(false)
            .setPositiveButton("remove") { _, _ ->
                Thread{db.getDao().deleteById(modelId)}.start()
                setResult(0)
                finish()
            }
            .setNegativeButton("cancel") { _, _ -> }
            .create().show()
    }

    fun editMyAnime(view: View) {
        Thread{db.getDao().replaceItem(MyAnime(modelId.toInt(),listName,rating,episode))}.start()
        val positionInList = intent.getIntExtra("position",-1)
        setResult(positionInList)
        finish()
    }

    private fun getChip(text: String): Chip {
        for (i in 0 until binding.chipGroup.childCount)
            if ((binding.chipGroup.getChildAt(i) as Chip).text == text) //знайшли чіп
                return binding.chipGroup.getChildAt(i) as Chip

        return binding.chipGroup.getChildAt(0) as Chip
    }

}