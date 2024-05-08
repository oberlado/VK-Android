package com.example.testtask

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min

class BottomNavigationBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @ViewCompat.NestedScrollType
    private var lastStartedType: Int = 0

    private var offsetAnimator: ValueAnimator? = null

    private var isSnappingEnabled = true

    @SuppressLint("RestrictedApi")
    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            try {
                updateSnackbar(child, dependency)
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        if (axes != ViewCompat.SCROLL_AXIS_VERTICAL)
            return false

        lastStartedType = type

        offsetAnimator?.cancel()

        return true
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        child.translationY = max(0f, min(child.height.toFloat(), child.translationY + dy))
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        type: Int
    ) {

        if (!isSnappingEnabled) {
            return
        }

        // Add snap behaviour
        // Logic here borrowed from AppBarLayout onStopNestedScroll
        if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
            // find nearest seam
            val currTranslation = child.translationY
            val childHalfHeight = child.height * 0.5f

            // translate down
            if (currTranslation >= childHalfHeight) {
                animateBarVisibility(child, isVisible = false)
            }
            // translate up
            else {
                animateBarVisibility(child, isVisible = true)
            }
        }

    }

    private fun animateBarVisibility(child: View, isVisible: Boolean) {
        if (offsetAnimator == null) {
            offsetAnimator = ValueAnimator().apply {
                interpolator = DecelerateInterpolator()
                duration = 150L
            }

            offsetAnimator?.addUpdateListener {
                child.translationY = it.animatedValue as Float
            }
        } else {
            offsetAnimator?.cancel()
        }

        val targetTranslation = if (isVisible) 0f else child.height.toFloat()
        offsetAnimator?.setFloatValues(child.translationY, targetTranslation)
        offsetAnimator?.start()
    }

    private fun updateSnackbar(child: View, @SuppressLint("RestrictedApi") snackbarLayout: Snackbar.SnackbarLayout) {
        if (snackbarLayout.layoutParams is CoordinatorLayout.LayoutParams) {

            // Remove messy shadow
            snackbarLayout.outlineProvider = null

            val params = snackbarLayout.layoutParams as CoordinatorLayout.LayoutParams
            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        }
    }

    fun slideUp(child: BottomNavigationView) {
        child.visibility = View.VISIBLE
        animateBarVisibility(child, isVisible = true)
    }

    fun slideDown(child: BottomNavigationView, isGone: Boolean) {
        animateBarVisibility(child, isVisible = false)
        if (isGone) {
            child.visibility = View.GONE
        }
    }
}
