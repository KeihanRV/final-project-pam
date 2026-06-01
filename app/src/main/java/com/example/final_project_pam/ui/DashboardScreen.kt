package com.example.final_project_pam.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.final_project_pam.data.model.DailyUsageStat
import com.example.final_project_pam.ui.theme.*
import com.example.final_project_pam.viewmodel.AggregatedAppUsage
import com.example.final_project_pam.viewmodel.DashboardSummary
import com.example.final_project_pam.viewmodel.DashboardUiState
import com.example.final_project_pam.viewmodel.DashboardViewModel
import com.example.final_project_pam.viewmodel.StackedChartData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.Fill

private val chartColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFF2196F3),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFFF5722),
    Color(0xFF607D8B),
)

private val dayLabelListKey = ExtraStore.Key<List<String>>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UnscrollPrimary)
            }
        }
        is DashboardUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is DashboardUiState.Success -> {
            DashboardContent(
                userName = state.userName,
                dailyUsageStats = state.dailyUsageStats,
                summary = state.summary,
                aggregatedApps = state.aggregatedApps,
                stackedChartData = state.stackedChartData
            )
        }
    }
}

@Composable
private fun DashboardContent(
    userName: String,
    dailyUsageStats: List<DailyUsageStat>,
    summary: DashboardSummary,
    aggregatedApps: List<AggregatedAppUsage>,
    stackedChartData: StackedChartData
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(summary = summary)

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Penggunaan 7 Hari Terakhir",
            subtitle = stackedChartData.dateRangeLabel.ifEmpty { null }
        )

        Spacer(modifier = Modifier.height(10.dp))

        UsageChart(
            stackedChartData = stackedChartData,
            dailyUsageStats = dailyUsageStats
        )

        Spacer(modifier = Modifier.height(28.dp))

        SectionHeader(title = "Aplikasi Paling Sering Dipakai")

        Spacer(modifier = Modifier.height(12.dp))

        RecentAppsRow(
            aggregatedApps = aggregatedApps,
            isEmpty = aggregatedApps.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryCard(summary: DashboardSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UnscrollPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Total minggu ini",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatMinutes(summary.totalMinutesWeek),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rata-rata",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatMinutes(summary.averageMinutesDay),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aplikasi",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${summary.appCount} dipantau",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(UnscrollPrimary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = UnscrollBlack
            )
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = UnscrollBlack.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun UsageChart(
    stackedChartData: StackedChartData,
    dailyUsageStats: List<DailyUsageStat>
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val indoDayLabels = remember {
        val today = java.time.LocalDate.now()
        val names = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        (6 downTo 0).map { names[today.minusDays(it.toLong()).dayOfWeek.value % 7] }
    }

    LaunchedEffect(stackedChartData) {
        if (stackedChartData.series.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    stackedChartData.series.forEach { seriesData ->
                        series(seriesData.values)
                    }
                }
                extras { it[dayLabelListKey] = indoDayLabels }
            }
        }
    }

    val bottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
        val labels = context.model.extraStore[dayLabelListKey]
        labels.getOrElse(x.toInt()) { "" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (stackedChartData.series.isNotEmpty()) {
            Column {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                chartColors.map { color ->
                                    rememberLineComponent(fill = Fill(color), thickness = 16.dp)
                                }
                            ),
                            mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = bottomAxisValueFormatter,
                            itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)
                )

                if (stackedChartData.series.isNotEmpty()) {
                    ChartLegend(series = stackedChartData.series)
                }
            }
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
                    tint = UnscrollBlack.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Belum ada data penggunaan",
                    color = UnscrollBlack.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mulai pantau penggunaan aplikasi\nuntuk melihat statistik di sini",
                    color = UnscrollBlack.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ChartLegend(series: List<com.example.final_project_pam.viewmodel.SeriesData>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text = "Breakdown per aplikasi:",
            fontSize = 11.sp,
            color = UnscrollBlack.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        series.take(8).forEachIndexed { index, data ->
            val icon = rememberAppIcon(context, data.packageName)
            val color = chartColors[index % chartColors.size]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color.copy(alpha = 0.15f))
                        .border(1.5.dp, color, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.appName,
                    fontSize = 12.sp,
                    color = UnscrollBlack.copy(alpha = 0.75f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatMinutes(data.values.sum().toLong()),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UnscrollBlack
                )
            }
        }
        if (series.size > 8) {
            Text(
                text = "dan ${series.size - 8} aplikasi lainnya",
                fontSize = 11.sp,
                color = UnscrollBlack.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RecentAppsRow(
    aggregatedApps: List<AggregatedAppUsage>,
    isEmpty: Boolean
) {
    if (isEmpty) {
        Text(
            "Belum ada aktivitas",
            color = UnscrollBlack.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        itemsIndexed(aggregatedApps, key = { _, app -> app.packageName }) { index, app ->
            RecentAppCard(app = app, rank = index + 1)
        }
    }
}

@Composable
private fun RecentAppCard(app: AggregatedAppUsage, rank: Int) {
    val context = LocalContext.current
    val icon = rememberAppIcon(context, app.packageName)
    val totalDisplay = formatMinutes(app.totalMinutes)

    Card(
        modifier = Modifier.size(width = 140.dp, height = 150.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UnscrollTertiary)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (rank <= 3) UnscrollPrimary else UnscrollBlack.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.White else UnscrollBlack.copy(alpha = 0.6f)
                )
            }

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
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = UnscrollSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = app.appName,
                    fontWeight = FontWeight.Bold,
                    color = UnscrollBlack,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = totalDisplay,
                    color = UnscrollBlack.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    return when {
        minutes >= 60 -> "${minutes / 60}j ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    FinalprojectpamTheme {
        DashboardContent(
            userName = "Harvey",
            dailyUsageStats = listOf(),
            summary = DashboardSummary(0, 0, 0),
            aggregatedApps = listOf(),
            stackedChartData = StackedChartData(emptyList(), emptyList())
        )
    }
}
