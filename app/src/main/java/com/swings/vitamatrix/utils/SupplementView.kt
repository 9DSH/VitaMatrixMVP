package com.swings.vitamatrix.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.swings.vitamatrix.R

class SupplementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        inflate(context, R.layout.item_supplement, this)
    }

    fun bind(supplement: HealthItem) {
        findViewById<TextView>(R.id.name).text = supplement.name
        findViewById<TextView>(R.id.dosage).text = supplement.dosage
        findViewById<TextView>(R.id.purpose).text = supplement.purpose
    }

    data class Supplement(val name: String, val dosage: String, val purpose: String)
}