package com.planetbarcode.tacoma

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class Login : AppCompatActivity() {
    companion object{
        lateinit var editTextUser:EditText
        lateinit var editTextPassword:EditText
        lateinit var buttonLogin:Button
        lateinit var cardView:androidx.cardview.widget.CardView
        var mUser = ""
        var mEmployee = ""
        var mAccessLevel = ""
        var factory = ""
        var databaseServer = ""
        var databaseName = ""
        var databaseUser = ""
        var databasePassword = ""

        var Ip = "192.168.1.193:1433"
        var printerIp = "192.168.1.2"
        var user = "sa"
        var password = "garfield"
        var db = "dbBarcode"

//        var Ip = "192.168.1.38:1433"
//        var printerIp = "192.168.1.2"
//        var user = "sa"
//        var password = "261197"
//        var db = "dbBarcode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUser = findViewById(R.id.edt_user)
        editTextPassword = findViewById(R.id.edt_password)
        buttonLogin = findViewById(R.id.button_login)
        cardView = findViewById(R.id.layout_setting)

        loadFactory()
        loadDatabaseServer()
        loadDatabaseName()
        loadDatabaseUser()
        loadDatabasePassword()

        buttonLogin.setOnClickListener {
            if(editTextUser.text.toString().replace(" ","") != "" && editTextPassword.text.toString().replace(" ","") != ""){
                when{
                    databaseServer == "" -> {Toast.makeText(this,"Please Check Database Server",Toast.LENGTH_LONG).show()}
                    databaseName == "" -> {Toast.makeText(this,"Please Check Database Name",Toast.LENGTH_LONG).show()}
                    databaseUser == "" -> {Toast.makeText(this,"Please Check Database User",Toast.LENGTH_LONG).show()}
                    databasePassword == "" -> {Toast.makeText(this,"Please Check Database Password",Toast.LENGTH_LONG).show()}
                    else -> {
                        if(login(editTextUser.text.toString(), editTextPassword.text.toString())){
                            var intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            editTextUser.selectAll()
                            editTextUser.requestFocus()
                        }
                    }
                }
            }
            else{
                Toast.makeText(this,"Please enter username and password",Toast.LENGTH_SHORT).show()
            }
        }

        cardView.setOnClickListener{
            val intent = Intent(this,Setup::class.java)
            startActivity(intent)

        }
    }

    fun login(username:String, userpassword:String):Boolean{
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? =null
        var name = ""
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            var ConnURL = ("jdbc:jtds:sqlserver://$databaseServer:1433/$databaseName;encrypt=fasle;user=$databaseUser;password=$databasePassword;")
            DriverManager.setLoginTimeout(3)
            conn = DriverManager.getConnection(ConnURL)

            val statement = conn.prepareStatement("{call [dbo].[isLogin](?,?)}")
            statement.setString(1, "$username")
            statement.setString(2, "$userpassword")
            result = statement.executeQuery()

            while (result.next()){
                name =  result.getString("employee_name")
            }

            return try{
                if(name != ""){
                    mUser = username
                    mEmployee = name
                    return try{
                        while (result.next()){
                            mAccessLevel =  result.getString("access_level")
                        }
                        true
                    }catch (e:Exception){
                        e.printStackTrace()
                        mAccessLevel = ""
                        Toast.makeText(this,"Login failed : AccessLevel Denied",Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                else{
                    Toast.makeText(this,"Login failed : User Not Found",Toast.LENGTH_SHORT).show()
                    conn.close()
                    false
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this,"Login failed : User Not Found",Toast.LENGTH_SHORT).show()
                conn.close()
                false
            }
        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(this,"Cannot connect to database",Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun loadFactory() {
        var prefs = getSharedPreferences("factory", Activity.MODE_PRIVATE)
        factory = prefs.getString("valFactory", "").toString()
    }

    private fun loadDatabaseServer() {
        var prefs = getSharedPreferences("databaseServer", Activity.MODE_PRIVATE)
        databaseServer = prefs.getString("valDatabaseServer", "").toString()
    }

    private fun loadDatabaseName() {
        var prefs = getSharedPreferences("databaseName", Activity.MODE_PRIVATE)
        databaseName = prefs.getString("valDatabaseName", "").toString()
    }

    private fun loadDatabaseUser() {
        var prefs = getSharedPreferences("databaseUser", Activity.MODE_PRIVATE)
        databaseUser = prefs.getString("valDatabaseUser", "").toString()
    }

    private fun loadDatabasePassword() {
        var prefs = getSharedPreferences("databasePassword", Activity.MODE_PRIVATE)
        databasePassword = prefs.getString("valDatabasePassword", "").toString()
    }
}