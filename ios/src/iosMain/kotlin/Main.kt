import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Application
import io.github.landrynorris.platform.view.*
import kotlinx.cinterop.*
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreImage.CIColor.Companion.redColor
import platform.Foundation.NSStringFromClass
import platform.UIKit.*
import platform.UIKit.UIAction.Companion.actionWithHandler

fun main() {
    val args = emptyArray<String>()
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()
        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

class SkikoAppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @ObjCObjectBase.OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<Any?, *>?): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds)
        window!!.rootViewController = Application("PlatformView Example") {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Compose Text")
                PlatformViewUI(factory = {
                    val view = MyCustomView()
                    view.text = "UIKit Text"
                    view.backgroundColor = UIColor.blueColor
                    view.textColor = UIColor.redColor
                    view
                },
                    update = {
                        println("Updated")
                    },
                    modifier = Modifier.size(200.dp, 30.dp)
                )
                Text("Compose Text Under Platform Text")
                PlatformViewUI(factory = {
                    val view = UIButton.buttonWithType(UIButtonTypeSystem)
                    view.setTitle("UIKit Button", UIControlStateNormal)
                    view.titleLabel?.textColor = UIColor.redColor
                    view.userInteractionEnabled = true
                    view.addAction(actionWithHandler {
                        println("Button pressed")
                    }, UIControlEventTouchUpInside)
                    view
                },
                    update = {
                        println("Updated")
                    },
                    modifier = Modifier.size(200.dp, 20.dp)
                )

                PlatformViewUI(factory = {
                    val view = UIStackView()
                    view.axis = UILayoutConstraintAxisHorizontal
                    val textView = UILabel().also { it.text = "UIText" }
                    val button = UIButton.buttonWithType(UIButtonTypeSystem).also { button ->
                        button.setTitle("UIButton", UIControlStateNormal)
                        button.addAction(actionWithHandler {
                            println("Button was pressed")
                            button.setTitle("Button that has been pressed", UIControlStateNormal)
                            invalidationCount.value++
                        }, UIControlEventTouchUpInside)
                    }
                    val textView2 = UILabel().also { it.text = "UIText2" }
                    view.addArrangedSubview(textView)
                    view.addArrangedSubview(button)
                    view.addArrangedSubview(textView2)
                    view.layoutSubviews()
                    view
                },
                    update = {
                        println("Updated")
                    },
                    modifier = Modifier.size(200.dp, 40.dp)
                )

                Text("Another Compose TextView")
            }
        }
        window!!.makeKeyAndVisible()
        return true
    }
}

class MyCustomView: UILabel(CGRectZero.readValue()) {

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        val touch = touches.first() as? UITouch
        val location = touch?.locationInView(null)
        println("Coordinates: ${location?.x}, ${location?.y}")
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        val touch = touches.first() as? UITouch
        val location = touch?.locationInView(null)
        println("Coordinates: ${location?.x}, ${location?.y}")
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        val touch = touches.first() as? UITouch
        val location = touch?.locationInView(null)
        println("Coordinates: ${location?.x}, ${location?.y}")
    }
}