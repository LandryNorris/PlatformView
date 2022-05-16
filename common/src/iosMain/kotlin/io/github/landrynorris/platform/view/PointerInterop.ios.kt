package io.github.landrynorris.platform.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.IntSize
import kotlinx.cinterop.CValue
import org.jetbrains.skiko.SkikoTouchEvent
import platform.CoreGraphics.CGPoint
import platform.UIKit.*

fun Modifier.pointerInterop(view: UIView): Modifier {
    val filter = PointerInteropFilter()
    val targetView = view.subviews.filterIsInstance<UIView>().first()
    filter.touchListener = object: TouchListener {
        override fun touchesBegan(touches: List<UITouch>) {
            println("TouchesBegan called")
            targetView.touchesBegan(touches.toSet(), null)
            view.viewToDeliverTo(touches.first().locationInView(view))
            if(targetView is UIControl) targetView.sendActionsForControlEvents(UIControlEventTouchDown)
        }
        override fun touchesMoved(touches: List<UITouch>) {
            targetView.touchesMoved(touches.toSet(), null)
            if(targetView is UIControl) targetView.sendActionsForControlEvents(UIControlEventTouchDragInside)
        }
        override fun touchesEnded(touches: List<UITouch>) {
            targetView.touchesEnded(touches.toSet(), null)
            if(targetView is UIControl) targetView.sendActionsForControlEvents(UIControlEventTouchUpInside)
        }
    }
    return this.then(filter)
}

interface TouchListener {
    fun touchesBegan(touches: List<UITouch>)
    fun touchesMoved(touches: List<UITouch>)
    fun touchesEnded(touches: List<UITouch>)
}

class PointerInteropFilter: PointerInputModifier {
    var touchListener: TouchListener? = null
    private val activeTouches = arrayListOf<UITouch>()

    override val pointerInputFilter = object: PointerInputFilter() {
        override fun onCancel() {

        }

        override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
            if(pass == PointerEventPass.Final) dispatchToView(pointerEvent)
        }

        private fun dispatchToView(pointerEvent: PointerEvent) {
            val uiTouch = pointerEvent.toUITouch() ?: return

            //UIApplication.sharedApplication.sendEvent()
            when (pointerEvent.type) {
                PointerEventType.Press -> {
                    activeTouches.add(uiTouch)
                    touchListener?.touchesBegan(activeTouches)
                    //UIApplication.sharedApplication.touchesBegan(setOf(uiTouch), null)
                }
                PointerEventType.Release -> {
                    touchListener?.touchesEnded(activeTouches)
                    activeTouches.removeAll { it == uiTouch }
                }
                PointerEventType.Move -> {
                    touchListener?.touchesMoved(activeTouches)
                }
            }
        }

        private fun PointerEvent.typeString() = when(type) {
            PointerEventType.Press -> "press"
            PointerEventType.Release -> "Release"
            PointerEventType.Move -> "Move"
            PointerEventType.Enter -> "Enter"
            PointerEventType.Exit -> "Exit"
            else -> "not defined"
        }
    }
}

private fun PointerEvent.toUITouch(): UITouch? {
    return (nativeEvent as? SkikoTouchEvent)?.platform
}

fun UIView.viewToDeliverTo(location: CValue<CGPoint>) {
    val result = hitTest(location, null)
    println("Found view that was pressed $result")
}

/*

Notes on iOS touch events:

Check for isMultiTouchEnabled. If this is false, we should only track the first touch we find.

If two of the same touch events occur simultaneously, there will be a single call with a size of 2.

 */

