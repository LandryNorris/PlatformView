package io.github.landrynorris.platform.view

import androidx.compose.runtime.snapshots.SnapshotStateObserver
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.UIKit.addSubview
import platform.UIKit.removeFromSuperview
import platform.UIKit.subviews

class IosViewHolder: UIView(CGRectZero.readValue()) {
    var view: UIView? = null
        internal set(value) {
            if(value !== field) {
                field = value
                subviews.filterIsInstance<UIView>().forEach { it.removeFromSuperview() }
                if(value != null) addSubview(value)
            }
        }

    private val snapshotObserver = SnapshotStateObserver { command ->
        command()
    }

    var update: (() -> Unit)? = null

    private val onCommitAffectingUpdate = { _: IosViewHolder ->
        runUpdate()
    }

    private val runUpdate: () -> Unit = {
        if (update != null) {
            snapshotObserver.observeReads(this, onCommitAffectingUpdate, update!!)
        }
    }
}
