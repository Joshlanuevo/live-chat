package com.ym.base_sdk

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ym.base.ext.matchYnPhone

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ym.base_sdk.test", appContext.packageName)
        //        val phone = "0912419333"
    }

    @Test
    fun testMatchPhone() {
        val phoneList = mutableListOf<String>().apply {
            add("0987463295")
            add("0332019073")
            add("0975324051")
            add("0982572929")
            add("0931370975")
            add("0984185333")
            add("0869847162")
            add("0938753738")
            add("0975802890")
            add("0965769660")
        }

        val matchResult = hashMapOf<String, String>()
        phoneList.forEach {
            val tempResult = it.matchYnPhone()
            matchResult.put(it,"手机号${it}检验结果：${tempResult}")
        }

        assert(matchResult.isEmpty())
    }
}