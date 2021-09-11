package com.example.diceroller.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.example.diceroller.R
import com.example.diceroller.constants.ApiUrlConstants
import com.example.diceroller.utils.HttpUtils
import com.example.diceroller.utils.SharedPreferencesUtils
import com.google.android.material.textfield.TextInputEditText
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class EnterMemberIdActivity: BaseActivity() {

    private var toolbar: Toolbar? = null
    private var memberIdText: TextInputEditText? = null
    private var checkMemberButton: Button? = null

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(SharedPreferencesUtils.getMemberId(this) != null)
            this.navigateToAskPermissionsActivity()
        setContentView(R.layout.enter_member_id)
        this.setupUI(findViewById(R.id.parent))

        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        memberIdText = findViewById<View>(R.id.member_id_input) as TextInputEditText
        setSupportActionBar(toolbar)

        checkMemberButton = findViewById<View>(R.id.check_if_member_exists_button) as Button
        checkMemberButton!!.setOnClickListener(View.OnClickListener {
            this.checkIfMemberExists()
        })
    }

    private fun checkIfMemberExists() {
        val memberId: String = memberIdText?.text.toString()
        val context = this;

        HttpUtils.get("${ApiUrlConstants.checkMemberExists}${memberId}", RequestParams(), object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header?>?, response: JSONObject) {
                try {
                    val serverResp = JSONObject(response.toString())
                    if(serverResp.get("success") == true) {
                        SharedPreferencesUtils.setMemberId(context, memberId)
                        context.navigateToAskPermissionsActivity()
                    }
                    else {
                        Toast.makeText(context, "Member with MemberId $memberId is not registered in the system.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun navigateToAskPermissionsActivity() {
        val startAskPermissionsActivity: Intent = Intent(this, AskPermissionsActivity::class.java)
        startActivity(startAskPermissionsActivity)
        this.finish()
    }

}