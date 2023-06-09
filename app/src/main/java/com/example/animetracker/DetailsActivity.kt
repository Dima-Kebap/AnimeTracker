package com.example.animetracker


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.animetracker.adapters.AnimeModel
import com.example.animetracker.adapters.ExtendedAnimeModel
import com.example.animetracker.dataBase.MainDb
import com.example.animetracker.databinding.ActivityDetailsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.squareup.picasso.Picasso
import java.io.Serializable

private const val REQUEST_CODE = 1234

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var anime: ExtendedAnimeModel
    private lateinit var animeId: String
    private var positionInList:Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animeId = intent.getStringExtra("anime").toString()
        positionInList = intent.getIntExtra("position",-1)
        binding.nestedScrollView.visibility = View.INVISIBLE
        binding.butEdit.visibility = View.INVISIBLE
        binding.swipeRefreshLayout.setOnRefreshListener { searchAnime(animeId) }
        binding.swipeRefreshLayout.isRefreshing = true
        searchAnime(animeId)
    }



    @SuppressLint("SetTextI18n")
    private fun updateDetails() = with(binding) {//запис даних про аніме на сторінці
        Picasso.get().load(anime.largePoster).into(Poster)
        Title.text = anime.title
        Rating.text = "Score\n⭐️" + anime.averageRating
        description.text = anime.description
        Popularity.text = "Popularity\n#" + anime.popularityRank
        Rank.text = "Rank\n#" + anime.ratingRank
        episodeCount.text =  "${anime.episodeCount} x ${anime.episodeLength} min"
        genres.text= anime.genres
        season.text = anime.season
        totalMarks.text = "Total marks: ${anime.ratingFrequencies.sum()}"
        ratingFrequencies()
    }

    private fun ratingFrequencies(){//гістограма оцінок(скільки якої оцінки поставили цьому аніме
        val list = anime.ratingFrequencies

        val entries = (1..list.size).map { BarEntry(it.toFloat(),list[it-1].toFloat()) }

        // Налаштування графіка
        val chart = binding.chart
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.animateY(3000)//час малювання(плавно)
        //відступ справа, бо якщо число більше за 99999, то воно показується неповністю
        chart.setExtraOffsets(0f, 0f, 50f, 0f)
        // Дані та налаштування для стовпців гістограм

        val colors = (1..10).map { getColor(R.color.DarkGreen) }.toMutableList()
        Thread{
            val myRating = MainDb.getDb(this).getDao().getById(anime.id.toInt())
            if(myRating!=null && myRating.rating!=0)
                colors[myRating.rating-1] = getColor(R.color.Red)//якщо аніме оцінене, то оцінка виділяється червоним
        }.start()


        val textColor =
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                getColor(R.color.White)
            else
                getColor(R.color.Black)

        val barDataSet = BarDataSet(entries, "")
        barDataSet.colors = colors
        barDataSet.valueTextSize = 12f//кількість оцінок
        barDataSet.valueTextColor = textColor
        barDataSet.barBorderWidth = 0.5f
        barDataSet.setDrawValues(true)

        // Налаштування вісі Х
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.textColor = textColor
        xAxis.textSize = 14f//оценка
        xAxis.labelCount = entries.size
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barData = BarData(barDataSet)// Дані та налаштування для графіка
        barData.barWidth = 0.5f

        chart.data = barData// Встановлюємо дані для графіка

        chart.invalidate()// Оновлюємо графік
    }


    fun butEdit(view: View) {//перехід на сторінку редагування своїх даних про це аніме
        val intent = Intent(this, EditActivity::class.java)
            .apply {
                //putExtra("anime", anime as Serializable)
                putExtra("anime_id", anime.id)
                putExtra("anime_title", anime.title)
                putExtra("anime_ep_count", anime.episodeCount)
            }
        startActivityForResult(intent, REQUEST_CODE)//для оновленя гістограми оцінок(якщо користувач поставив іншу оцінку, то вона виділиться)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE){
             if (resultCode == 0)
                positionInList = -1
            ratingFrequencies()//виділення поставленої нової оцінки
        }

    }

    override fun onBackPressed() {
        setResult(positionInList)
        finish()
        super.onBackPressed()
    }

    fun butBack(view: View) {
        setResult(positionInList)
        finish()
    }


    private fun searchGenres(id: String) {//запит до апі
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET,
            "https://kitsu.io/api/edge/anime/$id/genres",
            { result -> anime.genres = RequestParse().parseGenres(result)
                updateDetails()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.nestedScrollView.visibility = View.VISIBLE
                binding.butEdit.visibility = View.VISIBLE},
            { error -> Log.d("MyLog", "Error: $error")
                Toast.makeText(this, "timeout", Toast.LENGTH_SHORT).show()}
        )
        queue.add(request)
    }

    private fun searchAnime(id: String) {//запит до апі
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET,
            "https://kitsu.io/api/edge/anime?filter[id]=$id",
            { result ->
                anime = RequestParse().parseOneAnime(result)
                searchGenres(id)
            },
            { error -> Log.d("MyLog", "Error: $error")
                Toast.makeText(this, "timeout", Toast.LENGTH_SHORT).show()}
        )
        queue.add(request)
    }

}