package gov.anzong.androidnga.ui.coui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView

fun couiViewShell(activity: Activity, classic: View, body: View): View {
    classic.visibility = View.GONE
    val compose = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            CouiForumPage(activity) { padding ->
                AndroidView(
                    factory = {
                        (body.parent as? ViewGroup)?.removeView(body)
                        body.visibility = View.VISIBLE
                        body
                    },
                    modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
                )
            }
        }
    }
    return FrameLayout(activity).apply {
        // Retain the inflated IDs for fragments and view bindings until Compose attaches the body.
        addView(classic, ViewGroup.LayoutParams(0, 0))
        addView(compose, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }
}
