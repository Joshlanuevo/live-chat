package com.ym.base.util.other

import java.util.*


class QueueUtil(
    val action: ((item: Any) -> Unit)? = null,
) {
    var mQueue: Queue<Any> = LinkedList()

    /** 具体执行的方法--插入队列，插入队列要校验是否是插入的第一个，如果是插入第一个，则要执行这个  */
    @Synchronized fun enqueueAction(item: Any) {
        try {
            if (mQueue.isEmpty()) {
                mQueue.add(item)
                handleAction()
            } else {
                mQueue.add(item)
            }
        } catch (mThrowable: Throwable) {
            mThrowable.printStackTrace()
        }
    }


    /** 具体执行的方法--执行事务 ，最后调用executeNextAction，是自动执行下一个  */
    @Throws(Throwable::class)
    private fun handleAction() {
        try {
            if (mQueue.isEmpty()) return
            //手动调用事务的run方法
            val item = mQueue.peek() ?: return
            action?.invoke(item)
            executeNextAction()
        } catch (mE: Exception) {
            mE.printStackTrace()
            executeNextAction()
        }
    }

    private fun executeNextAction() {
        try {
            mQueue.poll()
            handleAction()
        } catch (mThrowable: Throwable) {
            mThrowable.printStackTrace()
        }
    }
}