package com.ym.base.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.reactivex.Observable

/**
 * Author:yangcheng
 * Date:2020/11/09
 */

/** 封装本方法可以对每个EditText设置监听并有Rxjava发出方便使用rxjava的CombineLatest操作符  */
fun TextView.textChangesObserver() : Observable<String> {
    return Observable.create { emitter ->
        this.addTextChangedListener(object : TextWatcher {
          override fun beforeTextChanged(s : CharSequence,start : Int,count : Int,after : Int) {}
          override fun onTextChanged(s : CharSequence,start : Int,before : Int,count : Int) {}
          override fun afterTextChanged(s : Editable) {
            emitter.onNext(s.toString())
          }
        })
        this.text = text
    }
}
