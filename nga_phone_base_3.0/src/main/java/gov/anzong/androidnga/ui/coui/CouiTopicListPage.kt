package gov.anzong.androidnga.ui.coui

import android.app.Activity
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.Dialog
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.Fab
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Item
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.shape.CouiShape
import com.itos.coui.theme.CouiTheme
import com.justwen.androidnga.ui.compose.miuix.NgaMiuixIcons
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
        progressiveHeader = true,
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
                Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()), contentAlignment = Alignment.Center) {
                    if (state.initialLoading) {
                        CircularProgressIndicator(color = colors.accent)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.emptyText, kind = CouiTextKind.Body, color = colors.onSurfaceVariant)
                            callbacks.onRefresh?.let { refresh ->
                                Item(onClick = refresh, title = { Text(text = "重试", kind = CouiTextKind.PreferenceTitle, color = colors.accent) })
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
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + if (post == null) 12.dp else 88.dp,
                    ),
                ) {
                    itemsIndexed(rows, key = { _, row -> row.key }, contentType = { _, _ -> "topic" }) { index, row ->
                        val groupStart = rows.getOrNull(index - 1)?.pinned != row.pinned
                        val groupEnd = rows.getOrNull(index + 1)?.pinned != row.pinned
                        val delete = callbacks.deleteConfirmText
                        TopicRow(
                            row = row,
                            titleSize = state.couiTitleSizeSp ?: state.titleSizeSp,
                            groupStart = groupStart,
                            groupEnd = groupEnd,
                            modifier = Modifier.padding(top = if (groupStart && index > 0) 10.dp else 0.dp),
                            onClick = { callbacks.onClick(row) },
                            onLongClick = if (delete == null) null else {
                                { delete(row)?.let { pendingDelete = row to it }; Unit }
                            },
                        )
                    }
                }
            }
        }
        val bodyModifier = Modifier.fillMaxSize().consumeWindowInsets(padding)
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
private fun TopicRow(
    row: MiuixTopicRow,
    titleSize: Float,
    groupStart: Boolean,
    groupEnd: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val colors = CouiTheme.colorScheme
    val topCorner = if (groupStart) 18.dp else 0.dp
    val bottomCorner = if (groupEnd) 18.dp else 0.dp
    val shape = RoundedCornerShape(topCorner, topCorner, bottomCorner, bottomCorner)
    val metadataStyle = CouiTheme.typography.body.copy(
        color = colors.onSurfaceVariant,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val details = row.postDateSeconds.takeIf { it > 0 }?.let { seconds ->
        val time = DateUtils.getRelativeTimeSpanString(seconds * 1000L, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        listOfNotNull(row.author?.takeIf(String::isNotBlank), time.toString()).joinToString(" · ")
    } ?: row.people
    Column(modifier.fillMaxWidth().clip(shape).background(colors.card)) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (row.pinned) {
                    BasicText(
                        text = "置顶",
                        modifier = Modifier
                            .background(colors.accent.copy(alpha = .12f), CouiShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        style = metadataStyle.copy(color = colors.accentOnSurface, fontWeight = FontWeight.Medium),
                    )
                }
                BasicText(
                    text = row.styledTitle(colors.onSurfaceVariant, colors.accent),
                    modifier = Modifier.weight(1f).padding(start = if (row.pinned) 8.dp else 0.dp),
                    style = CouiTheme.typography.body.copy(
                        color = colors.onSurface,
                        fontSize = titleSize.sp,
                        lineHeight = (titleSize + 6f).sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = if (row.summary == null) 2 else 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            row.summary?.let {
                Text(
                    text = it,
                    kind = CouiTextKind.Description,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (details.isNotEmpty() || row.replies != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = details,
                        style = metadataStyle,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    row.replies?.let { count ->
                        Row(
                            modifier = Modifier.widthIn(min = 42.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(NgaMiuixIcons.Reply, "回复", Modifier.size(14.dp), tint = colors.onSurfaceVariant)
                            BasicText(count, Modifier.padding(start = 4.dp), style = metadataStyle)
                        }
                    }
                }
            }
        }
        if (!groupEnd) Divider(startIndent = 16.dp, endIndent = 16.dp)
    }
}
