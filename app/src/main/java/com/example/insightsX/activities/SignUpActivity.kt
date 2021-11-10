package com.example.insightsX.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import com.example.insightsX.R
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.constants.MemberConstants
import com.example.insightsX.constants.RegexConstants
import com.example.insightsX.utils.HttpUtils
import com.example.insightsX.utils.SharedPreferencesUtils
import com.google.android.material.textfield.TextInputEditText
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : BaseActivity(), View.OnClickListener {

    private val myCalendar: Calendar = Calendar.getInstance()
    private var firstNameInput: TextInputEditText? = null
    private var lastNameInput: TextInputEditText? = null
    private var emailInput: EditText? = null
    private var phoneNumberInput: EditText? = null
    private var genderRadioGroup: RadioGroup? = null
    private var birthDateInput: EditText? = null
    private var professionInput: TextInputEditText? = null
    private var incomeGroupSpinner: Spinner? = null
    private var termOfServiceCheckBox: CheckBox? = null
    private var privacyPolicyCheckBox: CheckBox? = null
    private var submitButton: Button? = null


    private var editText: EditText? = null
    private var spinnerDropdown: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        if(SharedPreferencesUtils.getMemberId(this) != null &&
            SharedPreferencesUtils.getMemberId(this)!!.isNotEmpty() &&
            SharedPreferencesUtils.getMemberId(this)!!.isNotBlank())
            this.navigateToAskPermissionsActivity()

        this.addDatePickerForBirthDate()
        this.addIncomeGroupSpinnerDropdown()
        this.defineAllFormInputs()
    }

    private fun addDatePickerForBirthDate() {
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

    private fun addIncomeGroupSpinnerDropdown() {
        this.spinnerDropdown = findViewById(R.id.income_group_spinner) as Spinner
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MemberConstants.incomeGroupOptions)
        this.spinnerDropdown!!.adapter = adapter
    }

    private fun defineAllFormInputs() {
        firstNameInput = findViewById<TextInputEditText>(R.id.first_name_input)
        lastNameInput = findViewById<TextInputEditText>(R.id.last_name_input)
        emailInput = findViewById<EditText>(R.id.editTextTextEmailAddress)
        phoneNumberInput = findViewById<EditText>(R.id.editTextPhone)
        genderRadioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        birthDateInput = findViewById<EditText>(R.id.birthDate)
        professionInput = findViewById<TextInputEditText>(R.id.profession_input)
        incomeGroupSpinner = findViewById<Spinner>(R.id.income_group_spinner)
        termOfServiceCheckBox = findViewById<CheckBox>(R.id.term_of_service_checkBox)
        privacyPolicyCheckBox = findViewById<CheckBox>(R.id.privacy_policy_checkBox2)
        submitButton = findViewById<Button>(R.id.sign_up_button)
        submitButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val context = this
        if (view == submitButton) {
            if (this.checkIfAllInputsHaveBeenFilled() && this.checkRegexes()) {
                val entity: StringEntity = this.createSignUpRequestObj()

                HttpUtils.post(this, ApiUrlConstants.selfSignUpMember, entity, "application/json",
                    object : AsyncHttpResponseHandler(true) {

                        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                            try {
                                val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                                if (serverResp.get("success") == true) {
                                    SharedPreferencesUtils.setMemberId(context, serverResp.get("memberId").toString())
                                    context.navigateToAskPermissionsActivity()
                                    context.finish()
                                }
                                else {
                                    context.runOnUiThread {
                                        Toast.makeText(context, serverResp.get("message").toString(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(
                            statusCode: Int,
                            headers: Array<out Header>?,
                            responseBody: ByteArray?,
                            error: Throwable?) {
                            Log.d("Error", error.toString() + " " + responseBody.toString())
                        }
                    })
            }
        }
    }

    private fun createSignUpRequestObj(): StringEntity {
        val requestObj = JSONObject()
        requestObj.put("birthDate", this.birthDateInput?.text.toString())
        requestObj.put("email", this.emailInput?.text)
        requestObj.put("gender", (findViewById<RadioButton>(genderRadioGroup!!.checkedRadioButtonId)).text)
        requestObj.put("incomeGroup", this.spinnerDropdown?.selectedItem.toString())
        requestObj.put("name", "${this.firstNameInput?.text} ${this.lastNameInput?.text}")
        requestObj.put("phoneNumber", this.phoneNumberInput?.text)
        requestObj.put("workingCompany", this.professionInput?.text)

        return StringEntity(requestObj.toString(), "UTF-8")
    }

    private fun checkIfAllInputsHaveBeenFilled(): Boolean {
        if (!this.checkIfCheckBoxesAreChecked()) {
            Toast.makeText(this, "Please agree to our terms of conditions", Toast.LENGTH_SHORT).show()
            return false
        }

        if (genderRadioGroup?.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return false
        }
        if (firstNameInput!!.text == null || lastNameInput!!.text == null ||
            emailInput!!.text == null || phoneNumberInput!!.text == null ||
            birthDateInput!!.text == null || professionInput!!.text == null ||
            firstNameInput!!.text.toString() == "" || lastNameInput!!.text.toString() == "" ||
            emailInput!!.text.toString() == "" || phoneNumberInput!!.text.toString() == "" ||
            birthDateInput!!.text.toString() == "" || professionInput!!.text.toString() == "") {
            Toast.makeText(
                this, "Please fill all inputs before clicking submit button",
                Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkRegexes(): Boolean {
        if (!this.emailInput?.let { RegexConstants.emailRegex.toRegex().matches(it.text) }!!) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!this.phoneNumberInput?.let { RegexConstants.phoneNumberRegex.toRegex().matches(it.text) }!!) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun checkIfCheckBoxesAreChecked(): Boolean {
        return (this.termOfServiceCheckBox?.isChecked() == true && this.privacyPolicyCheckBox?.isChecked() == true)
    }

    private fun navigateToAskPermissionsActivity() {
        val startAskPermissionsActivity: Intent = Intent(this, AskPermissionsActivity::class.java)
        startActivity(startAskPermissionsActivity)
        this.finish()
    }


}