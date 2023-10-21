package com.example.kolekcja

import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout.Alignment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.resources.TextAppearance

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database: CollectionDatabase = CollectionDatabase(this)
        val layout = findViewById<LinearLayout>(R.id.ButtonHolder)

        findViewById<FloatingActionButton>(R.id.addCompany).imageTintList = ContextCompat.getColorStateList(this,R.color.background)

        val cursor: Cursor = database.getCompanies()
        if(cursor.count == 0) {
            val text = TextView(this)
            text.text = "Nie dodano jeszcze żadnych marek."
            text.textSize = 24F
            text.setTextColor(resources.getColor(R.color.primary))
            layout.addView(text)
        }
        else {
            for (i in 0 until cursor.count) {
                cursor.moveToNext()
                val row = arrayOf(cursor.getString(0),cursor.getString(1))
                val container = LinearLayout(this)
                container.orientation = LinearLayout.HORIZONTAL
                container.gravity = Gravity.RIGHT
                container.setPadding(25,30,25,30)
                container.layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )

                val companyText = TextView(this)
                companyText.text = row[1]
                companyText.setTextColor(getColor(R.color.primary))
                companyText.textSize = 24F
                companyText.layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    1F
                )
                companyText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                val percentage = TextView(this)
                percentage.layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    3F
                )
                percentage.text = database.getTastesCount(row[0])
                percentage.setTextColor(getColor(R.color.primary))
                percentage.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                val button = Button(this)
                button.text = row[1]

                container.setOnClickListener {
                    val intent = Intent(this, TastesActivity::class.java)
                    intent.putExtra("company",row[1])
                    intent.putExtra("company_id",row[0])
                    startActivity(intent)
                    percentage.text = database.getTastesCount(row[0])
                }

                container.setOnLongClickListener{
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("")
                        .setMessage("Czy na pewno chcesz usunąć firmę?")
                        .setPositiveButton("Usuń") { dialog, _ ->
                            // Handle OK button click
                            CollectionDatabase(this).removeCompany(row[0])
                            refresh()

                            dialog.dismiss() // Dismiss the dialog
                        }
                        .create()

                    alertDialog.show()

                    false
                }

                container.addView(percentage)
                container.addView(companyText)
                layout.addView(container)
            }
        }
    }

    private fun refresh() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    fun addCompany(view: View) {
        val input = EditText(this)
        input.hint = "Podaj nazwę marki"

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Dodaj firmę")
            .setView(input)
            .setPositiveButton("Dodaj") { dialog, _ ->
                val inputText = input.text.toString()
                // Handle OK button click
                try {
                    CollectionDatabase(this).addCompany(inputText)
                    refresh()

                    Toast.makeText(this, "Dodano firmę", Toast.LENGTH_SHORT).show()
                }
                catch(e: Exception) {
                    Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
                dialog.dismiss() // Dismiss the dialog
            }
            .create()

        alertDialog.show()
    }
}