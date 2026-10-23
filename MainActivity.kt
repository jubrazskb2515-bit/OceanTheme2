package com.custom.darkglasswater

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.WindowManager
import android.view.MotionEvent
import android.view.View
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.view.Gravity
import android.graphics.Typeface
import kotlin.math.*

class MainActivity : Activity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelX = 0f
    private var accelY = 0f
    private lateinit var waterView: WaterPanel
    private val notesPrefs by lazy { getSharedPreferences("glass_notes", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(3, 6, 17)
        window.navigationBarColor = Color.rgb(3, 6, 17)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        waterView = WaterPanel(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(3, 6, 18), Color.rgb(8, 12, 32), Color.rgb(2, 5, 15)))
        }
        val status = TextView(this).apply {
            text = "10:24     ◈  5G     Wi-Fi     78%"
            textSize = 13f; setTextColor(Color.rgb(170, 220, 255))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(12))
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        root.addView(status, LinearLayout.LayoutParams(-1, dp(34)))
        root.addView(waterView, LinearLayout.LayoutParams(-1, 0, 1f))
        val hint = TextView(this).apply {
            text = "TILT YOUR PHONE  •  WATER FOLLOWS GRAVITY"
            textSize = 11f; letterSpacing = .08f
            setTextColor(Color.rgb(80, 218, 255)); gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))
        }
        root.addView(hint, LinearLayout.LayoutParams(-1, dp(38)))
        setContentView(root)
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun openNotes() {
        val editor = EditText(this).apply {
            setText(notesPrefs.getString("note", ""))
            hint = "Write your note here..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            minLines = 5
            gravity = Gravity.TOP
            background = GradientDrawable().apply {
                setColor(Color.rgb(8, 18, 42))
                setStroke(dp(1), Color.rgb(0, 205, 255))
                cornerRadius = dp(14).toFloat()
            }
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(editor, LinearLayout.LayoutParams(-1, dp(190)))
        }
        AlertDialog.Builder(this)
            .setTitle("✦ Better Things Ahead — Notes")
            .setView(container)
            .setPositiveButton("Save") { _, _ -> notesPrefs.edit().putString("note", editor.text.toString()).apply() }
            .setNegativeButton("Close", null)
            .show()
    }
    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    override fun onPause() { super.onPause(); sensorManager.unregisterListener(this) }
    override fun onSensorChanged(e: SensorEvent) {
        if (e.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelX = accelX * .72f + (-e.values[0] / 9.81f).coerceIn(-1f, 1f) * .28f
            accelY = accelY * .72f + (e.values[1] / 9.81f).coerceIn(-1f, 1f) * .28f
            waterView.setTilt(accelX, accelY)
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private inner class WaterPanel(ctx: Context) : View(ctx) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private var tx = 0f; private var ty = 0f
        private var noteRect = RectF()
        private var downX = 0f; private var downY = 0f
        private val cyan = Color.rgb(0, 205, 255)
        private val pink = Color.rgb(255, 48, 210)
        private val green = Color.rgb(26, 255, 145)
        private val yellow = Color.rgb(255, 206, 55)
        private val blue = Color.rgb(70, 115, 255)
        fun setTilt(x: Float, y: Float) { tx = x; ty = y; invalidate() }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            val gap = dp(12).toFloat()
            val leftW = w * .61f
            val rightX = leftW + gap
            val clock = RectF(0f, 0f, leftW, h*.245f)
            val weather = RectF(rightX, 0f, w, h*.205f)
            val search = RectF(0f, clock.bottom+gap, leftW, clock.bottom+gap+dp(56))
            val quote = RectF(rightX, weather.bottom+gap, w, h*.56f)
            noteRect = RectF(quote)
            val rowY = search.bottom+gap
            val cellGap = gap*.6f
            val cellW = (leftW-cellGap*3)/4f
            val cellH = h*.115f
            val appRects = mutableListOf<RectF>()
            for (r in 0..1) for (c in 0..3) {
                val x = c*(cellW+cellGap)
                val y = rowY + r*(cellH+dp(38))
                appRects.add(RectF(x,y,x+cellW,y+cellH))
            }
            val battery = RectF(rightX, quote.bottom+gap, w, min(h*.88f, quote.bottom+gap+dp(126)))
            drawPanel(canvas, clock, cyan, .26f, "10:24", "TUE  •  GLASS MODE")
            drawPanel(canvas, weather, yellow, .34f, "28°", "PARTLY CLOUDY")
            drawPanel(canvas, search, blue, .5f, "⌕  Search", "VOICE     CAMERA")
            drawPanel(canvas, quote, pink, .18f, "BETTER", "THINGS AHEAD  ✦")
            drawPanel(canvas, battery, green, .25f, "78%  CHARGING", "POWER • LIQUID GLASS")
            val labels = listOf("WhatsApp","YouTube","Chrome","Gallery","Phone","Camera","Settings","Store")
            val colors = listOf(green, Color.rgb(255,65,80), yellow, pink, cyan, blue, cyan, green)
            val glyphs = listOf("◉","▶","●","▧","☎","◎","⚙","➤")
            for (i in appRects.indices) drawPanel(canvas, appRects[i], colors[i], .55f, glyphs[i], labels[i])
            val dock = RectF(0f, h-dp(67).toFloat(), w, h)
            drawPanel(canvas, dock, cyan, .48f, "☎       ▤       ⠿       ◉       ◎", "HOME DOCK")
        }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x; downY = event.y
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (abs(dx) < dp(12) && abs(dy) < dp(12) && noteRect.contains(event.x, event.y)) {
                        openNotes()
                        return true
                    }
                    performClick()
                    return true
                }
            }
            return true
        }
        override fun performClick(): Boolean {
            super.performClick()
            return true
        }
        private fun drawPanel(c: Canvas, r: RectF, accent: Int, waterLevel: Float, title: String, subtitle: String) {
            if (r.width() < 2 || r.height() < 2) return
            val radius = dp(17).toFloat()
            p.shader = null; p.style = Paint.Style.FILL
            p.color = Color.argb(225, 5, 13, 34)
            c.drawRoundRect(r, radius, radius, p)
            p.color = Color.argb(95, Color.red(accent), Color.green(accent), Color.blue(accent))
            c.drawRoundRect(r, radius, radius, p)
            c.save()
            val path = Path().apply { addRoundRect(r, radius, radius, Path.Direction.CW) }
            c.clipPath(path)
            val base = r.top + r.height()*(waterLevel + ty*.12f).coerceIn(.25f,.8f)
            val wave = Path().apply {
                moveTo(r.left-4, base)
                for (i in 0..24) {
                    val x = r.left + r.width()*i/24f
                    val y = base + sin(i*.48 + tx*3.2).toFloat()*r.height()*.045f +
                        sin(i*.23 + ty*2.1).toFloat()*r.height()*.025f
                    lineTo(x,y)
                }
                lineTo(r.right+4,r.bottom+4); lineTo(r.left-4,r.bottom+4); close()
            }
            p.color = Color.argb(115, Color.red(accent), Color.green(accent), Color.blue(accent))
            c.drawPath(wave,p)
            p.style = Paint.Style.STROKE; p.strokeWidth = dp(2).toFloat()
            p.color = Color.argb(220, Color.red(accent), Color.green(accent), Color.blue(accent))
            val line = Path().apply {
                moveTo(r.left-4, base)
                for (i in 0..24) lineTo(r.left+r.width()*i/24f, base + sin(i*.48+tx*3.2).toFloat()*r.height()*.045f)
            }
            c.drawPath(line,p)
            p.style = Paint.Style.FILL
            for (i in 0..4) {
                val bx = r.left + r.width()*(.18f+i*.15f) + tx*dp(7)
                val by = base + r.height()*(.18f+(i%3)*.13f) - abs(ty)*dp(3)
                p.color = Color.argb(150, 180, 240, 255)
                c.drawCircle(bx, by, dp((i%2+1)).toFloat(), p)
            }
            c.restore()
            p.style = Paint.Style.STROKE; p.strokeWidth = dp(1).toFloat()
            p.shader = LinearGradient(r.left,r.top,r.right,r.bottom,
                intArrayOf(Color.argb(240,255,255,255),accent,Color.argb(100,255,255,255)),null,Shader.TileMode.CLAMP)
            c.drawRoundRect(r, radius, radius, p); p.shader = null
            p.style = Paint.Style.FILL
            val pad = dp(12).toFloat()
            val big = r.height() > dp(90)
            p.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            p.textSize = if (big) min(dp(27).toFloat(),r.height()*.24f) else min(dp(18).toFloat(),r.height()*.32f)
            p.setShadowLayer(dp(7).toFloat(),0f,0f,accent)
            p.color = Color.WHITE
            c.drawText(title, r.left+pad, r.top+r.height()*(if(big).42f else .48f), p)
            p.clearShadowLayer()
            if (big && subtitle.isNotEmpty()) {
                p.textSize = dp(10).toFloat(); p.color = accent
                c.drawText(subtitle, r.left+pad, r.top+r.height()*.61f, p)
            } else if (r.height() > dp(40)) {
                p.textSize = dp(8).toFloat(); p.color = accent
                c.drawText(subtitle, r.left+pad, r.top+r.height()*.78f, p)
            }
        }
    }
}
