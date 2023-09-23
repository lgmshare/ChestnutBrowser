package com.welcome.browser.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

const val SHORT_ANIMATION_DURATION = 150L

fun View.isVisible() = visibility == View.VISIBLE

/**
 * 设置view显示
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 设置view占位隐藏
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrGone(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrInvisible(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

/**
 * 设置view隐藏
 */
fun View.gone() {
    visibility = View.GONE
}

fun View.fadeIn() {
    animate().alpha(1f).setDuration(SHORT_ANIMATION_DURATION).withStartAction { visible() }.start()
}

fun View.fadeOut() {
    animate().alpha(0f).setDuration(SHORT_ANIMATION_DURATION).withEndAction { gone() }.start()
}

/**
 * 防止连续短时点击
 */
fun View.setOnDebouncedClickListener(action: () -> Unit) {
    val actionDebouncer = ActionDebouncer(action)

    // This is the only place in the project where we should actually use setOnClickListener
    setOnClickListener {
        actionDebouncer.notifyAction()
    }
}

private class ActionDebouncer(private val action: () -> Unit) {

    companion object {
        const val DEBOUNCE_INTERVAL_MILLISECONDS = 500L
    }

    private var lastActionTime = 0L

    fun notifyAction() {
        val now = SystemClock.elapsedRealtime()
        val millisecondsPassed = now - lastActionTime
        lastActionTime = now

        if (millisecondsPassed > DEBOUNCE_INTERVAL_MILLISECONDS) {
            action.invoke()
        }
    }
}

fun View.getViewBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    layout(left, top, right, bottom)
    draw(canvas)
    return bitmap
}

fun View.doAfterMeasure(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        }
    )
}

fun View.setMargin(leftMargin: Int = 0, topMargin: Int = 0, rightMargin: Int = 0, bottomMargin: Int = 0) {
    when (parent) {
        is CollapsingToolbarLayout -> {
            (layoutParams as CollapsingToolbarLayout.LayoutParams).setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        }

        is LinearLayout -> {
            (layoutParams as LinearLayout.LayoutParams).setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        }

        is ConstraintLayout -> {
            (layoutParams as ConstraintLayout.LayoutParams).setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        }

        is RelativeLayout -> {
            (layoutParams as RelativeLayout.LayoutParams).setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        }

        is FrameLayout -> {
            (layoutParams as FrameLayout.LayoutParams).setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        }

        else -> {

        }
    }
}

fun View.hideSoftInput() {
    val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive) {
        imm.hideSoftInputFromWindow(applicationWindowToken, 0)
    }
}

