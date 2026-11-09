package gov.anzong.androidnga.ui.coui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.Scaffold
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.CouiTopAppBarDefaults
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.TopAppBarSurface
import com.justwen.androidnga.ui.compose.miuix.MiuixChromeOwner
import com.justwen.androidnga.ui.compose.miuix.performHostMenuItem
import com.justwen.androidnga.ui.compose.miuix.rememberHostOptionsMenu
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.BaseActivity
import sp.phone.theme.ThemeManager

fun isCouiForumHost(activity: Activity?): Boolean = (activity as? BaseActivity)?.isCouiPage == true

@Composable
internal fun CouiSolidHeader(content: @Composable (CouiBackdrop) -> Unit) {
    val backdrop = rememberCouiBackdrop()
    val color = CouiTheme.colorScheme.background
    Box(Modifier.fillMaxWidth().background(color)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(color))
        content(backdrop)
    }
}

@Composable
internal fun CouiBackButton(onBack: () -> Unit) {
    TopAppBarButton(onClick = onBack) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description),
            tint = LocalCouiContentColor.current,
        )
    }
}

@Composable
internal fun CouiForumPage(
    activity: Activity,
    pageTitle: String? = null,
    onTitleClick: (() -> Unit)? = null,
    extraTopBar: (@Composable (CouiBackdrop) -> Unit)? = null,
    progressiveHeader: Boolean = false,
    floatingAction: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    CouiTheme(darkTheme = ThemeManager.getInstance().isNightMode) {
        val title = pageTitle ?: (activity as? MiuixChromeOwner)?.miuixChrome?.title.orEmpty()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (progressiveHeader) {
                    val chipBackdrop = rememberCouiBackdrop()
                    TopAppBarSurface(bottomExtension = 24.dp) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().statusBarsPadding().height(52.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CouiBackButton {
                                    (activity as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                                        ?: activity.finish()
                                }
                                Text(
                                    text = title,
                                    kind = CouiTextKind.Toolbar,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(start = 12.dp).then(
                                        if (onTitleClick == null) Modifier else Modifier.clickable(onClick = onTitleClick),
                                    ),
                                )
                                CouiForumMenu(activity)
                            }
                            if (extraTopBar != null) {
                                Box {
                                    Box(Modifier.matchParentSize().couiBackdrop(chipBackdrop)
                                        .background(CouiTheme.colorScheme.background.copy(alpha = 0.12f)))
                                    extraTopBar(chipBackdrop)
                                }
                            }
                        }
                    }
                } else {
                CouiSolidHeader { backdrop ->
                    Column {
                        TopAppBar(
                            backdrop = backdrop,
                            colors = CouiTopAppBarDefaults.colors(backgroundColor = Color.Transparent),
                            title = {
                                Text(
                                    text = title,
                                    kind = CouiTextKind.Toolbar,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = if (onTitleClick == null) Modifier else Modifier.clickable(onClick = onTitleClick),
                                )
                            },
                            navigationIcon = {
                                CouiBackButton {
                                    (activity as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                                        ?: activity.finish()
                                }
                            },
                            actions = { CouiForumMenu(activity) },
                        )
                        extraTopBar?.invoke(backdrop)
                    }
                }
                }
            },
            floatingActionButton = floatingAction,
            content = content,
        )
    }
}

@Composable
internal fun CouiForumMenu(activity: Activity) {
    val entries = rememberHostOptionsMenu(activity)
    val search = entries.firstOrNull { it.item.itemId == R.id.menu_search && it.asAction }
    if (search != null) {
        TopAppBarButton(onClick = { activity.performHostMenuItem(search.item) }, enabled = search.enabled) {
            Icon(Icons.Default.Search, search.title, tint = LocalCouiContentColor.current)
        }
    }
    val send = entries.firstOrNull { it.item.itemId == R.id.send }
    if (send != null) {
        TopAppBarButton(
            onClick = { activity.performHostMenuItem(send.item) },
            enabled = send.enabled,
            text = true,
        ) {
            Text(text = send.title, kind = CouiTextKind.Toolbar, color = LocalCouiContentColor.current)
        }
    }
    val overflow = entries.filter { it != search && it != send }
    if (overflow.isEmpty()) return
    val anchor = rememberCouiMenuAnchor()
    var expanded by remember { mutableStateOf(false) }
    Box {
        TopAppBarButton(
            modifier = Modifier.couiMenuAnchor(anchor),
            onClick = { anchor.open { expanded = true } },
        ) {
            Icon(Icons.Default.MoreVert, "更多选项", tint = LocalCouiContentColor.current)
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            anchor = anchor,
            items = overflow.map { CouiMenuItem(it.item, it.title, enabled = it.enabled) },
            onItemClick = {
                expanded = false
                activity.performHostMenuItem(it.value)
            },
        )
    }
}
