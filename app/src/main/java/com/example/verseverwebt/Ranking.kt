package com.example.verseverwebt

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.verseverwebt.api.ApiClient
import com.example.verseverwebt.ui.theme.VerseVerwebtTheme
import com.example.verseverwebt.ui.theme.CustomTypography
import com.example.verseverwebt.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Ranking : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var users by remember { mutableStateOf<List<User>>(emptyList()) }
            var userRank by remember { mutableStateOf(0) }

            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getLong("user_id", 0L)

            LaunchedEffect(Unit) {
                ApiClient.instance.calculateRankings()
                ApiClient.instance.getRankedUsers().enqueue(object : Callback<List<User>> {
                    override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                        if (response.isSuccessful) {
                            users = response.body() ?: emptyList()
                            Log.d("Ranking", "Fetched users: $users")
                        }
                        else {
                            Log.e("Ranking", "API call failed with response code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<List<User>>, t: Throwable) {
                        Log.e("Ranking", "Error fetching ranked users", t)
                    }
                })
            }

            for(user in users){
                if(user.id == userId){
                    userRank = user.rank
                    Log.d("Ranking", "Rank: $userRank")
                }
            }

            VerseVerwebtTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RankingContent(users = users, rank = userRank)
                }
            }
        }
    }
}

@Composable
fun RankingContent(users: List<User>, rank: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackToMenuButton()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Leaderboard",
            style = CustomTypography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (users.isEmpty()) {
            Text(
                text = "No users available",
                style = CustomTypography.bodyMedium,
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    if(user.rank != 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${user.rank}. ${user.name}",
                                style = CustomTypography.bodyLarge,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "%.2f".format(user.time1 + user.time2 + user.time3 + user.time4 + user.time5 + user.time6 + user.time7) + "s",
                                style = CustomTypography.bodyMedium,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = if (rank > 0) "Your rank: $rank" else "Your rank: N/A",
            style = CustomTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}