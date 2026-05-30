package com.example.final_project_pam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.final_project_pam.data.model.AppUsageStats
import com.example.final_project_pam.data.model.DailyUsageStat
import com.example.final_project_pam.ui.theme.*
import com.example.final_project_pam.viewmodel.DashboardUiState
import com.example.final_project_pam.viewmodel.DashboardViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = UnscrollPrimary)
            }
            is DashboardUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    userName = state.userName,
                    usageStats = state.usageStats,
                    dailyUsageStats = state.dailyUsageStats
                )
            }
        }
    }
}

private val dayLabelListKey = ExtraStore.Key<List<String>>()

@Composable
fun DashboardContent(
    userName: String,
    usageStats: List<AppUsageStats>,
    dailyUsageStats: List<DailyUsageStat>
) {
    val dayNames = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEEE")
        (6 downTo 0).map { today.minusDays(it.toLong()).format(formatter) }
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyUsageStats) {
        if (dailyUsageStats.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(dailyUsageStats.map { it.totalMinutes.toFloat() })
                }
                extras { it[dayLabelListKey] = dayNames }
            }
        }
    }

    val bottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
        val labels = context.model.extraStore[dayLabelListKey]
        labels.getOrElse(x.toInt()) { "" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hi, $userName",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )
        Text(
            text = "Always look at your achievement!",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = UnscrollBlack.copy(alpha = 0.8f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Penggunaan 7 Hari Terakhir",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = UnscrollPrimary)
        ) {
            if (dailyUsageStats.isNotEmpty()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = bottomAxisValueFormatter
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(12.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada data penggunaan",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mulai pantau penggunaan aplikasi\nuntuk melihat statistik di sini",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recent",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(usageStats) { stat ->
                RecentAppCard(stat)
            }

            if (usageStats.isEmpty()) {
                item {
                    Text("No recent activity", color = UnscrollBlack.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun RecentAppCard(stat: AppUsageStats) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UnscrollTertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = UnscrollSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stat.app_name,
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack,
                fontSize = 14.sp
            )

            Text(
                text = "${stat.time_spent_minutes} Minutes",
                color = UnscrollBlack.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FinalprojectpamTheme {
        DashboardContent(
            userName = "Harvey",
            usageStats = listOf(),
            dailyUsageStats = listOf()
        )
    }
}