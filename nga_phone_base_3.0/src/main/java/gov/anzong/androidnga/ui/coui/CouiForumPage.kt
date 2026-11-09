package gov.anzong.androidnga.ui.coui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.itos.coui.topappbar.TopAppBarButton
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
    onTitleClick: (() -> Unit)? = null,
    extraTopBar: (@Composable (CouiBackdrop) -> Unit)? = null,
    floatingAction: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    CouiTheme(darkTheme = ThemeManager.getInstance().isNightMode) {
        val title = (activity as? MiuixChromeOwner)?.miuixChrome?.title.orEmpty()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CouiSolidHeader { backdrop ->
                    Column {
                        TopAppBar(
                            backdrop = backdrop,
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
            },
            floatingActionButton = floatingAction,
            content = content,
        )
    }
}

@Composable
private fun CouiForumMenu(activity: Activity) {
    val entries = rememberHostOptionsMenu(activity)
    val search = entries.firstOrNull { it.item.itemId == R.id.menu_search && it.asAction }
    if (search != null) {
        TopAppBarButton(onClick = { activity.performHostMenuItem(search.item) }, enabled = search.enabled) {
            Icon(Icons.Default.Search, search.title, tint = LocalCouiContentColor.current)
        }
    }
    val overflow = entries.filter { it != search }
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
