package sensors_in_paradise.sonar.page2.labels_editor

import android.content.Context
import android.util.AttributeSet

class ClickableCarouselTextView(context: Context, attrSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatTextView(context, attrSet) {
    private var onClickListener: OnClickListener? = null
    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }

    override fun performClick(): Boolean {
        onClickListener?.onClick(this)
        return super.performClick()
    }
}
