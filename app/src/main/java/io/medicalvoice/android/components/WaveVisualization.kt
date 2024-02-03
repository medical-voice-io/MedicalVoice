package io.medicalvoice.android.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle

@Composable
fun WaveVisualization(
    modifier: Modifier = Modifier,
    frames: List<BarData> = emptyList()
) {
    val yStepSize = 1

    val xAxisData = AxisData.Builder()
        .axisStepSize(1.dp)
        .steps(frames.size - 1)
        .bottomPadding(40.dp)
        // .axisLabelAngle(20f)
        .startDrawPadding(10.dp)
        // .labelData { index -> frames[index].label }
        .build()
    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        // .labelAndAxisLinePadding(20.dp)
        .axisOffset(1.dp)
        // .labelData { index -> (index * (maxRange / yStepSize)).toString() }
        .build()

    val barChartData = BarChartData(
        chartData = frames,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        barStyle = BarStyle(
            paddingBetweenBars = 2.dp,
            barWidth = 10.dp
        ),
        showYAxis = true,
        showXAxis = true,
        horizontalExtraSpace = 10.dp,
    )
    BarChart(modifier = Modifier.height(500.dp), barChartData = barChartData)
}