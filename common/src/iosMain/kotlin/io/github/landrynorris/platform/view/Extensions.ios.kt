package io.github.landrynorris.platform.view

import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRect
import platform.UIKit.UIView

val CValue<CGRect>.width get() = useContents { size.width }
val CValue<CGRect>.height get() = useContents { size.height }

val UIView.width get() = frame.width
val UIView.height get() = frame.height

val CValue<CGPoint>.x get() = useContents { x }
val CValue<CGPoint>.y get() = useContents { y }
