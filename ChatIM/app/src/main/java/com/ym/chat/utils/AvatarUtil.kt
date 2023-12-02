package com.ym.chat.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.ym.base_sdk.R
import java.util.*


object AvatarUtil {

    fun getBuilder(context: Context): Builder? {
        return Builder(context)
    }

    class Builder(context: Context) {
        private val mContext: Context = context
        private var mList: List<Any>? = null
        private var mWidth = 150 // 控件宽度
        private var mHeight = 150 // 控件高度
        private var mShape = Shape.ROUND // 控件形状
        private var mRoundAngel = 360 // 圆角大小
        private var mMarginWidth = 4 // 图片间隙
        private var mMarginColor: Int = R.color.grgray // 图片间隙颜色
        private var hasEdge = true // 是否包含边缘
        private var mTextSize = 100f // 文字大小
        private var mTextColor: Int = R.color.white // 文字颜色
        private var mBackGroundColor: Int = Color.rgb(23, 255, 138) // 文字背景颜色

        /**
         * 随机生成一个颜色值
         */
        private fun getBackgroundColor(): Int {
            val random = Random()
            val r: Int = random.nextInt(256)
            val g: Int = random.nextInt(256)
            val b: Int = random.nextInt(256)
            //如果背景色接近白色，字体颜色显示黑色
            if (r > 230 && g > 230 && b > 230) {
                mTextColor = R.color.black
            }
            return Color.rgb(r, g, b)
        }

        /**
         * 根据字符串生成一个指定串颜色值
         */
        fun setBackgroundColor(str: String): Builder {
            var r: Int = 0
            var g: Int = 0
            var b: Int = 0
            if (str.isNotBlank() && str.isNotEmpty()) {
                r = UniCodeUtils.utf8ToUnicodeNoPrefix(str[0].toString()).toInt() % 256
                if (str.length > 1) {
                    g = UniCodeUtils.utf8ToUnicodeNoPrefix(str[1].toString()).toInt() % 256
                    if (str.length > 2) {
                        b = UniCodeUtils.utf8ToUnicodeNoPrefix(str[2].toString()).toInt() % 256
                    }
                }
                mBackGroundColor = Color.rgb(r, g, b)
                //如果背景色接近白色，字体颜色显示黑色
                if (r > 230 && g > 230 && b > 230) {
                    mTextColor = R.color.black
                }
            } else {
                mBackGroundColor = getBackgroundColor()
            }
            return this
        }

        /**
         * 设置数据源
         */
        fun setList(mList: List<Any>?): Builder {
            this.mList = mList
            return this
        }

        /**
         * 设置图片尺寸
         */
        fun setBitmapSize(mWidth: Int, mHeight: Int): Builder {
            if (mWidth > 0) {
                this.mWidth = mWidth
            }
            if (mHeight > 0) {
                this.mHeight = mHeight
            }
            return this
        }

        /**
         * 设置展示类型（圆形、圆角、方形）
         */
        fun setShape(mShape: Int): Builder {
            this.mShape = mShape
            return this
        }

        /**
         * 设置圆角角度
         * 当shape设置为Shape.Round时读取改属性
         *
         * @param mRoundAngel 圆角角度
         */
        fun setRoundAngel(mRoundAngel: Int): Builder {
            this.mRoundAngel = mRoundAngel
            return this
        }

        /**
         * 设置分割线宽度
         */
        fun setMarginWidth(mMarginWidth: Int): Builder {
            this.mMarginWidth = mMarginWidth
            return this
        }

        /**
         * 设置分割线颜色
         */
        fun setMarginColor(mMarginColor: Int): Builder {
            this.mMarginColor = mMarginColor
            return this
        }

        /**
         * 设置文字大小
         */
        fun setTextSize(mTextSize: Int): Builder {
            this.mTextSize = mTextSize.toFloat()
            return this
        }

        /**
         * 设置文字颜色
         */
        fun setTextColor(mTextColor: Int): Builder {
            this.mTextColor = mTextColor
            return this
        }

        fun setHasEdge(hasEdge: Boolean): Builder {
            this.hasEdge = hasEdge
            return this
        }

        fun create(): Bitmap {
            val result = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()
            paint.isAntiAlias = true
            canvas.drawPath(drawShapePath(), paint)
            val marginPath: FloatArray
            val listSize = mList!!.size
            when (listSize) {
                1 -> startDraw(canvas, mList!![0], DrawPosition.WHOLE)
                2 -> {
                    startDraw(canvas, mList!![0], DrawPosition.LEFT)
                    startDraw(canvas, mList!![1], DrawPosition.RIGHT)
                    marginPath = floatArrayOf(
                        (mWidth / 2).toFloat(),
                        0f,
                        (mWidth / 2).toFloat(),
                        mHeight.toFloat()
                    )
                    drawMarginLine(canvas, marginPath)
                }
                3 -> {
                    startDraw(canvas, mList!![0], DrawPosition.LEFT)
                    startDraw(canvas, mList!![1], DrawPosition.RIGHT_TOP)
                    startDraw(canvas, mList!![2], DrawPosition.RIGHT_BOTTOM)
                    marginPath = floatArrayOf(
                        (mWidth / 2).toFloat(), 0f, (
                                mWidth / 2).toFloat(), mHeight.toFloat(), (
                                mWidth / 2).toFloat(), (mHeight / 2).toFloat(),
                        mWidth.toFloat(), (mHeight / 2).toFloat()
                    )
                    drawMarginLine(canvas, marginPath)
                }
                else -> {
                    startDraw(canvas, mList!![0], DrawPosition.LEFT_TOP)
                    startDraw(canvas, mList!![1], DrawPosition.LEFT_BOTTOM)
                    startDraw(canvas, mList!![2], DrawPosition.RIGHT_TOP)
                    startDraw(canvas, mList!![3], DrawPosition.RIGHT_BOTTOM)
                    marginPath = floatArrayOf(
                        (mWidth / 2).toFloat(),
                        0f,
                        (
                                mWidth / 2).toFloat(),
                        mHeight.toFloat(),
                        0f,
                        (mHeight / 2).toFloat(),
                        mWidth.toFloat(),
                        (mHeight / 2).toFloat()
                    )
                    drawMarginLine(canvas, marginPath)
                }
            }
            // 仅方形支持边缘  且单个文字不支持边缘
            if (hasEdge && mShape == Shape.SQUARE && !(mList!!.size == 1 && mList!![0] is String)) {
                drawEdge(canvas)
            }
            return result
        }

        /**
         * 根据边角配置绘制画布path
         */
        private fun drawShapePath(): Path {
            val mPath = Path()
            when (mShape) {
                Shape.ROUND -> mPath.addRoundRect(
                    RectF(
                        0F, 0F,
                        mHeight.toFloat(), mWidth.toFloat()
                    ), mRoundAngel.toFloat(), mRoundAngel.toFloat(), Path.Direction.CCW
                )
                Shape.SQUARE -> mPath.addRect(
                    RectF(
                        0F, 0F,
                        mHeight.toFloat(), mWidth.toFloat()
                    ), Path.Direction.CCW
                )
                Shape.CIRCLE -> {
                    val radius = mWidth.coerceAtLeast(mHeight) / 2
                    mPath.addCircle(
                        (mWidth / 2).toFloat(),
                        (mHeight / 2).toFloat(),
                        radius.toFloat(),
                        Path.Direction.CCW
                    )
                }
            }
            return mPath
        }

        /**
         * 根据数据源类型区分绘制图片或文字
         */
        private fun startDraw(canvas: Canvas, resource: Any, position: Int) {
            if (resource is Bitmap) {
                drawBitmap(canvas, resource, position)
            } else if (resource is String) {
                drawText(canvas, resource, position)
            }
        }

        /**
         * 绘制图片
         * 最多支持四张图
         */
        private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, mode: Int) {
            val left: Int
            val top: Int
            val x: Int
            val y: Int
            val width: Int
            val height: Int
            val dstWidth: Int
            val dstHeight: Int
            val paint = Paint()
            paint.isAntiAlias = true
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (mode == DrawPosition.WHOLE) {
                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, false)
                canvas.drawBitmap(bmp, 0f, 0f, paint)
            } else if (mode == DrawPosition.LEFT) {
                dstWidth = mWidth
                dstHeight = mHeight
                x = mWidth / 4 + mMarginWidth / 4
                y = 0
                width = mWidth / 2 - mMarginWidth / 4
                height = mHeight
                left = 0
                top = 0

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 裁取中间部分(从x点裁取置顶距离)
                val dstBmp = Bitmap.createBitmap(bmp, x, y, width, height)
                // 绘图
                canvas.drawBitmap(dstBmp, left.toFloat(), top.toFloat(), paint)
            } else if (mode == DrawPosition.RIGHT) {
                dstWidth = mWidth
                dstHeight = mHeight
                x = mWidth / 4 + mMarginWidth / 4
                y = 0
                width = mWidth / 2 - mMarginWidth / 4
                height = mHeight
                left = mWidth / 2 + mMarginWidth / 4
                top = 0

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 裁取中间部分(从x点裁取置顶距离)
                val dstBmp = Bitmap.createBitmap(bmp, x, y, width, height)
                // 绘图
                canvas.drawBitmap(dstBmp, left.toFloat(), top.toFloat(), paint)
            } else if (mode == DrawPosition.LEFT_TOP) {
                dstWidth = mWidth / 2 - mMarginWidth / 4
                dstHeight = mHeight / 2 - mMarginWidth / 4
                left = 0
                top = 0

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 绘图
                canvas.drawBitmap(bmp, left.toFloat(), top.toFloat(), paint)
            } else if (mode == DrawPosition.LEFT_BOTTOM) {
                dstWidth = mWidth / 2 - mMarginWidth / 4
                dstHeight = mHeight / 2 - mMarginWidth / 4
                left = 0
                top = mHeight / 2 + mMarginWidth / 4

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 绘图
                canvas.drawBitmap(bmp, left.toFloat(), top.toFloat(), paint)
            } else if (mode == DrawPosition.RIGHT_TOP) {
                dstWidth = mWidth / 2 - mMarginWidth / 4
                dstHeight = mHeight / 2 - mMarginWidth / 4
                left = mWidth / 2 + mMarginWidth / 4
                top = 0

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 绘图
                canvas.drawBitmap(bmp, left.toFloat(), top.toFloat(), paint)
            } else if (mode == DrawPosition.RIGHT_BOTTOM) {
                dstWidth = mWidth / 2 - mMarginWidth / 4
                dstHeight = mHeight / 2 - mMarginWidth / 4
                left = mWidth / 2 + mMarginWidth / 4
                top = mHeight / 2 + mMarginWidth / 4

                // 比例缩放
                val bmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)
                // 绘图
                canvas.drawBitmap(bmp, left.toFloat(), top.toFloat(), paint)
            }
        }

        /**
         * 绘制文字
         */
        private fun drawText(canvas: Canvas, text: String, mode: Int) {
            var bgLeft = 0f
            var bgTop = 0f
            var bgRight = 0f
            var bgBottom = 0f
            var textSize = mTextSize
            val textBgPaint = Paint()
            textBgPaint.color = mBackGroundColor
            textBgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (mode == DrawPosition.WHOLE) {
                bgLeft = 0f
                bgTop = 0f
                bgRight = mWidth.toFloat()
                bgBottom = mHeight.toFloat()
                textSize = (mWidth / 2).toFloat()
            } else if (mode == DrawPosition.LEFT) {
                bgLeft = 0f
                bgTop = 0f
                bgRight = (mWidth / 2 - mMarginWidth / 4).toFloat()
                bgBottom = mHeight.toFloat()
                textSize = (mWidth / 4).toFloat()
            } else if (mode == DrawPosition.RIGHT) {
                bgLeft = (mWidth / 2 + mMarginWidth / 4).toFloat()
                bgTop = 0f
                bgRight = mWidth.toFloat()
                bgBottom = mHeight.toFloat()
                textSize = (mWidth / 4).toFloat()
            } else if (mode == DrawPosition.LEFT_TOP) {
                bgLeft = 0f
                bgTop = 0f
                bgRight = (mWidth / 2 - mMarginWidth / 4).toFloat()
                bgBottom = (mHeight / 2 - mMarginWidth / 4).toFloat()
                textSize = (mWidth / 5).toFloat()
            } else if (mode == DrawPosition.LEFT_BOTTOM) {
                bgLeft = 0f
                bgTop = (mHeight / 2 + mMarginWidth / 4).toFloat()
                bgRight = (mWidth / 2 - mMarginWidth / 4).toFloat()
                bgBottom = mHeight.toFloat()
                textSize = (mWidth / 5).toFloat()
            } else if (mode == DrawPosition.RIGHT_TOP) {
                bgLeft = (mWidth / 2 + mMarginWidth / 4).toFloat()
                bgTop = 0f
                bgRight = mWidth.toFloat()
                bgBottom = (mHeight / 2 - mMarginWidth / 4).toFloat()
                textSize = (mWidth / 5).toFloat()
            } else if (mode == DrawPosition.RIGHT_BOTTOM) {
                bgLeft = (mWidth / 2 + mMarginWidth / 4).toFloat()
                bgTop = (mHeight / 2 + mMarginWidth / 4).toFloat()
                bgRight = mWidth.toFloat()
                bgBottom = mHeight.toFloat()
                textSize = (mWidth / 5).toFloat()
            }
            val rect = RectF(bgLeft, bgTop, bgRight, bgBottom)
            canvas.drawRect(rect, textBgPaint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.isAntiAlias = true
            textPaint.color = ContextCompat.getColor(mContext, mTextColor)
            textPaint.textSize = Math.min(mTextSize, textSize)
            textPaint.style = Paint.Style.FILL
            textPaint.textAlign = Paint.Align.CENTER
            val fontMetrics = textPaint.fontMetrics
            val baseline = ((bgBottom + bgTop - fontMetrics.bottom - fontMetrics.top) / 2).toInt()
            canvas.drawText(text, rect.centerX(), baseline.toFloat(), textPaint)
        }

        /**
         * 绘制边缘线
         */
        private fun drawEdge(canvas: Canvas) {
            val edgePaint = Paint()
            edgePaint.strokeWidth = mMarginWidth.toFloat()
            edgePaint.style = Paint.Style.STROKE
            edgePaint.color = ContextCompat.getColor(mContext, mMarginColor)
            val mPath = Path()
            mPath.moveTo(0f, 0f)
            mPath.lineTo(0f, mHeight.toFloat())
            mPath.lineTo(mWidth.toFloat(), mHeight.toFloat())
            mPath.lineTo(mWidth.toFloat(), 0f)
            mPath.close()
            canvas.drawPath(mPath, edgePaint)
        }

        /**
         * 绘制分割线
         */
        private fun drawMarginLine(canvas: Canvas, path: FloatArray) {
            val marginPaint = Paint()
            marginPaint.strokeWidth = (mMarginWidth / 2).toFloat()
            marginPaint.color = ContextCompat.getColor(mContext, mMarginColor)
            canvas.drawLines(path, marginPaint)
        }

    }

    interface Shape {
        companion object {
            const val ROUND = 0X33
            const val CIRCLE = 0X11
            const val SQUARE = 0X22
        }
    }

    internal interface DrawPosition {
        companion object {
            const val WHOLE = 0
            const val LEFT = 1
            const val RIGHT = 2
            const val LEFT_TOP = 3
            const val LEFT_BOTTOM = 4
            const val RIGHT_TOP = 5
            const val RIGHT_BOTTOM = 6
        }
    }

}