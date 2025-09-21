package com.ierusalem.androchat.core.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.features_local.tcp.presentation.TcpView
import kotlin.math.max

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdaptiveTcpTabRow(
    allTabs: SnapshotStateList<TcpView>,
    currentPage: Int,
    onTabChanged: (TcpView) -> Unit,
    minTabWidth: Dp = 120.dp
) {
    val ctx = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // ✅ Read composable values here (legal composable context)
    val tabTextStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp)
    val minTabWidthPx = with(density) { minTabWidth.toPx() }
    val horizontalPaddingPx = with(density) { 24.dp.toPx() }

    BoxWithConstraints(Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background)) {
        val availablePx = with(density) { maxWidth.toPx() }

        // Now this block does NOT call composables
        val perTabMinWidthPx = remember(allTabs, tabTextStyle, minTabWidthPx, horizontalPaddingPx) {
            allTabs.map { tab ->
                val label = tab.displayName.asString(ctx)
                val textWidthPx = textMeasurer
                    .measure(AnnotatedString(label), style = tabTextStyle)
                    .size.width.toFloat()

                max(textWidthPx + horizontalPaddingPx * 2, minTabWidthPx)
            }
        }

        val fits = perTabMinWidthPx.sum() <= availablePx

        val indicator: @Composable (List<TabPosition>) -> Unit = { tabPositions ->
            if (currentPage in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage]),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (fits) {
            // Equal-width tabs (TabRow already distributes them evenly)
            TabRow(
                selectedTabIndex = currentPage,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                divider = {},
                indicator = indicator
            ) {
                allTabs.forEachIndexed { index, t ->
                    AndroChatTab(
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag(t.testIdentifier),
                        isSelected = index == currentPage,
                        onSelected = { onTabChanged(t) },
                        tab = t.displayName
                    )
                }
            }
        } else {
            // Doesn’t fit → scrollable, keep your min width
            ScrollableTabRow(
                selectedTabIndex = currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                divider = {},
                indicator = indicator
            ) {
                allTabs.forEachIndexed { index, t ->
                    AndroChatTab(
                        modifier = Modifier
                            .widthIn(min = minTabWidth)
                            .semantics { testTagsAsResourceId = true }
                            .testTag(t.testIdentifier),
                        isSelected = index == currentPage,
                        onSelected = { onTabChanged(t) },
                        tab = t.displayName
                    )
                }
            }
        }
    }
}
