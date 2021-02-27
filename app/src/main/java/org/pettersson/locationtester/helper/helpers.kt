package org.pettersson.locationtester.helper

fun Float.format(digits: Int) = "%.${digits}f".format(this)
fun Double.format(digits: Int) = "%.${digits}f".format(this)

inline fun <reified T> T.TAG(): String = T::class.java.simpleName

