package com.ym.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.lang.NullPointerException


/**
 *  配合 RecyclerView 的吸顶控件
 *     使用方式
 *          1、给当前控件设置需要联动的 recyclerView
 *          2、floatViewAdapter，设置floatViewAdapter 是悬浮窗口和数据的关联关系的实现
 *                  获取对应等级的布局
 *                      abstract fun getLevelLayout(level: Int): Int
 *                  根据位置获取数据对应的等级，负数表述不吸顶
 *                      abstract fun getLevelByPosition(position: Int): Int
 *                  根据不同的位置绑定数据
 *                      abstract fun covertInfo(view: View, position: Int)
 */
class FloatTopWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    //悬浮的视图控制
    var floatViewAdapter: FloatViewAdapter? = null

    //设置联动的RecyclerView
    var recyclerView: RecyclerView? = null
        set(value) {
            if (value == null) throw NullPointerException("RecyclerView Can Not Be Set A NULL")
            if (value.layoutManager !is LinearLayoutManager) throw Exception("The RecyclerView Should  Use LinearLayoutManager")

            layoutManager = value.layoutManager as LinearLayoutManager

            value?.addOnScrollListener(onScrollListener)
            field = value
        }


    private var layoutManager: LinearLayoutManager? = null
    private var floatStack: ArrayList<FloatDescribeView> = arrayListOf() // 浮动视图队列
    private var tempFloatStack: ArrayList<FloatDescribeView> = arrayListOf() // 缓冲悬浮视图

    private var levelSizeTemple: HashMap<Int, Int> = hashMapOf()
    private var measureController = MeasureController()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var heightCount = 0
        for (index in 0 until childCount) {
            var itemView = getChildAt(index)
            itemView.measure(widthMeasureSpec, heightMeasureSpec)
            heightCount += itemView.measuredHeight
        }
        setMeasuredDimension(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(heightCount, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (index in 0 until childCount) {
            var itemView = getChildAt(index)
            var describe: FloatDescribeView = getDescribeByView(itemView)
            itemView.layout(l, describe.top, r, describe.top + itemView.measuredHeight)
        }
    }

    /**
     * 通过描述获取对应的视图
     */
    private fun getDescribeByView(view: View): FloatDescribeView {
        for (describe in floatStack) {
            if (describe.view == view) {
                return describe
            }
        }
        throw Exception("Can Not Find Describe Object")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recyclerView?.removeOnScrollListener(onScrollListener)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)
        return true
    }

    /**
     * ==============================================
     * 生成新描述列表
     * ==============================================
     */

    private var onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (floatViewAdapter == null || layoutManager == null) return
            tempFloatStack.clear()

            var position = layoutManager!!.findFirstVisibleItemPosition()
            if (position > 0)
                frontAddView(position - 1)
//            Log.e("result", "前查追加结果：=====")
//            showNode(tempFloatStack)
            afterAddView(position)
//            Log.e("result", "后查追加结果：=====")
//            showNode(tempFloatStack)
            compileViewList()
        }
    }

    /**
     * 前查追加
     *  1、当前控件为空时前查追加
     */
    private fun frontAddView(pos: Int) {
        var position = pos
        while (position >= 0) {
            //  只处理浮动窗口
            var currentLevel = floatViewAdapter!!.getLevelByPosition(position)

            if (currentLevel >= 0) {
                // 列表为空时和发现更高等级的悬浮窗时追加
                if (tempFloatStack.isEmpty() || tempFloatStack.first().level > currentLevel) {
                    tempFloatStack.add(
                        0,
                        FloatDescribeView(
                            position,
                            currentLevel,
                            -1,
                            getHeightByLevel(currentLevel)
                        )
                    )
                }
            }
            if (currentLevel == 0) break
            position--

        }
        measureController.soreData(tempFloatStack)
    }

    /**
     *  后查追加
     */
    private fun afterAddView(pos: Int) {
        var position = pos
        //当前列表中的控件高度
        var floatWidgetHeight =
            if (tempFloatStack.isEmpty()) 0 else tempFloatStack.last().top + tempFloatStack.last().viewHeight

        while (position < layoutManager!!.findFirstVisibleItemPosition() + layoutManager!!.childCount) {  //添加到列表没有接触的悬浮控件为止

            var tempView = layoutManager!!.findViewByPosition(position)
            if (tempView!!.top > floatWidgetHeight) { //下一个悬浮窗口偏离当前悬浮窗口列表时不处理
                break
            }

            var currentLevel = floatViewAdapter!!.getLevelByPosition(position)
            if (currentLevel >= 0) {    //后查查到悬浮控件
                var newDescribe = FloatDescribeView(
                    position,
                    currentLevel,
                    tempView!!.top,
                    getHeightByLevel(currentLevel)
                )
                if (!addAfterDescribe(tempFloatStack, newDescribe)) {
                    return
                }

                measureController.soreData(tempFloatStack)

                position++
                afterAddView(position)
                return
            }
            position++
        }
    }

    /**
     *  后查追加只保存前三段
     *      1       2  3    2 3
     *    不折叠段    折叠段   结尾段
     *    0  1  1  1
     */
    private fun addAfterDescribe(
        tempFloatStack: ArrayList<FloatDescribeView>,
        newDescribe: FloatDescribeView
    ): Boolean {

        //检测当前列表中有几段     1 2 3 2 3
        var nodePartCount = 0  //当前列表有几段
        var levelFlag = -1
        for (index in tempFloatStack.size - 1 downTo 0) {
            if (levelFlag >= 0 && tempFloatStack[index].level >= levelFlag) {
                nodePartCount++
            }
            levelFlag = tempFloatStack[index].level
        }

        //如果当前列表中只有第一段时直接添加  或者  当前已经存在第二段时只添加低等级
        if (nodePartCount < 1 || (nodePartCount == 1 && tempFloatStack.last().level < newDescribe.level)) {
            tempFloatStack.add(newDescribe)
            return true
        }
        return false
    }


//    /**
//     *
//     */
//    fun showNode(tempFloatStack: ArrayList<FloatDescribeView>) {
//        for (item in tempFloatStack) {
//            Log.e("result", item.toString())
//        }
//    }


    /**
     * 获取不同等级悬浮窗高度
     */
    private fun getHeightByLevel(level: Int): Int {
        if (levelSizeTemple[level] == null) {
            var view =
                LayoutInflater.from(context).inflate(floatViewAdapter!!.getLevelLayout(level), null)
            view.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            levelSizeTemple[level] = view.measuredHeight
        }
        return levelSizeTemple[level]!!
    }

    /**
     * ==============================================
     * 新描述列表和原有描述列表处理
     * ==============================================
     */

    fun compileViewList() {
        //清空视图
        removeAllViews()

        //重新整理视图空间
        var newStack: ArrayList<FloatDescribeView> = arrayListOf()
        if (tempFloatStack.isNotEmpty())
            for (index in 0 until tempFloatStack.size) {
                var describe = getDescribeByPosition(floatStack, tempFloatStack[index].position)
                if (describe == null) {
                    describe = tempFloatStack[index]
                    describe!!.view = LayoutInflater.from(context)
                        .inflate(floatViewAdapter!!.getLevelLayout(describe!!.level), null)
                    floatViewAdapter!!.covertInfo(describe!!.view!!, describe!!.position)
                }
                describe!!.top = tempFloatStack[index].top
                newStack.add(describe!!)
            }
        floatStack.clear()
        floatStack.addAll(newStack)

        //刷新视图
        for (index in floatStack.size - 1 downTo 0) {
            addView(floatStack[index].view)
        }

        //刷新布局
        requestLayout()

    }

    private fun getDescribeByPosition(
        floatStack: ArrayList<FloatDescribeView>,
        position: Int
    ): FloatDescribeView? {
        for (des in floatStack) {
            if (des.position > position) break
            if (des.position == position) return des
        }
        return null
    }


    /**
     * ==============================================
     * 相关类
     * ==============================================
     */

    /**
     * 对控件描述
     */
    class FloatDescribeView(
        var position: Int,   //当前描述在数据中的位置
        var level: Int = 0,  //当前描述所属悬浮等级
        var top: Int = 0,    //当前描述到顶部的距离
        var viewHeight: Int = 0,  //当前描述控件高度
        var view: View? = null    // 当前描述的控件

    ) {
        override fun toString(): String {
            return "  position: $position  level:$level  top:$top  viewHeight:$viewHeight view:$view"
        }
    }

    /**
     * 浮动控件
     */
    abstract class FloatViewAdapter {
        // 获取对应等级的布局
        abstract fun getLevelLayout(level: Int): Int

        // 根据位置获取数据对应的等级，负数表述不吸顶
        abstract fun getLevelByPosition(position: Int): Int

        // 根据不同的位置绑定数据
        abstract fun covertInfo(view: View, position: Int)
    }

    /**
     * 计算位置
     */
    class MeasureController {

        lateinit var tempFloatStack: ArrayList<FloatDescribeView>

        //折叠节点位置
        var lastParentIndex = -1  //最后一节的最高等级节点
        var foldIndex = -1  //折叠节点

        //关键高度属性
        var firstHeight = 0 //第一节可以为0
        var secondHeight = 0


        constructor()

        fun soreData(tempFloatStack: ArrayList<FloatDescribeView>) {
            this.tempFloatStack = tempFloatStack
            if (tempFloatStack.isEmpty()) return

            lastParentIndex = -1
            foldIndex = -1

            firstHeight = 0
            secondHeight = 0


            getFoldIndex()
            countHeight()
            moveByLevel()
        }

        /**
         * 判断关键节点
         */
        private fun getFoldIndex() {

            //先找到第三节的最高等级  1 2 3 2 3
            var lastParentDes: FloatDescribeView? = null
            for (index in tempFloatStack.size - 1 downTo 0) {
                if (lastParentDes == null || lastParentDes!!.level > tempFloatStack[index].level) {
                    lastParentDes = tempFloatStack[index]
                    lastParentIndex = index
                } else if (lastParentDes != null && tempFloatStack[index].level >= lastParentDes!!.level) {
                    break
                }
            }

            // 找到二节折叠位置
            if (lastParentIndex > 0) {
                for (index in lastParentIndex - 1 downTo 0) {
                    foldIndex = index
                    if (tempFloatStack[index].level <= lastParentDes!!.level) {
                        break
                    }
                }
            }
//            Log.e("result", "折叠位置： $lastParentIndex   $foldIndex")
        }


        /**
         * 计算关键高度
         */
        private fun countHeight() {
            if (foldIndex < 0) return

            // 计算第一节总高度
            for (fIndex in 0 until foldIndex) {
                firstHeight += tempFloatStack[fIndex].viewHeight
            }

            // 计算中间节的总高度
            for (fIndex in foldIndex until lastParentIndex) {
                secondHeight += tempFloatStack[fIndex].viewHeight
            }
//            Log.e("result", "第一节高度： $foldIndex  第二节高度：$secondHeight")
        }

        /**
         * 折叠计算
         */
        private fun moveByLevel() {
            //  或者     直接计算整个悬浮高度
            if (foldIndex >= 0) {  //折叠流程
                var countSize = 0
                var covertHeight = 0
                //设置折叠第一部分的位置
                for (index in 0 until foldIndex) {
                    var item = tempFloatStack[index]
                    item.top = countSize
                    countSize += item.viewHeight
                }
                covertHeight = countSize

                // 计算折叠偏移量
                var top =
                    if (lastParentIndex > 0) tempFloatStack[lastParentIndex].top else tempFloatStack.last().top
                // 顶部偏移 =  第一部分高度  + 折叠区可用空间 - 折叠区高度
                countSize = countSize + (top - firstHeight) - secondHeight
                // 折叠区小于折叠高度的情况
                countSize =
                    if (countSize + secondHeight <= firstHeight) firstHeight - secondHeight else countSize
                //设置折叠后的位置
                for (index in foldIndex until tempFloatStack.size) {
                    var item = tempFloatStack[index]
                    item.top = countSize
                    countSize += item.viewHeight
                }

                if (covertHeight == 0) { //删除边界外部分
                    var tempIndex = 0
                    while (tempFloatStack.size > tempIndex) {
                        if (tempFloatStack[tempIndex].top + tempFloatStack[tempIndex].viewHeight <= 0) {
                            tempFloatStack.removeAt(tempIndex)
                        } else if (tempFloatStack[tempIndex].top + tempFloatStack[tempIndex].viewHeight > 0) {
                            break
                        } else {
                            tempIndex++
                        }
                    }
                } else if (covertHeight != 0) { //  删除第一部分遮盖部分
                    var tempIndex = foldIndex
                    while (tempFloatStack.size > tempIndex) {
                        if (tempFloatStack[tempIndex].top + tempFloatStack[tempIndex].viewHeight <= covertHeight) {
                            tempFloatStack.removeAt(tempIndex)
                        } else if (tempFloatStack[tempIndex].top + tempFloatStack[tempIndex].viewHeight > covertHeight) {
                            break
                        } else {
                            tempIndex++
                        }
                    }
                }
            } else if (tempFloatStack.last().top < 0  //全部为前追加
                || tempFloatStack.last().top - firstHeight <= 0 //后追加列表高度小于基本悬浮高度
                || (firstHeight == 0 && foldIndex < 0)  //单一列表情况
            ) {
                var countSize = 0
                for (item in tempFloatStack) {
                    item.top = countSize
                    countSize += item.viewHeight
                }
            } else {
                throw Exception("未知状态")
            }
        }
    }
}