package gov.anzong.androidnga.ui.coui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text

object CouiAboutPage {
    @JvmStatic
    fun show(activity: ComponentActivity, title: CharSequence, list: MaterialAboutList) {
        activity.setContent {
            CouiForumPage(activity, pageTitle = title.toString()) { padding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
                    contentPadding = padding,
                ) {
                    items(list.cards, key = { it.id }) { card ->
                        Group(title = card.title?.toString()
                            ?: card.titleRes.takeIf { it != 0 }?.let(activity::getString)) {
                            card.items.forEachIndexed { index, item ->
                                if (index > 0) Divider()
                                when (item) {
                                    is MaterialAboutActionItem -> {
                                        val titleText = item.text?.toString()
                                            ?: item.textRes.takeIf { it != 0 }?.let(activity::getString).orEmpty()
                                        val subtitle = item.subText?.toString()
                                            ?: item.subTextRes.takeIf { it != 0 }?.let(activity::getString)
                                        val click = item.onClickAction
                                        val hold = item.onLongClickAction
                                        Item(
                                            modifier = if (hold == null) Modifier else Modifier.combinedClickable(
                                                onClick = { click?.onClick() },
                                                onLongClick = { hold.onClick() },
                                            ),
                                            title = { Text(text = titleText, kind = CouiTextKind.PreferenceTitle) },
                                            summary = subtitle?.let { value ->
                                                { Text(text = value, kind = CouiTextKind.PreferenceSummary) }
                                            },
                                            onClick = if (hold == null && click != null) ({ click.onClick() }) else null,
                                        )
                                    }
                                    is MaterialAboutTitleItem -> Item(
                                        title = {
                                            Text(
                                                text = item.text?.toString() ?: activity.getString(item.textRes),
                                                kind = CouiTextKind.PreferenceTitle,
                                            )
                                        },
                                        summary = item.desc?.toString()?.let { value ->
                                            { Text(text = value, kind = CouiTextKind.PreferenceSummary) }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
