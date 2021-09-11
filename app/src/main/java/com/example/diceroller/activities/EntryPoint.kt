package com.example.diceroller.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.diceroller.R
import com.example.diceroller.utils.SharedPreferencesUtils

class EntryPoint: BaseActivity(), View.OnClickListener {

    var enterMemberId: Button? = null
    var signUpButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(SharedPreferencesUtils.getMemberId(this) != null)
            this.navigateToAskPermissionsActivity()

        setContentView(R.layout.app_entry_options)
        this.setupUI(findViewById(R.id.parent))

        this.enterMemberId = findViewById(R.id.enterMemberIdButton)
        this.signUpButton = findViewById(R.id.signUpButton)

        this.enterMemberId?.setOnClickListener(this)
        this.signUpButton?.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        var activityIntent: Intent? = null
        if(view == enterMemberId)
            activityIntent = Intent(this, EnterMemberIdActivity::class.java)
        else if(view == signUpButton)
            activityIntent = Intent(this, SignUpActivity::class.java)

        startActivity(activityIntent)
    }

    private fun navigateToAskPermissionsActivity() {
        val startAskPermissionsActivity: Intent = Intent(this, AskPermissionsActivity::class.java)
        startActivity(startAskPermissionsActivity)
        this.finish()
    }
}