package com.nest.kanxue.core

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style
import android.graphics.Paint.Align
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import android.util.Base64

class GpuFingerprinter {
    companion object {
        // 生成显卡特征哈希值
        fun generateGpuFingerprint(): String {
            // 1. 创建虚拟画布并绘制特定图形
            val canvas = createCanvasImage()

            // 2. 获取图像数据并计算哈希
            return calculateImageHash(canvas)
        }

        // 创建虚拟画布并绘制特定图形
        private fun createCanvasImage(): Bitmap {
            val width = 200
            val height = 200

            // 创建ARGB图像
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(ANTI_ALIAS_FLAG)

            // 绘制背景
            canvas.drawColor(Color.WHITE)

            // 绘制特定图形 - 模拟浏览器Canvas指纹方法
            drawFingerprintShapes(canvas, paint, width, height)

            return bitmap
        }

        // 绘制用于生成指纹的特定图形
        private fun drawFingerprintShapes(canvas: Canvas, paint: Paint, width: Int, height: Int) {
            // 绘制矩形
            paint.color = Color.rgb(100, 100, 200)
            paint.style = Style.FILL
            canvas.drawRect(10f, 10f, 60f, 60f, paint)

            // 绘制圆形
            paint.color = Color.argb(150, 200, 100, 100)
            canvas.drawOval(70f, 10f, 130f, 70f, paint)

            // 绘制复杂路径
            paint.color = Color.BLACK
            paint.style = Style.STROKE
            paint.strokeWidth = 2f
            paint.strokeCap = Cap.ROUND
            paint.strokeJoin = Join.ROUND
            
            val path = Path()
            path.moveTo(20f, 150f)
            path.lineTo(50f, 120f)
            path.lineTo(80f, 150f)
            path.lineTo(110f, 120f)
            path.lineTo(140f, 150f)
            path.lineTo(170f, 120f)
            canvas.drawPath(path, paint)

            // 绘制文本 - 使用不同字体和大小
            val typefaces = arrayOf(
                Typeface.DEFAULT_BOLD,
                Typeface.SERIF,
                Typeface.MONOSPACE
            )

            paint.color = Color.rgb(0, 100, 0)
            paint.style = Style.FILL
            paint.textAlign = Align.LEFT

            for (i in typefaces.indices) {
                paint.typeface = typefaces[i]
                paint.textSize = (14 + i * 2).toFloat()
                canvas.drawText("GPU Fingerprint Test $i", 20f, 80f + i * 20, paint)
            }

            // 绘制渐变
            val gradient = LinearGradient(
                100f, 100f, 180f, 180f,
                Color.RED, Color.BLUE,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(100f, 100f, 180f, 180f, paint)
        }

        // 计算图像哈希值
        private fun calculateImageHash(bitmap: Bitmap): String {
            try {
                // 将图像转为字节数组
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val imageData = outputStream.toByteArray()

                // 计算SHA-256哈希
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(imageData)

                // 转为Base64字符串
                return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                throw RuntimeException("Failed to calculate image hash", e)
            } finally {
                bitmap.recycle()
            }
        }
    }
} 