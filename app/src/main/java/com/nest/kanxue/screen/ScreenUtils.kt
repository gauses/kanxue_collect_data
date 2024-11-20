import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.util.Log
import org.json.JSONObject


class ScreenUtils(private val context: Context) {



    data class DisplayMode(
        val isScreenMirroring: Boolean,
        val displayInfo: DisplayInfo
    )
    fun getDisplayMode(): DisplayMode {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val mainDisplay = displayManager.displays.firstOrNull() ?: return DisplayMode(
            isScreenMirroring = false,
            displayInfo = getDefaultDisplayInfo()
        )

        // 检查是否在进行屏幕镜像
        val isScreenMirroring = try {
            // 检查一些可能表明正在进行屏幕镜像的标志
            val flags = mainDisplay.flags
            val isSfireScreening = (flags and Display.FLAG_SECURE) != 0
            val isPrivate = (flags and Display.FLAG_PRIVATE) != 0
            val isPresentationDisplay = mainDisplay.flags.let {
                (it and Display.FLAG_PRESENTATION) != 0
            }

            isSfireScreening || isPrivate || isPresentationDisplay
        } catch (e: Exception) {
            Log.e("ScreenUtils", "Error checking screen mirroring status", e)
            false
        }

        return DisplayMode(
            isScreenMirroring = isScreenMirroring,
            displayInfo = getDisplayInfo(mainDisplay)
        )
    }




    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    // 显示屏参数数据类
    data class DisplayMetrics(
        val density: Float,          // 屏幕密度
        val densityDpi: Int,         // 屏幕密度DPI
        val scaledDensity: Float,    // 字体缩放密度
        val xdpi: Float,             // 屏幕X方向DPI
        val ydpi: Float              // 屏幕Y方向DPI
    )

    // 显示屏信息数据类
    data class DisplayInfo(
        val width: Int,              // 宽度（像素）
        val height: Int,             // 高度（像素）
        val refreshRate: Float,      // 刷新率
        val rotation: Int,           // 旋转角度
        val name: String,            // 显示屏名称
        val isMain: Boolean,         // 是否是主屏
        val displayId: Int,          // 显示屏ID
        val flags: Int,              // 显示屏标志
        val appVsyncOffsetNanos: Long,
        val metrics: DisplayMetrics  // 显示屏参数
    ) {
        fun getScreenSizeInches(): Double {
            val widthInches = width / metrics.xdpi
            val heightInches = height / metrics.ydpi
            return Math.sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
        }

        override fun toString(): String {
            return """
                显示屏名字: $name
                ID: $displayId
                类型: ${if (isMain) "主屏幕" else "辅助屏幕"}
                分辨率: ${width}x${height}
                刷新率: $refreshRate Hz
                旋转角度: ${
                when (rotation) {
                    Surface.ROTATION_0 -> "0° (自然方向)"
                    Surface.ROTATION_90 -> "90° (顺时针)"
                    Surface.ROTATION_180 -> "180° (倒置)"
                    Surface.ROTATION_270 -> "270° (逆时针)"
                    else -> "$rotation° (未知)"
                }
            }
                屏幕尺寸: ${String.format("%.1f", getScreenSizeInches())} 英寸
                密度-density: ${metrics.density}
                DPI: ${metrics.densityDpi}
                XDPI: ${metrics.xdpi}
                YDPI: ${metrics.ydpi}
               
            """.trimIndent()
        }
    }

    /**
     * 获取所有显示屏的信息并按主副屏分类
     */
    fun getScreensInfo(includeSimulated: Boolean = true): Pair<DisplayInfo, List<DisplayInfo>> {
        var mainDisplay: DisplayInfo? = null
        val secondaryDisplays = mutableListOf<DisplayInfo>()

        // 尝试多种方式获取显示器
        val displaysList = mutableListOf<Display>()
        displaysList.addAll(displayManager.displays)

        // 获取模拟显示器
        displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)?.let {
            displaysList.addAll(it)
        }


        // 尝试获取所有显示器
        displayManager.getDisplays(null)?.let {
            displaysList.addAll(it)
        }

        // 去重
        val uniqueDisplays = displaysList.distinctBy { it.displayId }

        Log.d("ScreenUtils", """
            Display Detection Results:
            - Total displays found: ${uniqueDisplays.size}
            - Regular displays: ${displayManager.displays.size}
            - Presentation displays: ${displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION).size}
            - All displays (including disabled): ${displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION).size}
        """.trimIndent())

        uniqueDisplays.forEach { display ->
            val displayInfo = getDisplayInfo(display)
            if (display.displayId == Display.DEFAULT_DISPLAY) {
                mainDisplay = displayInfo
                Log.d("ScreenUtils", "Found main display: ${display.name}, ID: ${display.displayId}")
            } else {
                secondaryDisplays.add(displayInfo)
                Log.d("ScreenUtils", "Found secondary display: ${display.name}, ID: ${display.displayId}")
            }
        }

        return Pair(
            mainDisplay ?: getDefaultDisplayInfo(),
            secondaryDisplays
        )
    }

    /**
     * 获取单个显示屏的详细信息
     */
    private fun getDisplayInfo(display: Display): DisplayInfo {
        val size = Point()
        @Suppress("DEPRECATION")
        display.getRealSize(size)

        val metrics = context.resources.displayMetrics


        return DisplayInfo(
            width = size.x,
            height = size.y,
            refreshRate = display.refreshRate,
            rotation = display.rotation,
            name = display.name,
            isMain = (display.displayId == Display.DEFAULT_DISPLAY),
            displayId = display.displayId,
            flags = display.flags,
            appVsyncOffsetNanos= display.appVsyncOffsetNanos,
            metrics = DisplayMetrics(
                density = metrics.density,
                densityDpi = metrics.densityDpi,
                scaledDensity = metrics.scaledDensity,
                xdpi = metrics.xdpi,
                ydpi = metrics.ydpi
            )
        )
    }

    /**
     * 获取默认显示屏信息
     */
    private fun getDefaultDisplayInfo(): DisplayInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        return getDisplayInfo(windowManager.defaultDisplay)
    }
}