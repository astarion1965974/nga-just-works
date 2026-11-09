package gov.anzong.androidnga.ui.coui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.Dialog
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.Fab
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Item
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.theme.CouiTheme
import gov.anzong.androidnga.ui.miuix.MiuixTopicListCallbacks
import gov.anzong.androidnga.ui.miuix.MiuixTopicListState
import gov.anzong.androidnga.ui.miuix.MiuixTopicRow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CouiTopicListPage(activity: Activity, state: MiuixTopicListState, callbacks: MiuixTopicListCallbacks) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val rows = state.rows
    val post = callbacks.fab
    var pendingDelete by remember { mutableStateOf<Pair<MiuixTopicRow, String>?>(null) }
    LaunchedEffect(state.scrollToTopRequest) {
        if (state.scrollToTopRequest > 0) listState.scrollToItem(0)
    }
    LaunchedEffect(listState, rows.size, callbacks) {
        snapshotFlow {
            rows.isNotEmpty() && state.hasNextPage && !state.refreshing &&
                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) >= rows.lastIndex
        }.filter { it }.collect { callbacks.onLoadNextPage() }
    }
    CouiForumPage(
        activity = activity,
        onTitleClick = callbacks.onTitleClick?.let { refresh ->
            {
                scope.launch { listState.scrollToItem(0) }
                refresh()
            }
        },
        floatingAction = if (post == null) null else {
            {
                Fab(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    renderer = remember { CouiFloatingRenderer.standalone() },
                    onClick = post,
                ) {
                    Icon(Icons.Default.Edit, "发帖", tint = Color.White)
                }
            }
        },
    ) { padding ->
        val colors = CouiTheme.colorScheme
        val body: @Composable () -> Unit = {
            if (rows.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()), contentAlignment = Alignment.Center) {
                    if (state.initialLoading) {
                        CircularProgressIndicator(color = colors.accent)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.emptyText, color = colors.onSurfaceVariant)
                            callbacks.onRefresh?.let { refresh ->
                                Item(onClick = refresh, title = { Text(text = "重试", color = colors.accent) })
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = CouiPageDefaults.horizontalMargin,
                        end = CouiPageDefaults.horizontalMargin,
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + if (post == null) 12.dp else 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(rows, key = { it.key }, contentType = { "topic" }) { row ->
                        val delete = callbacks.deleteConfirmText
                        TopicRow(
                            row, state.titleSizeSp,
                            onClick = { callbacks.onClick(row) },
                            onLongClick = if (delete == null) null else {
                                { delete(row)?.let { pendingDelete = row to it }; Unit }
                            },
                        )
                    }
                }
            }
        }
        val bodyModifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).consumeWindowInsets(padding)
        val refresh = callbacks.onRefresh
        if (refresh == null) {
            Box(bodyModifier) { body() }
        } else {
            PullToRefreshBox(
                isRefreshing = state.refreshing && !state.initialLoading,
                onRefresh = refresh,
                modifier = bodyModifier,
            ) { body() }
        }
        pendingDelete?.let { (row, message) ->
            Dialog(
                title = "删除",
                message = message,
                onDismissRequest = { pendingDelete = null },
                dismissAction = CouiDialogAction("取消", { pendingDelete = null }),
                confirmAction = CouiDialogAction("删除", {
                    pendingDelete = null
                    callbacks.onDelete?.invoke(row)
                }),
            )
        }
    }
}

@Composable
private fun TopicRow(row: MiuixTopicRow, titleSize: Float, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    val colors = CouiTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().clip(CouiPageDefaults.shape).background(colors.card)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        androidx.compose.foundation.text.BasicText(
            text = row.styledTitle(colors.onSurfaceVariant, colors.accent),
            style = CouiTheme.typography.body.copy(color = colors.onSurface, fontSize = titleSize.sp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        row.summary?.let {
            Text(text = it, color = colors.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        val details = listOfNotNull(row.people.takeIf { it.isNotEmpty() }, row.replies).joinToString("  ")
        if (details.isNotEmpty()) {
            Text(text = details, kind = CouiTextKind.Description, color = colors.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
