package com.example.kolekcja

import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TastesActivity : AppCompatActivity() {
    private var company: String? = String()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tastes)

        val database = CollectionDatabase(this)
        val layout = findViewById<LinearLayout>(R.id.Tastes)
        val cursor: Cursor = database.getTastes(intent.getStringExtra("company_id")!!)

        this.company = intent.getStringExtra("company")

        findViewById<TextView>(R.id.Company).text = company
        findViewById<FloatingActionButton>(R.id.addTasteButton).imageTintList = ContextCompat.getColorStateList(this,R.color.background)

        for (i in 0 until cursor.count) {
            cursor.moveToNext()
            val row = arrayOf(cursor.getString(0),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5))
            val mainContainer = LinearLayout(this)
            mainContainer.gravity = Gravity.CENTER_VERTICAL
            mainContainer.setPadding(25,0,25,0)
            mainContainer.setOnLongClickListener {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("Czy na pewno chcesz usunąć smak?")
                    .setPositiveButton("Usuń") { dialog, _ ->
                        // Handle OK button click
                        CollectionDatabase(this).removeTaste(row[0])
                        refresh()

                        dialog.dismiss() // Dismiss the dialog
                    }
                    .create()

                alertDialog.show()

                false
            }

            val textContainer = LinearLayout(this)
            textContainer.orientation = LinearLayout.VERTICAL
            textContainer.setPadding(20,20,20,20)

            val tasteText = TextView(this)
            tasteText.setTextColor(getColor(R.color.primary))
            tasteText.textSize = 22F
            tasteText.text = row[1]

            val sizeText = TextView(this)
            sizeText.setTextColor(getColor(R.color.primary))
            sizeText.text = row[4]

            textContainer.addView(tasteText)
            textContainer.addView(sizeText)


            val canMain: Drawable?
            val canBorder: Drawable?
            val canShadow: Drawable?

            if(row[4]=="250ml") {
                canMain = ContextCompat.getDrawable(this,R.drawable.ic_can_250ml_main)
                canBorder = ContextCompat.getDrawable(this,R.drawable.ic_can_250ml_borders)
                canShadow = ContextCompat.getDrawable(this,R.drawable.ic_can_250ml_shadow)
            }
            else if(row[4]=="330ml") {
                canMain = ContextCompat.getDrawable(this,R.drawable.ic_can_330ml_main)
                canBorder = ContextCompat.getDrawable(this,R.drawable.ic_can_330ml_borders)
                canShadow = ContextCompat.getDrawable(this,R.drawable.ic_can_330ml_shadow)
            }
            else {
                canMain = ContextCompat.getDrawable(this,R.drawable.ic_can_500ml_main)
                canBorder = ContextCompat.getDrawable(this,R.drawable.ic_can_500ml_borders)
                canShadow = ContextCompat.getDrawable(this,R.drawable.ic_can_500ml_shadow)
            }
            try {
                if(row[5].isNotEmpty()) {
                    val canColor = Color.parseColor(row[5])
                    canMain?.setColorFilter(canColor, android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
            catch(_: IllegalArgumentException) {
                canMain?.setColorFilter(Color.argb(64,128,32,32), android.graphics.PorterDuff.Mode.SRC_IN)
                canBorder?.setColorFilter(Color.argb(128,128,0,0), android.graphics.PorterDuff.Mode.SRC_IN)
            }


            val canArray = arrayOf(
                canMain,
                canBorder,
                canShadow
            )
            /*if(drawableExists(row[4],this.company.toString())) {
                val resource = "ic_can_${row[4]}_trim_${this.company?.lowercase()}"
                val resourceId = resources.getIdentifier(resource,"drawable",packageName)
                val canTrim: Drawable? = ContextCompat.getDrawable(this, resourceId)
                canArray = arrayOf(
                    canMain,
                    canTrim,
                    canShadow,
                    canBorder
                )
            }*/

            val fullCan = LayerDrawable(canArray)

            val can = ImageView(this)
            can.setImageDrawable(fullCan)
            can.layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.can_width),
                resources.getDimensionPixelSize(R.dimen.can_height)
            )
            if(row[3]=="0") {
                tasteText.alpha = 0.6F
                sizeText.alpha = 0.6F
            }
            mainContainer.setOnClickListener {

                if(tasteText.alpha==0.6F) {
                    tasteText.alpha = 1F
                    sizeText.alpha = 1F
                    CollectionDatabase(this).updateTaste(row[0], true)
                }
                else {
                    tasteText.alpha = 0.6F
                    sizeText.alpha = 0.6F
                    CollectionDatabase(this).updateTaste(row[0], false)
                }
            }
            can.setOnLongClickListener {
                val color = EditText(this)
                color.hint = row[5]
                color.highlightColor = getColor(R.color.primary)
                color.doAfterTextChanged {
                    if(it!!.isNotEmpty()) {
                        try {
                            val textColor = Color.parseColor(it.toString())
                            color.setTextColor(textColor)
                        } catch (e: IllegalArgumentException) {
                            color.setTextColor(ContextCompat.getColor(this, R.color.primary))
                        }
                    }
                }

                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Edytuj kolor")
                    .setView(color)
                    .setPositiveButton("Zmień") { dialog, _ ->
                        // Handle OK button click
                        CollectionDatabase(this).changeTasteColor(row[0],color.text.toString())
                        refresh()

                        dialog.dismiss() // Dismiss the dialog
                    }
                    .create()

                alertDialog.show()

                false
            }

            mainContainer.addView(can)
            mainContainer.addView(textContainer)
            layout.addView(mainContainer)
        }
    }

    private fun refresh() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    fun closeActivity() {
        finish()
    }

    fun addTaste(view: View) {
        val container: LinearLayout = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        val taste = EditText(this)
        taste.hint = "Smak"

        val size = Spinner(this)
        val items = arrayOf("250ml","330ml","500ml")
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        size.adapter = adapter

        val color = EditText(this)
        color.hint = "#888888"
        color.doAfterTextChanged {
            if(it!!.isNotEmpty()) {
                try {
                    val textColor = Color.parseColor(it.toString())
                    color.setTextColor(textColor)
                } catch (e: IllegalArgumentException) {
                    color.setTextColor(ContextCompat.getColor(this, R.color.primary))
                }
            }
        }

        container.addView(taste)
        container.addView(size)
        container.addView(color)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Dodaj smak")
            .setView(container)
            .setPositiveButton("W kolekcji") { dialog, _ ->
                val tasteText = taste.text.toString()
                val colorText = color.text.toString()
                val sizeText = items[size.selectedItemPosition]

                // Handle OK button click
                CollectionDatabase(this).addTaste(tasteText, sizeText, colorText, "1",intent.getStringExtra("company_id")!!)
                refresh()

                Toast.makeText(this, "Dodano smak", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss the dialog
            }
            .setNegativeButton("Do zdobycia") { dialog, _ ->
                val tasteText = taste.text.toString()
                val colorText = color.text.toString()
                val sizeText = items[size.selectedItemPosition]

                CollectionDatabase(this).addTaste(tasteText, sizeText, colorText, "0",intent.getStringExtra("company_id")!!)
                refresh()

                Toast.makeText(this, "Dodano smak", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss the dialog
            }

            .create()

        alertDialog.show()
    }

    private fun drawableExists(size: String, company: String): Boolean {
        return try {
            val resource = "ic_can_${size}_trim_${company.lowercase()}"
            val resourceId = resources.getIdentifier(resource,"drawable",packageName)
            val drawable: Drawable? = ContextCompat.getDrawable(this, resourceId)
            drawable != null
        } catch (e: Exception) {
            false
        }
    }
}