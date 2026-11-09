package gov.anzong.androidnga.activity.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itos.coui.checkbox.Checkbox
import com.itos.coui.chip.ChipTabRow
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.Scaffold
import com.itos.coui.radiobutton.RadioButton
import com.itos.coui.search.SearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.TopAppBarSurface
import gov.anzong.androidnga.ui.coui.CouiBackButton
import gov.anzong.androidnga.ui.coui.CouiSolidHeader
import sp.phone.theme.ThemeManager

@Composable
internal fun CouiSearchPage(viewModel: SearchViewModel, onBack: () -> Unit, onSearch: (String) -> Unit) {
    val mode by viewModel.searchMode.observeAsState(SearchViewModel.SEARCH_MODE_TOPIC)
    val history by viewModel.keyList.observeAsState(emptyList())
    val scopes = remember(viewModel) { viewModel.searchData.map { (label, key) -> CouiChipTabItem(key, label) } }
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(true) }
    val search = rememberCouiSearchBarState(expanded)
    val keyboard = LocalSoftwareKeyboardController.current
    val submit: (String) -> Unit = { text ->
        if (text.isNotBlank()) {
            keyboard?.hide()
            onSearch(text)
        }
    }
    val setExpanded: (Boolean) -> Unit = { open ->
        expanded = open
        if (!open) query = ""
    }
    BackHandler(enabled = expanded) { setExpanded(false) }
    CouiTheme(darkTheme = ThemeManager.getInstance().isNightMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            topBar = {
                CouiSolidHeader { backdrop ->
                    Column {
                        TopAppBarSurface(backdrop = backdrop, gradientColor = Color.Transparent) {
                            Row(
                                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CouiBackButton(onBack)
                                SearchBar(
                                    modifier = Modifier.weight(1f),
                                    state = search,
                                    query = query,
                                    onQueryChange = { query = it },
                                    onExpandedChange = setExpanded,
                                    placeholder = "搜索",
                                    cancelText = "取消",
                                    clearDescription = "清除搜索词",
                                    onSearch = { submit(query) },
                                )
                            }
                        }
                        ChipTabRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            items = scopes,
                            selectedKey = mode,
                            onSelectedKeyChange = { viewModel.searchMode.value = it as Int },
                            backdrop = backdrop,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                    .verticalScroll(rememberScrollState()).padding(bottom = CouiPageDefaults.groupSpacing),
            ) {
                when (mode) {
                    SearchViewModel.SEARCH_MODE_TOPIC -> {
                        var selected by remember { mutableStateOf(viewModel.searchTopicMode) }
                        var withContent by remember { mutableStateOf(viewModel.searchTopicWithContent) }
                        SearchOptions(viewModel.searchTopicData, selected, {
                            selected = it
                            viewModel.searchTopicMode = it
                        }) {
                            Divider()
                            val source = remember { MutableInteractionSource() }
                            Item(
                                modifier = Modifier.couiButtonFeedback(CouiTheme.isDark, source, true, list = true)
                                    .toggleable(withContent, source, null, role = Role.Checkbox) {
                                        withContent = it
                                        viewModel.searchTopicWithContent = it
                                    },
                                title = { Text(text = "包括正文", kind = CouiTextKind.PreferenceTitle) },
                                trailing = { Checkbox(checked = withContent) },
                            )
                        }
                    }
                    SearchViewModel.SEARCH_MODE_USER -> {
                        var selected by remember { mutableStateOf(viewModel.searchUserMode) }
                        SearchOptions(viewModel.searchUserData, selected, {
                            selected = it
                            viewModel.searchUserMode = it
                        })
                    }
                }
                if (history.isNotEmpty()) {
                    SearchHistory(history, submit, viewModel::deleteHistory, viewModel::clearHistory)
                }
            }
        }
    }
}

@Composable
private fun SearchOptions(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    extra: @Composable () -> Unit = {},
) {
    Group(title = "搜索选项") {
        Column(Modifier.selectableGroup()) {
            options.forEachIndexed { index, (label, value) ->
                if (index > 0) Divider()
                val source = remember { MutableInteractionSource() }
                Item(
                    modifier = Modifier.couiButtonFeedback(CouiTheme.isDark, source, true, list = true)
                        .selectable(value == selected, source, null, role = Role.RadioButton) { onSelect(value) },
                    title = { Text(text = label, kind = CouiTextKind.PreferenceTitle) },
                    trailing = { RadioButton(selected = value == selected) },
                )
            }
        }
        extra()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistory(
    history: List<String>,
    onSearch: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
) {
    val colors = CouiTheme.colorScheme
    Group(
        title = "搜索历史",
        headerActions = {
            Text(text = "全部清除", kind = CouiTextKind.Description, color = colors.accent, modifier = Modifier.clickable(onClick = onClear).padding(8.dp))
        },
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            history.forEach { term ->
                Row(
                    modifier = Modifier.clip(CircleShape).background(colors.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = term,
                        kind = CouiTextKind.Body,
                        modifier = Modifier.weight(1f, fill = false).clickable { onSearch(term) }.padding(12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { onDelete(term) }) {
                        Icon(Icons.Default.Close, "删除搜索记录：$term", tint = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
