package com.planetbarcode.tacoma

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.cardview.widget.CardView

class Setup : AppCompatActivity() {
    companion object{
        lateinit var spinnerFactory:Spinner
        lateinit var editTextDatabaseServer: EditText
        lateinit var editTextDatabaseName: EditText
        lateinit var editTextDatabaseUser: EditText
        lateinit var editTextDatabasePassword: EditText
        lateinit var buttonSave: Button
        lateinit var cardViewBack : CardView
        var factoryList = ArrayList<String>()
        var factoryListIndex = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        spinnerFactory = findViewById(R.id.spinner_factory)
        editTextDatabaseServer = findViewById(R.id.editText_database_server)
        editTextDatabaseName = findViewById(R.id.editText_database_name)
        editTextDatabaseUser = findViewById(R.id.editText_database_user)
        editTextDatabasePassword = findViewById(R.id.editText_database_password)
        cardViewBack = findViewById(R.id.cardView_back)
        buttonSave = findViewById(R.id.button_save)

        factoryList.clear()
        factoryList.add("Factory1")
        factoryList.add("Factory2")


        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, factoryList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFactory.adapter = arrayAdapter

        editTextDatabaseServer.setText(Login.databaseServer)
        editTextDatabaseName.setText(Login.databaseName)
        editTextDatabaseUser.setText(Login.databaseUser)
        editTextDatabasePassword.setText(Login.databasePassword)

        for(i in 0 until factoryList.size){
            if(Login.factory == factoryList[i]){
                factoryListIndex = i
            }
        }
        spinnerFactory.setSelection(factoryListIndex)

        buttonSave.setOnClickListener{
            when{
                editTextDatabaseServer.text.toString() == "" -> {Toast.makeText(this,"Please Enter Database Server",Toast.LENGTH_LONG).show()}
                editTextDatabaseName.text.toString() == "" -> {Toast.makeText(this,"Please Enter Database Name",Toast.LENGTH_LONG).show()}
                editTextDatabaseUser.text.toString() == "" -> {Toast.makeText(this,"Please Enter Database User",Toast.LENGTH_LONG).show()}
                editTextDatabasePassword.text.toString() == "" -> {Toast.makeText(this,"Please Enter Database Password",Toast.LENGTH_LONG).show()}
                else -> {
                    setFactory(spinnerFactory.selectedItem.toString())
                    setDatabaseServer(editTextDatabaseServer.text.toString())
                    setDatabaseName(editTextDatabaseName.text.toString())
                    setDatabaseUser(editTextDatabaseUser.text.toString())
                    setDatabasePassword(editTextDatabasePassword.text.toString())
                    Toast.makeText(this,"Save Successful",Toast.LENGTH_LONG).show()
                    loadFactory()
                    loadDatabaseServer()
                    loadDatabaseName()
                    loadDatabaseUser()
                    loadDatabasePassword()
                }
            }
        }

        cardViewBack.setOnClickListener {
            finish();
            super.onBackPressed();
        }

    }

    private fun setFactory(v: String){
        var editor = getSharedPreferences("factory", Activity.MODE_PRIVATE).edit()
        editor.putString("valFactory", v)
        editor.apply()
    }

    private fun setDatabaseServer(v: String){
        var editor = getSharedPreferences("databaseServer", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseServer", v)
        editor.apply()
    }

    private fun setDatabaseName(v: String){
        var editor = getSharedPreferences("databaseName", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseName", v)
        editor.apply()
    }

    private fun setDatabaseUser(v: String){
        var editor = getSharedPreferences("databaseUser", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseUser", v)
        editor.apply()
    }

    private fun setDatabasePassword(v: String){
        var editor = getSharedPreferences("databasePassword", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabasePassword", v)
        editor.apply()
    }

    private fun loadFactory() {
        var prefs = getSharedPreferences("factory", Activity.MODE_PRIVATE)
        Login.factory = prefs.getString("valFactory", "").toString()
    }

    private fun loadDatabaseServer() {
        var prefs = getSharedPreferences("databaseServer", Activity.MODE_PRIVATE)
        Login.databaseServer = prefs.getString("valDatabaseServer", "").toString()
    }

    private fun loadDatabaseName() {
        var prefs = getSharedPreferences("databaseName", Activity.MODE_PRIVATE)
        Login.databaseName = prefs.getString("valDatabaseName", "").toString()
    }

    private fun loadDatabaseUser() {
        var prefs = getSharedPreferences("databaseUser", Activity.MODE_PRIVATE)
        Login.databaseUser = prefs.getString("valDatabaseUser", "").toString()
    }

    private fun loadDatabasePassword() {
        var prefs = getSharedPreferences("databasePassword", Activity.MODE_PRIVATE)
        Login.databasePassword = prefs.getString("valDatabasePassword", "").toString()
    }
}