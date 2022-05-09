package io.github.landrynorris.platform.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.cinterop.*
import org.jetbrains.skia.Bitmap
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreGraphics.*
import platform.UIKit.*

@Composable
fun PlatformViewUI(factory: Factory) {
    val view = remember { factory() }
    Box(modifier = Modifier.drawView(view))
}

fun Modifier.drawView(view: PlatformView): Modifier =
    drawBehind {
        drawIntoCanvas { canvas ->
            val bitmap = ImageBitmap(view.width().toInt(), view.height().toInt())
            println("Created Bitmap")
            val image = view.toUIImage()
            println("Created UIImage")
            val skiaBitmap = bitmap.asSkiaBitmap()
            image.render(skiaBitmap)
            //image.render(bitmap.asSkiaBitmap())
            println("Rendered Image to Bitmap")
            //val buffer = IntArray(view.width().toInt()*view.height().toInt())
            //bitmap.readPixels(buffer)
            //println("Number of non-zero pixels: ${buffer.count { it != 0 }}, size is ${buffer.size}")
            canvas.drawImage(bitmap, Offset.Zero, Paint())
            println("Drew bitmap to canvas")
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
    println("Found array with size $size")
    val pixels = ByteArray(size) { index -> raw?.get(index)?.toByte() ?: 0 }
    bitmap.installPixels(pixels)
}
