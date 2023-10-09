package com.example.kolekcja

import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database: CollectionDatabase = CollectionDatabase(this)
        val layout = findViewById<LinearLayout>(R.id.ButtonHolder)

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
                val button = Button(this)
                val row = arrayOf(cursor.getString(0),cursor.getString(1))
                button.text = row[1]

                button.setOnClickListener {
                    val intent = Intent(this, TastesActivity::class.java)
                    intent.putExtra("company",row[1])
                    intent.putExtra("company_id",row[0])
                    startActivity(intent)
                }

                button.setOnLongClickListener{
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
                layout.addView(button)
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