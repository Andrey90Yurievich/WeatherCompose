package com.dlyagoogleplay.weatherneco

import android.app.DownloadManager.Request
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dlyagoogleplay.weatherneco.Screens.DialogSearch
import com.dlyagoogleplay.weatherneco.Screens.MainCard
import com.dlyagoogleplay.weatherneco.Screens.TabLayout
import com.dlyagoogleplay.weatherneco.data.WeatherModel
import com.dlyagoogleplay.weatherneco.ui.theme.WeatherNecoTheme
import org.json.JSONObject


const val API_KEY = "20314ad8b5de4f67872175656241501"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherNecoTheme {
                //состояние
                val daysList = remember {
                    //передаем список из объектов WeatherModel
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    //передаем список из объектов WeatherModel
                    mutableStateOf(false)
                }

                val currentDay = remember {
                    //передаем список из объектов WeatherModel
                    mutableStateOf(WeatherModel(
                        "",
                        "",
                        "10.0",
                        "",
                        "",
                        "10.0",
                        "10.0",
                        ""

                    ))
                }
                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this,  daysList, currentDay)
                    })
                }
                getData("London", this, daysList, currentDay)




                Image(painter = painterResource(id = R.drawable.logo), contentDescription = "image1", modifier = Modifier.fillMaxSize().alpha(0.5f), contentScale = ContentScale.FillBounds)
                Column {
                    MainCard(currentDay, onClickSync = {
                        getData("London", this@MainActivity, daysList, currentDay)
                    }, onClickSearch = {
                        dialogState.value = true
                    })
                    TabLayout(daysList, currentDay) }
                }
            }
        }
    }


//получить данные
private fun getData(city: String, context: Context,
                    daysList: MutableState<List<WeatherModel>>,
                    currentDay: MutableState<WeatherModel>) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" + "&q=$city" + "&days=" + "3" + "&aqi=no&alerts=no"
    //Очередь запросов
    val queue = Volley.newRequestQueue(context)
    //Log.e("TAG", "$queue")
    //запрос строки
    val sRequest = StringRequest(
        com.android.volley.Request.Method.GET,
        url, {
            //ответ
            response ->
            val list = getWeatherByDays(response)
            //получаем обработанный список weathermodel
            currentDay.value = list[0]
            daysList.value = list
            Log.e("TAG", "response") },
        { Log.d("MyLog", "VolleyError: $it") })
    queue.add(sRequest) }

//узнать погоду по Дням
private fun getWeatherByDays(response: String) : List<WeatherModel> { //передали ответ
// строку с сервера и возвращаем список с объектами тира weathermodel
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>() //пустой список в который добавляем
    //JSONObject - понимает данные JSON
    val mainObject = JSONObject(response)
    //в city присваиваем название города из location
    val city = mainObject.getJSONObject("location").getString("name")
        //в days создадуться массивы прогноз дня
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        //В айтем зайдет ответ json за день
        val item = days[i] as JSONObject
        list.add( //в лист добавляются объекты каждого дня
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()

            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
    )
    return list
    //Log.e("TAG", "$list")
}

