package gov.anzong.androidnga.activity.compose.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu as DrawerMenu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
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
import com.justwen.androidnga.ui.compose.widget.OptionMenuData
import com.justwent.androidnga.bu.UserManager
import gov.anzong.androidnga.ui.coui.NgaMistTheme
import gov.anzong.androidnga.ui.coui.ngaMistBrush
import gov.anzong.androidnga.activity.compose.board.ForumBoardView
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.activity.compose.board.LocalCouiBoardStyle
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch
import sp.phone.common.User
import sp.phone.theme.ThemeManager

@Composable
internal fun CouiHomeScreen(
    title: String,
    menu: List<OptionMenuData>,
    forumBoardViewModel: ForumBoardViewModel,
    entries: List<MiuixDrawerEntry>,
    footer: MiuixDrawerEntry,
    onAccountClick: (User?, Int) -> Unit,
) {
    NgaMistTheme(dark = ThemeManager.getInstance().isNightMode) {
        val drawer = rememberHomeDrawerState()
        val gestures = remember { HomeDrawerGestureState() }
        val scope = rememberCoroutineScope()
        HomeNavigationDrawer(
            drawerState = drawer,
            gestureState = gestures,
            drawerContent = {
                CouiDrawer(entries + footer, onAccountClick) { action ->
                    scope.launch { drawer.close() }
                    action()
                }
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        colors = CouiTopAppBarDefaults.colors(backgroundColor = Color.Transparent),
                        title = { Text(text = title, kind = CouiTextKind.Toolbar) },
                        navigationIcon = {
                            TopAppBarButton(onClick = { scope.launch { drawer.open() } }) {
                                Icon(Icons.Default.DrawerMenu, "打开侧栏", tint = LocalCouiContentColor.current)
                            }
                        },
                        actions = {
                            val search = menu.firstOrNull { it.type == OptionMenuData.OPTION_MENU_TYPE_ALWAYS_SHOW }
                            if (search != null) {
                                TopAppBarButton(onClick = search.action) {
                                    Icon(Icons.Default.Search, search.title, tint = LocalCouiContentColor.current)
                                }
                            }
                            val more = menu.filter { it != search }
                            if (more.isNotEmpty()) {
                                val anchor = rememberCouiMenuAnchor()
                                var expanded by remember { mutableStateOf(false) }
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
                                    items = more.map { CouiMenuItem(it, it.title.orEmpty()) },
                                    onItemClick = {
                                        expanded = false
                                        it.value.action()
                                    },
                                )
                            }
                        },
                    )
                },
            ) { padding ->
                val tabBackdrop = rememberCouiBackdrop()
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val density = LocalDensity.current
                    val viewportHeight = with(density) { maxHeight.toPx() }
                    val topInset = with(density) { padding.calculateTopPadding().toPx() }
                    val dark = CouiTheme.isDark
                    Box(Modifier.matchParentSize().background(ngaMistBrush(dark, false, viewportHeight)))
                    Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                        Box(
                            Modifier.fillMaxWidth().height(52.dp)
                                .couiBackdrop(tabBackdrop)
                                .background(ngaMistBrush(dark, false, viewportHeight, topInset)),
                        )
                        CompositionLocalProvider(LocalCouiBoardStyle provides true) {
                            ForumBoardView(
                                forumBoardViewModel = forumBoardViewModel,
                                pagerModifier = Modifier.onGloballyPositioned { gestures.pagerBounds = it.boundsInRoot() },
                                onPagerInteractionChanged = {
                                    gestures.settledPage = it?.settledPage ?: -1
                                    gestures.pagerSettled = it?.isScrollInProgress == false
                                },
                                onFavoriteReorderActiveChanged = { gestures.favoriteReorderActive = it },
                                tabBackdrop = tabBackdrop,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouiDrawer(
    entries: List<MiuixDrawerEntry>,
    onAccountClick: (User?, Int) -> Unit,
    openDestination: (() -> Unit) -> Unit,
) {
    val users by UserManager.getUserListLiveData().observeAsState(emptyList())
    val active by UserManager.getActiveIndexLiveData().observeAsState(0)
    val user = users.getOrNull(active ?: 0)
    val colors = CouiTheme.colorScheme
    Column(
        Modifier.width(300.dp).fillMaxHeight().background(colors.background)
            .statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(bottom = CouiPageDefaults.groupSpacing),
    ) {
        Group {
            Item(
                iconSize = 48.dp,
                icon = {
                    val fallback = painterResource(com.justwen.androidnga.module.message.R.drawable.default_avatar)
                    Image(
                        painter = if (user?.avatarUrl.isNullOrEmpty()) fallback else {
                            rememberAsyncImagePainter(user?.avatarUrl, placeholder = fallback, error = fallback)
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                    )
                },
                title = { Text(text = user?.nickName ?: "未登录", kind = CouiTextKind.PreferenceTitle) },
                summary = { Text(text = user?.let { "UID ${it.userId}" } ?: "点击登录账号", kind = CouiTextKind.PreferenceSummary) },
                onClick = { openDestination { onAccountClick(user, users.size) } },
            )
            if (users.size > 1) {
                Divider()
                val anchor = rememberCouiMenuAnchor()
                var expanded by remember { mutableStateOf(false) }
                Item(
                    modifier = Modifier.couiMenuAnchor(anchor),
                    title = { Text(text = "切换账号", kind = CouiTextKind.PreferenceTitle) },
                    onClick = { anchor.open { expanded = true } },
                )
                Menu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    anchor = anchor,
                    items = users.mapIndexed { index, account ->
                        CouiMenuItem(index, accountLabel(account), selected = index == active)
                    },
                    onItemClick = {
                        expanded = false
                        if (it.value != active) UserManager.setActiveIndex(it.value)
                    },
                )
            }
        }
        Group {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    Divider(startIndent = CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
                }
                Item(
                    icon = { Icon(entry.icon, null, tint = colors.onSurface) },
                    title = { Text(text = entry.title, kind = CouiTextKind.PreferenceTitle) },
                    trailing = {
                        entry.badge?.let { Text(text = it, kind = CouiTextKind.PreferenceValue, color = colors.accent) }
                    },
                    onClick = { openDestination(entry.onClick) },
                )
            }
        }
    }
}
