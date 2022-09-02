package io.github.landrynorris.platform.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import interop.UIViewWithOverridesProtocol
import kotlinx.cinterop.*
import org.jetbrains.skia.Bitmap
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreGraphics.*
import platform.QuartzCore.CALayer
import platform.UIKit.*
import kotlin.system.measureTimeMicros

var invalidationCount = mutableStateOf(0)

@Composable
fun PlatformViewUI(factory: Factory, modifier: Modifier = Modifier, update: () -> Unit = {}) {
    val view = remember { createViewHolder(factory) }
    val density = LocalDensity.current.density

    Box(
        modifier = modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size

            view.setFrame(CGRectMake(
                (location.x / density).toDouble(),
                (location.y / density).toDouble(),
                (size.width / density).toDouble(),
                (size.height / density).toDouble()
            ))
            val content = view.subviews.firstOrNull() as? UIView ?: error("No UIView subview set")
            content.setFrame(CGRectMake(0.0, 0.0, view.width, view.height))
        }.drawView(view).pointerInterop(view),
    )
}

fun createViewHolder(factory: Factory): UIView {
    val result = object: UIView(CGRectZero.readValue()), UIViewWithOverridesProtocol {
        override fun drawRect(aRect: CValue<CGRect>) {
            if(!isDrawing) invalidationCount.value++
            println("Drawing Rect $isDrawing")
        }

        override fun layoutSubviews() {
            println("Laying out subview")
        }

        override fun setNeedsDisplay() {
            invalidationCount.value++
            println("SetNeedsDisplay called")
        }

        override fun setNeedsLayout() {
            invalidationCount.value++
            println("SetNeedsLayout called")
        }

        override fun setNeedsDisplayInRect(rect: CValue<CGRect>) {
            println("SetNeedsDisplayInRect called")
        }

        override fun displayIfNeeded() {
            println("DisplayIfNeeded called")
        }
    }

    val subview = factory()
    result.userInteractionEnabled = true
    result.addSubview(subview)
    result.contentMode = UIViewContentMode.UIViewContentModeRedraw
    return result
}

private var isDrawing = false

@Composable
fun Modifier.drawView(view: PlatformView): Modifier {

    return drawBehind {
        drawIntoCanvas { canvas ->
            val w = view.width().toInt()
            val h = view.height().toInt()
            val bitmap = ImageBitmap(w, h)
            invalidationCount.value //read the variable, so we can recompose when this value changes.
            lateinit var image: UIImage
            val createImageTime = measureTimeMicros {
                image = view.toUIImage()
            }
            val renderTime = measureTimeMicros {
                image.render(bitmap.asSkiaBitmap())
            }
            println("Creating image took $createImageTime us. Rendering to bitmap took $renderTime us")
            canvas.drawImage(bitmap, Offset.Zero, Paint())
        }
    }
}

fun PlatformView.width() = bounds.useContents { size.width }
fun PlatformView.height() = bounds.useContents { size.height }

fun PlatformView.toUIImage(): UIImage {
    UIGraphicsBeginImageContext(CGSizeMake(width(), height()))
    isDrawing = true
    drawViewHierarchyInRect(CGRectMake(0.0, 0.0, width(), height()), true)
    isDrawing = false
    val image = UIGraphicsGetImageFromCurrentImageContext() ?: error("Unable to get image")
    UIGraphicsEndImageContext()
    return image
}

fun UIImage.render(bitmap: Bitmap) {
    val dataProvider = CGImageGetDataProvider(CGImage)
    val imageData = CGDataProviderCopyData(dataProvider)
    //val bytes = imageData?.readBytes(size.useContents { width }.toInt() * size.useContents { height }.toInt())
    val raw = CFDataGetBytePtr(imageData)
    val size = CFDataGetLength(imageData).toInt()
    val pixels = raw?.readBytes(size)
    bitmap.installPixels(pixels)
}
