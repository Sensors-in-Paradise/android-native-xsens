package sensors_in_paradise.sonar.page2.labels_editor

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout

class ItemClickableMotionLayout(context: Context, attributeSet: AttributeSet) :
    MotionLayout(context, attributeSet) {
    /*
    // TODO: fix not scrolling on child view with onClickListener

      private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

      private var isScrolling = false

      private var initialTouchX = -1
      private var initialTouchY = -1

      override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
          /*
           * This method JUST determines whether we want to intercept the motion.
           * If we return true, onTouchEvent will be called and we do the actual
           * scrolling there.
           */
          val action: Int = ev.actionMasked
          val actionIndex: Int = ev.actionIndex

          return when (action) {
              MotionEvent.ACTION_DOWN -> {
                  initialTouchX = (ev.getX() + 0.5f).toInt()
                  initialTouchY = (ev.getY() + 0.5f).toInt()

                  true
              }
              // Always handle the case of the touch gesture being complete.
              MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                  // Release the scroll.
                  Log.d("ItemClickableMotionLayout", "releasing scroll")
                  isScrolling = false
                  false // Do not intercept touch event, let the child handle it
              }
              MotionEvent.ACTION_MOVE -> {
                  Log.d("ItemClickableMotionLayout", "isScrolling: $isScrolling")
                  if (isScrolling) {
                      // We're currently scrolling, so yes, intercept the
                      // touch event!

                      true
                  } else {

                      // If the user has dragged their finger horizontally more than
                      // the touch slop, start the scroll

                      // left as an exercise for the reader
                      val xDiff: Int =
                          if (ev.historySize > 0) kotlin.math.abs((ev.x - ev.getHistoricalX(0)).toInt()) else 0

                      Log.d("ItemClickableMotionLayout", "xDiff: $xDiff, touchSlop: $mTouchSlop")
                      // Touch slop should be calculated using ViewConfiguration
                      // constants.
                      if (xDiff > mTouchSlop) {
                          // Start scrolling!

                          isScrolling = true
                          true
                      } else {
                          isScrolling = false
                          false
                      }
                  }
              }

              else -> {
                  // In general, we don't want to intercept touch events. They should be
                  // handled by the child view.
                  isScrolling = false
                  false
              }
          }
      }
  */
}
