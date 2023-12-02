package com.ym.base.widget.ext

import android.text.*
import android.widget.EditText
import androidx.annotation.IntRange
import androidx.core.widget.addTextChangedListener
import com.ym.base.ext.CanUseString

/**
 * Author:yangcheng
 * Date:2020/8/11
 * Time:17:47
 */
fun EditText.addTextWatcher(after : (s : Editable?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s : Editable?) {
        after.invoke(s)
      }

      override fun beforeTextChanged(
        s : CharSequence?,
        start : Int,
        count : Int,
        after : Int,
      ) {
      }

      override fun onTextChanged(
        s : CharSequence?,
        start : Int,
        before : Int,
        count : Int,
      ) {
      }

    })
}

fun EditText.setNumberCheckListener(
  @CanUseString("最大值从外界传入,") getMaxNumber : () -> Float = { 0f },
  @CanUseString("超过最大值的操作,") exceedMaxNumber : () -> Unit = {},
  @CanUseString("输入值合格后会走这里,") afterTextChanged : (input : Editable?) -> Unit = {},
) {
    this.addTextChangedListener { input -> afterTextChanged.invoke(input) }
    //设置Input的类型可以影响键盘弹出类型,实际判断输入类型是我们下面的代码完成的
    this.inputType = /*InputType.TYPE_NUMBER_FLAG_DECIMAL or*/ InputType.TYPE_CLASS_NUMBER
    this.filters = arrayOf(
      InputFilter { source,start,end,dest,dstart,dend ->
        val maxNumber = getMaxNumber.invoke()
        /*
                                参数source：将要插入的字符串，来自键盘输入、粘贴
                                参数start：source的起始位置，为0（暂时没有发现其它值的情况）
                                参数end：source的长度
                                参数dest：EditText中已经存在的字符串
                                参数dstart：插入点的位置
                                参数dend：插入点的结束位置，一般情况下等于dstart；如果选中一段字符串（这段字符串将会被替换），dstart的值就插入点的结束位置
                                */
        val inputNumber = source.substring(0,11.coerceAtMost(source.length))
          .filter { /*it == '.' || */(it in '0'..'9') }
        val oldString = dest.toString()
        val 输入框原来的内容 = this.text
        when {
          oldString.isEmpty() -> return@InputFilter if (inputNumber.isEmpty()) "" else {
            if ((inputNumber.toInt() > maxNumber)) {
              //超出范围 提示 并拦截 输入值
              exceedMaxNumber.invoke()
              return@InputFilter 输入框原来的内容.toString()
            } else return@InputFilter inputNumber.toInt().toString()
          }
          inputNumber.isEmpty() -> return@InputFilter ""
          else -> {
            val oldTextSelect = dest.substring(dstart,dend)
            val oldTextEnd = dest.substring(dend,dest.length)
            val realInputNumber =
              StringBuilder(dest.toString()).replace(dstart,dend,source.toString())
                .toString()
            if ((realInputNumber.toInt() > maxNumber)) {
              //超出范围 提示 并拦截 输入值
              exceedMaxNumber.invoke()
              return@InputFilter oldTextSelect
            } else {
              //考虑到  realInputString 的复杂性(如 0.03通过08.5240替换成008.03),所以采用最简单有效的处理方式
              this.setText(realInputNumber)
              this.setSelection(this.text.length - oldTextEnd.length)
              return@InputFilter oldTextSelect   //返回优化后的输入值
            }
          }
        }
      })
}

fun EditText.setDecimalCheckListener(
  @IntRange(from = 1) mDecimalLength : Int,
  @CanUseString("小数最大值从外界传入,") getMaxNumber : () -> Float = { 0f },
  @CanUseString("超过小数最大值的操作,") exceedMaxNumber : () -> Unit = {},
  @CanUseString("输入值合格后会走这里,") afterTextChanged : (input : Editable?) -> Unit = {},
) {
    this.addTextChangedListener { input -> afterTextChanged.invoke(input) }
    //设置Input的类型可以影响键盘弹出类型,实际判断输入类型是我们下面的代码完成的
    this.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
    //设置字符过滤
    //设置字符过滤
    this.filters = arrayOf(
      InputFilter { source,start,end,dest,dstart,dend ->
        val maxNumber = getMaxNumber.invoke()
        /*
                                参数source：将要插入的字符串，来自键盘输入、粘贴
                                参数start：source的起始位置，为0（暂时没有发现其它值的情况）
                                参数end：source的长度
                                参数dest：EditText中已经存在的字符串
                                参数dstart：插入点的位置
                                参数dend：插入点的结束位置，一般情况下等于dstart；如果选中一段字符串（这段字符串将会被替换），dstart的值就插入点的结束位置
                                */
        val inputNumber = source.substring(0,11.coerceAtMost(source.length))
          .filter { it == '.' || (it in '0'..'9') }

        val oldString = dest.toString()
        val 输入框原来的内容 = this.text
        when {
          //当，原来输入内容为空，
          oldString.isEmpty() -> {
            //注意oldString为空,但是 输入框原来的内容 不一定为空,因为如果调用setText设置新内容,oldString是会为空的
            when {
              inputNumber.isEmpty() -> {
                //过滤后的新的输入为空,
                return@InputFilter ""
              }
              //小数点在开头
              inputNumber.startsWith(".") -> {
                //替换所有小数点.防止出现 类似这样".2.3.2.5"的 复制 内容
                val replace = inputNumber.replace(".","")
                val substring =
                  replace.substring(0,mDecimalLength.coerceAtMost(replace.length))
                val result = "0.$substring"
                if ((result.toDouble() > maxNumber)) {
                  //超出范围 提示 并拦截 输入值
                  exceedMaxNumber.invoke()
                  return@InputFilter 输入框原来的内容.toString()
                } else return@InputFilter result
              }
              else -> {
                //没有小数点或者小数点不在开头的,我们都要处理
                val indexOfPoint = inputNumber.indexOf('.')
                when {
                  //没有小数点,整数
                  indexOfPoint == -1 -> {
                    if ((inputNumber.toDouble() > maxNumber)) {
                      //超出范围 提示 并拦截 输入值
                      exceedMaxNumber.invoke()
                      return@InputFilter 输入框原来的内容.toString()
                    } else return@InputFilter inputNumber.toInt().toString()
                  }
                  //有不在开头的小数点但是不清楚有几个小数点.保留第一个小数点,得到输入值
                  else -> {
                    //得到小数点前的数字,转为int.去除多余的开头0
                    val startIntString =
                      inputNumber.substring(0,indexOfPoint).toInt().toString()
                    //小数点后的数字按照约定小数长度切割,不去除末尾多余的0
                    val endIntString = StringBuilder(inputNumber.replace(".",""))
                      .replace(0,indexOfPoint,"")
                      .let {
                        it.substring(
                          0,
                          it.length.coerceAtMost(mDecimalLength)
                        )
                      }
                      .toString()

                    val result = "${startIntString}.${endIntString}"
                    if ((result.toDouble() > maxNumber)) {
                      //超出范围 提示 并拦截 输入值
                      exceedMaxNumber.invoke()
                      return@InputFilter 输入框原来的内容.toString()
                    } else {
                      //超出范围 提示 并拦截 输入值
                      return@InputFilter result   //返回优化后的输入值
                    }
                  }
                }
              }
            }
          }
          //当原来输入内容不为空,则可能发生替换,效应
          else -> {
            when {
              source.isEmpty() -> {
                //当之前输入不为空的时候,当新的输入为空,比如删除和对选中的数据替换剪切板的空数据
                return@InputFilter ""
              }
              else -> {
                //得将输入框的内容分为,用户选中数据和未选中数据的其后部分
                val oldTextStart = dest.substring(0,dstart)
                val oldTextSelect = dest.substring(dstart,dend)
                val oldTextEnd = dest.substring(dend,dest.length)
                when {
                  oldTextStart.contains('.') -> {
                    //前半段未选中数据已经包含小数点,则新输入的内容是添加到小数位,则需要过滤掉小数点,且需要关心还能插入几位小数
                    val inputNumberUse = inputNumber.replace(".","")
                    //得到前半部分的已经使用的小数位数
                    val oldTextStartDecimalLength =
                      oldTextStart.substring(oldTextStart.indexOf('.') + 1).length
                    //总小数位减去 oldTextEnd.length得到最大还能输入的位数.
                    val mRealDecimalLength =
                      0.coerceAtLeast(mDecimalLength - oldTextEnd.length - oldTextStartDecimalLength)
                    //因为小数位数的限制.我们需要控制输入内容的长度
                    val realInput = inputNumberUse.substring(
                      0,
                      inputNumberUse.length.coerceAtMost(mRealDecimalLength)
                    )
                    //新输入内容不为空,将输入的新内容和老内容结合得到推算即将生成的值,来判断是否需要拦截
                    val realInputNumber = StringBuilder(dest.toString()).replace(
                      dstart,
                      dend,
                      realInput
                    ).toString()
                    if ((realInputNumber.toDouble() > maxNumber)) {
                      //超出范围 提示 并拦截 输入值
                      exceedMaxNumber.invoke()
                      return@InputFilter oldTextSelect        //返回之前选中的数据
                    } else {
                      //计算后的输入值
                      this.setText(realInputNumber)
                      this.setSelection(this.text.length - oldTextEnd.length)
                      return@InputFilter realInput
                    }
                  }
                  oldTextEnd.contains('.') -> {
                    //后半段未选中数据已经包含小数点,则新输入的内容是添加到整数位,则需要过滤掉小数点,也不用关心还能插入几位小数
                    val inputNumberUse = inputNumber.replace(".","")
                    val realInputString = StringBuilder(dest.toString()).replace(
                      dstart,
                      dend,
                      inputNumberUse
                    ).toString()
                    //考虑到  realInputString 的复杂性(如 0.03替换成00.03),所以采用最简单有效的处理方式
                    this.setText(realInputString)
                    this.setSelection(this.text.length - oldTextEnd.length)
                    return@InputFilter oldTextSelect
                  }
                  else -> {
                    //前半段不包含小数点,后半段不包含小数,则新输入内容允许最多一个小数
                    val indexOfPoint = inputNumber.indexOf('.')
                    when {
                      //新输入内容没有小数点,整数
                      indexOfPoint == -1 -> {
                        val realInputString =
                          StringBuilder(dest.toString()).replace(
                            dstart,
                            dend,
                            inputNumber
                          ).toString()

                        if ((realInputString.toDouble() > maxNumber)) {
                          //超出范围 提示 并拦截 输入值
                          exceedMaxNumber.invoke()
                          return@InputFilter oldTextSelect
                        } else {
                          //考虑到  realInputString 的复杂性(如 0.03通过080替换成0080.03),所以采用最简单有效的处理方式
                          this.setText(realInputString)
                          this.setSelection(this.text.length - oldTextEnd.length)
                          return@InputFilter oldTextSelect   //返回优化后的输入值
                        }
                      }
                      //新输入内容有小数点,我们只保留一个小数点得到输入值
                      else -> {
                        //新输入内容允许有一个小数点,则需要过滤掉多余小数点,且需要关心优化后的输入值,还能拥有多少个小数
                        val inputNumberUse = inputNumber.replace(".","")
                        //得到可输入内容的的小数位长度.
                        val inputPointLength =
                          inputNumberUse.substring(indexOfPoint).length
                        //虽然可输入内容有小数,但是最后用不用 即将输入的小数需要计算得知
                        val 我们最多还能插入几个小数位 = mDecimalLength - oldTextEnd.length
                        //小于0表示连容纳小数点的机会都没了.
                        val 要插入的字符 = if (我们最多还能插入几个小数位 < 0) "" else "."
                        //计算后得到实际上我们用到的小数位
                        val mRealDecimalLength = inputPointLength.coerceAtMost(
                          0.coerceAtLeast(我们最多还能插入几个小数位)
                        )
                        //因为小数位数的限制.我们需要控制输入内容的小数位的长度
                        val realInputString =
                          StringBuilder(inputNumberUse).replace(
                            indexOfPoint,
                            indexOfPoint,
                            要插入的字符
                          )
                            .substring(
                              0,
                              indexOfPoint + 要插入的字符.length + mRealDecimalLength
                            )
                        val result =
                          "${oldTextStart}${realInputString}${oldTextEnd}"
                        if ((result.toDouble() > maxNumber)) {
                          //超出范围 提示 并拦截 输入值
                          exceedMaxNumber.invoke()
                          return@InputFilter oldTextSelect
                        } else {
                          //考虑到  realInputString 的复杂性(如 0.03通过08.5240替换成008.03),所以采用最简单有效的处理方式
                          this.setText(result)
                          this.setSelection(this.text.length - oldTextEnd.length)
                          return@InputFilter oldTextSelect   //返回优化后的输入值
                        }
                      }
                    }
                  }
                }

              }
            }
          }
        }
        return@InputFilter null
      }
    )
}