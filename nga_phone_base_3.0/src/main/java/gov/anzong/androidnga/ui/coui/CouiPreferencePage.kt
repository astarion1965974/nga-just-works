package gov.anzong.androidnga.ui.coui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.TwoStatePreference
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.Scaffold
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.switch.Switch
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.TopAppBarButton
import gov.anzong.androidnga.ui.miuix.EXTRA_CUSTOM_DIALOG
import gov.anzong.androidnga.ui.miuix.preferenceSections

@Composable
internal fun CouiPreferencePage(
    title: String,
    dark: Boolean,
    screen: PreferenceGroup?,
    revision: Int,
    onBack: () -> Unit,
    onClick: (Preference) -> Unit,
) {
    // The hidden PreferenceGroupAdapter drives visibility, summaries and checked state.
    val sections = remember(screen, revision) { screen?.let(::preferenceSections).orEmpty() }
    CouiTheme(darkTheme = dark) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = title, kind = CouiTextKind.Toolbar) },
                    navigationIcon = {
                        TopAppBarButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(androidx.appcompat.R.string.abc_action_bar_up_description),
                                tint = LocalCouiContentColor.current,
                            )
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                items(sections) { section ->
                    Group(title = section.title) {
                        section.rows.forEachIndexed { index, preference ->
                            CouiPreferenceRow(preference, revision, onClick)
                            if (index < section.rows.lastIndex) Divider()
                        }
                    }
                }
                item { Box(Modifier.padding(bottom = CouiPageDefaults.groupSpacing)) }
            }
        }
    }
}

@Composable
private fun CouiPreferenceRow(
    preference: Preference,
    revision: Int,
    onClick: (Preference) -> Unit,
) {
    val title = remember(preference, revision) { preference.title?.toString().orEmpty() }
    val summary = remember(preference, revision) { preference.summary?.toString()?.takeIf(String::isNotEmpty) }
    val enabled = remember(preference, revision) { preference.isEnabled }
    val colors = CouiPageDefaults.themeColors()
    val dark = CouiTheme.isDark
    val titleContent: @Composable () -> Unit = {
        Text(text = title, kind = CouiTextKind.PreferenceTitle, color = if (enabled) colors.title else colors.disabled)
    }
    val summaryContent: (@Composable () -> Unit)? = summary?.let { value ->
        { Text(text = value, kind = CouiTextKind.PreferenceSummary, color = if (enabled) colors.summary else colors.disabled) }
    }
    if (preference is TwoStatePreference) {
        val checked = remember(preference, revision) { preference.isChecked }
        val interaction = remember(preference) { MutableInteractionSource() }
        val change: (Boolean) -> Unit = { next ->
            if (preference.isChecked != next && preference.callChangeListener(next)) {
                preference.isChecked = next
            }
        }
        Item(
            modifier = Modifier.couiButtonFeedback(dark, interaction, enabled, list = true)
                .toggleable(checked, interaction, indication = null, enabled = enabled, role = Role.Switch, onValueChange = change),
            enabled = enabled,
            title = titleContent,
            summary = summaryContent,
            trailing = {
                Switch(
                    modifier = Modifier.padding(start = 16.dp),
                    checked = checked,
                    onCheckedChange = null,
                    onDragCheckedChange = change,
                    enabled = enabled,
                    interactionSource = interaction,
                )
            },
        )
        return
    }
    val choices = remember(preference, revision) {
        if (preference is ListPreference && !preference.extras.getBoolean(EXTRA_CUSTOM_DIALOG, false)) {
            val entries = preference.entries.orEmpty()
            val values = preference.entryValues.orEmpty()
            if (entries.size == values.size) {
                entries.indices.map { index ->
                    val value = values[index].toString()
                    CouiMenuItem(value, entries[index].toString(), selected = value == preference.value)
                }
            } else emptyList()
        } else emptyList()
    }
    if (choices.isEmpty()) {
        Item(
            enabled = enabled,
            title = titleContent,
            summary = summaryContent,
            onClick = { onClick(preference) },
            trailing = { PreferenceChevron() },
        )
        return
    }
    val listPreference = preference as ListPreference
    val anchor = rememberCouiMenuAnchor()
    var expanded by remember(preference) { mutableStateOf(false) }
    Box {
        Item(
            modifier = Modifier.couiMenuAnchor(anchor),
            enabled = enabled,
            title = titleContent,
            onClick = { anchor.open { expanded = true } },
            trailing = {
                Text(
                    text = choices.firstOrNull { it.selected }?.text.orEmpty(),
                    kind = CouiTextKind.PreferenceValue,
                    color = if (enabled) colors.summary else colors.disabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                PreferenceChevron()
            },
        )
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = choices,
            anchor = anchor,
            onItemClick = { item ->
                expanded = false
                if (listPreference.value != item.value && listPreference.callChangeListener(item.value)) {
                    listPreference.value = item.value
                }
            },
        )
    }
}

@Composable
private fun PreferenceChevron() {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = CouiPageDefaults.themeColors().summary,
    )
}
