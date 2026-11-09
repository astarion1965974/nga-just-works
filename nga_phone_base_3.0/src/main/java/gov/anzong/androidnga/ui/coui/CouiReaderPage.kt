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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import gov.anzong.androidnga.R
import androidx.compose.ui.viewinterop.AndroidView
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.chip.ChipTab
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.Fab
import gov.anzong.androidnga.ui.miuix.MiuixReaderCallbacks
import gov.anzong.androidnga.ui.miuix.MiuixReaderTabsState
import kotlinx.coroutines.delay
import java.util.WeakHashMap

@Composable
internal fun CouiReaderPage(activity: Activity, content: View, tabs: MiuixReaderTabsState, callbacks: MiuixReaderCallbacks) {
    val reply = callbacks.onReply
    CouiForumPage(
        activity = activity,
        progressiveHeader = true,
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
        val top = with(LocalDensity.current) { padding.calculateTopPadding().roundToPx() }
        val originalPadding = remember(content) { WeakHashMap<RecyclerView, Int>() }
        DisposableEffect(content, top) {
            // Returns true when a list's padding had to change. Called before every draw so the list is
            // re-anchored in the same frame and the user never sees it jump from the old position.
            fun update(view: View): Boolean {
                if (view is RecyclerView && view.id == R.id.list) {
                    val base = originalPadding.getOrPut(view) { view.paddingTop }
                    if (view.paddingTop == base + top) return false
                    // LinearLayoutManager keeps laid-out rows at their absolute position when padding changes,
                    // so re-anchor the first visible row relative to the new padding. This keeps both the default
                    // "open at floor 1" position and pid/notification anchor jumps intact.
                    val layout = view.layoutManager as? LinearLayoutManager
                    val first = layout?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
                    val offset = if (first == RecyclerView.NO_POSITION) null else {
                        layout?.findViewByPosition(first)?.let { layout.getDecoratedTop(it) - view.paddingTop }
                    }
                    view.setPadding(view.paddingLeft, base + top, view.paddingRight, view.paddingBottom)
                    view.clipToPadding = false
                    if (layout != null && offset != null) layout.scrollToPositionWithOffset(first, offset)
                    // The floor list runs under the blurred top bar; move the refresh spinner below it.
                    generateSequence(view.parent) { it.parent }.filterIsInstance<SwipeRefreshLayout>().firstOrNull()?.let { refresh ->
                        val distance = (64 * view.resources.displayMetrics.density).toInt()
                        refresh.setProgressViewOffset(false, top - refresh.progressCircleDiameter, top + distance)
                    }
                    return true
                }
                var changed = false
                if (view is ViewGroup) {
                    for (index in 0 until view.childCount) changed = update(view.getChildAt(index)) || changed
                }
                return changed
            }
            update(content)
            // Skip the frame in which padding changed; the next frame is drawn already re-anchored.
            val listener = android.view.ViewTreeObserver.OnPreDrawListener { !update(content) }
            content.viewTreeObserver.addOnPreDrawListener(listener)
            onDispose {
                if (content.viewTreeObserver.isAlive) content.viewTreeObserver.removeOnPreDrawListener(listener)
                originalPadding.forEach { (view, base) ->
                    view.setPadding(view.paddingLeft, base, view.paddingRight, view.paddingBottom)
                }
                originalPadding.clear()
            }
        }
        AndroidView(
            factory = {
                (content.parent as? ViewGroup)?.removeView(content)
                content.visibility = View.VISIBLE
                content
            },
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).consumeWindowInsets(padding),
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
            val refresh = callbacks.onReplyLongPressRepeat
            ChipTab(
                modifier = if (index == selected && refresh != null) {
                    Modifier.holdToRefresh(refresh, callbacks.longPressRepeatMillis)
                } else Modifier,
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
