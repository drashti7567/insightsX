package com.example.diceroller.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.diceroller.R
import com.example.diceroller.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity: BaseActivity() {

    private val myCalendar: Calendar = Calendar.getInstance()

    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(SharedPreferencesUtils.getMemberId(this) != null)
            setContentView(R.layout.sign_up)

        this.addDatepickerForBirthDate()
    }

    private fun addDatepickerForBirthDate() {
        editText = findViewById<View>(R.id.birthDate) as EditText

        val date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        editText!!.setOnClickListener {
            DatePickerDialog(
                this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateLabel() {
        val myFormat = "MM/dd/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editText?.setText(sdf.format(myCalendar.time))
    }
}