package io.github.landrynorris.platform.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import kotlinx.cinterop.*
import org.jetbrains.skia.Bitmap
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreGraphics.*
import platform.UIKit.*

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
    val result = UIView()
    val subview = factory()
    result.userInteractionEnabled = true
    result.addSubview(subview)
    return result
}

fun Modifier.drawView(view: PlatformView): Modifier =
    drawBehind {
        drawIntoCanvas { canvas ->
            val bitmap = ImageBitmap(view.width().toInt(), view.height().toInt())
            view.toUIImage().render(bitmap.asSkiaBitmap())
            canvas.drawImage(bitmap, Offset.Zero, Paint())
        }
    }

fun PlatformView.width() = bounds.useContents { size.width }
fun PlatformView.height() = bounds.useContents { size.height }

fun PlatformView.toUIImage(): UIImage {
    UIGraphicsBeginImageContext(CGSizeMake(width(), height()))
    drawViewHierarchyInRect(CGRectMake(0.0, 0.0, width(), height()), true)
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
    val pixels = ByteArray(size) { index -> raw?.get(index)?.toByte() ?: 0 }
    bitmap.installPixels(pixels)
}
