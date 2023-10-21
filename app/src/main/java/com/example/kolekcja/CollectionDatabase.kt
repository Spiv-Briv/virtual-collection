package com.example.kolekcja

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class CollectionDatabase(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val COMPANY_TABLE = "companies"
    private val TASTES_TABLE = "company_tastes"
    companion object {
        private const val DATABASE_NAME = "Kolecja.db"
        private const val DATABASE_VERSION = 8
    }

    override fun onCreate(db: SQLiteDatabase) {
        val queries = arrayOf(
            "CREATE TABLE $COMPANY_TABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT DEFAULT \"unnamed\" NOT NULL)",
            "CREATE TABLE $TASTES_TABLE (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT DEFAULT \"unnamed\" NOT NULL, " +
                    "company_id INTEGER, " +
                    "collected INTEGER, " +
                    "capacity TEXT, " +
                    "color TEXT DEFAULT \"#000000\", " +
                    "FOREIGN KEY (company_id) REFERENCES $COMPANY_TABLE(id)" +
                    ")"
        )
        for (i in queries.indices) {
            db.execSQL(queries[i])
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if(oldVersion<newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $COMPANY_TABLE")
            db.execSQL("DROP TABLE IF EXISTS $TASTES_TABLE")
            onCreate(db)
        }
    }

    fun addCompany(name: String) {
        val db: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put("name",name)

        val result = db.insert(COMPANY_TABLE, null, values)
        if(result== (-1).toLong()) {
            Toast.makeText(context, "Nie udało się dodać", Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(context, "Dodano", Toast.LENGTH_LONG).show()
        }
    }

    fun getCompanies(id: String? = null): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        if(id==null) {
            return db.rawQuery("SELECT `$COMPANY_TABLE`.`id`, `$COMPANY_TABLE`.`name`, COUNT(`$TASTES_TABLE`.`id`) AS `Taste count` FROM `$COMPANY_TABLE` LEFT JOIN `$TASTES_TABLE` ON `$COMPANY_TABLE`.`id`=`$TASTES_TABLE`.`company_id` GROUP BY `$COMPANY_TABLE`.`id` ORDER BY `Taste count` DESC, `$COMPANY_TABLE`.`name`",null)
            return db.query(COMPANY_TABLE, null, null, null, null, null, "name")
        }
        return db.query(COMPANY_TABLE,null,"id=?", arrayOf(id),null,null,null)
    }

    fun getTastesCount(companyId: String): String {
        val db: SQLiteDatabase = this.writableDatabase
        val itemsCount = db.query(TASTES_TABLE,null,"company_id=?", arrayOf(companyId),null,null,null).count

        val activeItemsCursor = db.rawQuery("SELECT COUNT(*) FROM $TASTES_TABLE WHERE company_id=$companyId AND collected=1",null)
        activeItemsCursor.moveToNext()
        val activeItems = activeItemsCursor.getString(0)
        return "$activeItems / $itemsCount"
    }

    fun removeCompany(id: String) {
        val db: SQLiteDatabase = this.writableDatabase
        db.delete(COMPANY_TABLE,"id=?", arrayOf(id))
    }

    fun addTaste(taste: String, size: String, color: String, collected: String, company: String) {
        val db: SQLiteDatabase = this.writableDatabase
        val values: ContentValues = ContentValues()
        values.put("name",taste)
        values.put("capacity", size)
        values.put("color",color)
        values.put("company_id",company)
        values.put("collected", collected)

        db.insert(TASTES_TABLE, null, values)
    }

    fun getTastes(company: String, id: String? = null): Cursor {
        val db = this.writableDatabase
        if(id==null) {
            return db.query(TASTES_TABLE, null, "company_id=?", arrayOf(company),null,null,"collected DESC, capacity DESC, name")
        }
        return db.query(TASTES_TABLE, null, "company_id=? AND id=?", arrayOf(company,id),null,null,null)
    }

    fun updateTaste(id: String, active: Boolean): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        if(active) {
            values.put("collected","1")
        }
        else {
            values.put("collected","0")
        }
        return db.update(TASTES_TABLE,values,"id=?", arrayOf(id))

    }

    fun changeTasteColor(id: String, newColor: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("color",newColor)

        return db.update(TASTES_TABLE,values, "id=?", arrayOf(id))
    }

    fun removeTaste(id: String) {
        val db: SQLiteDatabase = this.writableDatabase
        db.delete(TASTES_TABLE,"id=?", arrayOf(id))
    }
}