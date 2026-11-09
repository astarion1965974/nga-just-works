package gov.anzong.androidnga.ui.coui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.chip.ChipTab
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.Fab
import gov.anzong.androidnga.ui.miuix.MiuixReaderCallbacks
import gov.anzong.androidnga.ui.miuix.MiuixReaderTabsState
import kotlinx.coroutines.delay

@Composable
internal fun CouiReaderPage(activity: Activity, content: View, tabs: MiuixReaderTabsState, callbacks: MiuixReaderCallbacks) {
    val reply = callbacks.onReply
    CouiForumPage(
        activity = activity,
        onTitleClick = callbacks.onTitleClick,
        extraTopBar = { backdrop ->
            if (tabs.pages.size > 1) ReaderPages(backdrop, tabs, callbacks)
        },
        floatingAction = if (reply == null) null else {
            {
                val refresh = callbacks.onReplyLongPressRepeat
                val modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                Fab(
                    modifier = if (refresh == null) modifier else modifier.holdToRefresh(refresh, callbacks.longPressRepeatMillis),
                    renderer = remember { CouiFloatingRenderer.standalone() },
                    onClick = reply,
                ) {
                    Icon(Icons.Default.Edit, "回复", tint = Color.White)
                }
            }
        },
    ) { padding ->
        AndroidView(
            factory = {
                (content.parent as? ViewGroup)?.removeView(content)
                content.visibility = View.VISIBLE
                content
            },
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        )
    }
}

@Composable
private fun ReaderPages(backdrop: CouiBackdrop, tabs: MiuixReaderTabsState, callbacks: MiuixReaderCallbacks) {
    val pages = tabs.pages
    val selected = tabs.selected.coerceIn(pages.indices)
    val scroll = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    LaunchedEffect(selected, pages.size) {
        if (scroll.layoutInfo.visibleItemsInfo.none { it.index == selected }) {
            scroll.scrollToItem(selected)
        }
    }
    LazyRow(
        state = scroll,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(pages) { index, label ->
            ChipTab(
                selected = index == selected,
                onClick = {
                    if (index == selected) callbacks.onTabReselected() else callbacks.onTabSelected(index)
                },
                label = label,
                backdrop = backdrop,
            )
        }
    }
}

@Composable
private fun Modifier.holdToRefresh(refresh: () -> Unit, repeatMillis: Long): Modifier {
    val latestRefresh by rememberUpdatedState(refresh)
    var holding by remember { mutableStateOf(false) }
    LaunchedEffect(holding, repeatMillis) {
        while (holding) {
            latestRefresh()
            delay(repeatMillis)
        }
    }
    return semantics {
        onLongClick("刷新当前页") { latestRefresh(); true }
    }.pointerInput(Unit) {
        awaitEachGesture {
            val pass = PointerEventPass.Initial
            val down = awaitFirstDown(requireUnconsumed = false, pass = pass)
            val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation(pass)
                true
            }
            if (released == null) {
                holding = true
                try {
                    do {
                        val event = awaitPointerEvent(pass)
                        val primary = event.changes.firstOrNull { it.id == down.id }
                        event.changes.forEach { it.consume() }
                    } while (primary?.pressed == true)
                } finally {
                    holding = false
                }
            }
        }
    }
}
