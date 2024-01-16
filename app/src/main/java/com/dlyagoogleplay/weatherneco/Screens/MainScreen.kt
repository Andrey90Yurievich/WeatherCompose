package com.dlyagoogleplay.weatherneco.Screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dlyagoogleplay.weatherneco.R
import com.dlyagoogleplay.weatherneco.data.WeatherModel
import com.dlyagoogleplay.weatherneco.ui.theme.BlueLight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset


import com.google.accompanist.pager.rememberPagerState




import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    Column(modifier = Modifier.padding(5.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = 0.dp, backgroundColor = BlueLight, shape = RoundedCornerShape(10.dp))
        {
            Column (modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column (modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {}
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time, style = TextStyle(fontSize = 15.sp), color = androidx.compose.ui.graphics.Color.White
                    )
                    AsyncImage(model = "https:" + currentDay.value.icon,
                        contentDescription = "image2",
                        modifier = Modifier
                            .padding(top = 3.dp, end = 8.dp)
                            .size(35.dp))
                }
                Text(
                    text = currentDay.value.city,
                    style = TextStyle(fontSize = 24.sp),
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text(
                    text = if (currentDay.value.currentTemp.isNotEmpty())
                        currentDay.value.currentTemp.toFloat().toInt().toString() + "°С"
                    else "${currentDay.value.maxTemp.toFloat().toInt().toString()}°С" +
                            "/${currentDay.value.minTemp.toFloat().toInt()}°С"
                        ,
                    style = TextStyle(fontSize = 65.sp),
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = androidx.compose.ui.graphics.Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            onClickSearch.invoke()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "im3",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Text(
                        text = "${currentDay.value.maxTemp.toFloat().toInt()}°С/${currentDay.value.minTemp.toFloat().toInt()}°С",
                        style = TextStyle(fontSize = 16.sp),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    IconButton(
                        onClick = {
                            onClickSync.invoke()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.sync),
                            contentDescription = "im4",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tablist = listOf("По часам", "По дням")
    //3 разные состояния
    val pagerState = rememberPagerState() //первая вкладка 0 и вторая 1
    val tabIndex = pagerState.currentPage //0 и 1
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(start = 5.dp, end = 5.dp).clip(RoundedCornerShape(5.dp))) {
        TabRow(selectedTabIndex = tabIndex,
            indicator = { pos ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, pos)) }, backgroundColor = BlueLight, contentColor = Color.White
        ) { tablist.forEachIndexed { index, text ->
                Tab(selected = false,
                    onClick = {
                        Log.e("TAG", "$pagerState")
                        Log.e("TAG", "$tabIndex")
                        Log.e("TAG", "$coroutineScope")
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index) } //animateScrollToPage - анимировать прокрутку страницы
                },
                    text = { Text(text = text) } //текст выводится по индексам ("По часам", "По дням")
                )
            }
        }
        HorizontalPager(count = tablist.size, //тут 2 слова размер 2
            state = pagerState, modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when(index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay) } } }

private fun getWeatherByHours(hours: String) : List<WeatherModel> {
    if (hours.isEmpty()) return listOf()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°С",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                ""
            )
        )
    }
    return list

}