package com.ym.base.ext

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*

/**
 *
 * author     : yangcheng
 * since       : 2020/10/31 14:11
 */

//@CanUseInt(top_bottom, tr_bl)
@Retention(SOURCE)
@Target(
    CLASS,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
    FIELD,
    LOCAL_VARIABLE,
    ANNOTATION_CLASS
)
annotation class CanUseInt(vararg val value : Int = [])

@Retention(SOURCE)
@Target(
    CLASS,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
    FIELD,
    LOCAL_VARIABLE,
    ANNOTATION_CLASS
)
annotation class CanUseString(vararg val value : String = [])

@Retention(SOURCE)
@Target(
    CLASS,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
    FIELD,
    LOCAL_VARIABLE,
    ANNOTATION_CLASS
)
annotation class CanUseLong(vararg val value : Long = [])

@Retention(SOURCE)
@Target(
    CLASS,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
    FIELD,
    LOCAL_VARIABLE,
    ANNOTATION_CLASS
)
annotation class CanUseFloat(vararg val value : Float = [])

@Retention(SOURCE)
@Target(
    CLASS,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
    FIELD,
    LOCAL_VARIABLE,
    ANNOTATION_CLASS
)
annotation class CanUseDouble(vararg val value : Double = [])