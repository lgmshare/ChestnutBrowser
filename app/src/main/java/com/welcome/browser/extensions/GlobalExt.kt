package com.welcome.browser.extensions

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * 批量添加点击事件
 */
inline fun setOnClick(vararg v: View?, crossinline block: View.() -> Unit) {
    v.forEach {
        it?.run {
            setOnDebouncedClickListener {
                this.block()
            }
        }
    }
}

/**
 * flow计时器
 *
 * @param total
 * @return job
 */
inline fun countDownCoroutines(
    total: Int,
    step: Int = 50,
    delay: Long = 50,
    crossinline onTick: (Int) -> Unit,
    crossinline onFinish: () -> Unit,
    scope: CoroutineScope
): Job {
    return flow {
        for (i in 0..total step step) {
            emit(i)
            delay(delay)
        }
    }.flowOn(Dispatchers.Default)
        .onCompletion { onFinish.invoke() }
        .onEach { onTick.invoke(it) }
        .flowOn(Dispatchers.Main)
        .launchIn(scope)
}




