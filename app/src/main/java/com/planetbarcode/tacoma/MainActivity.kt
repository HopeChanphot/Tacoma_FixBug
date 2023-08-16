package com.planetbarcode.tacoma

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.media.MediaPlayer
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.example.tscdll.TscWifiActivity
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var linearLayoutK1: LinearLayout
        lateinit var linearLayoutK2: LinearLayout
        lateinit var linearLayoutK3: LinearLayout
        lateinit var linearLayoutTestPrint: LinearLayout
        lateinit var linearLayoutEditText : LinearLayout
        lateinit var textViewUser: TextView
        lateinit var textViewGreeting: TextView
        lateinit var textViewCurrentPrinterIP: TextView
        lateinit var editTextIP : EditText
        lateinit var dialog: AlertDialog
        val BTSDK = TscWifiActivity()
        var printerIp = ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linearLayoutK1 = findViewById(R.id.layoutK1)
        linearLayoutK2 = findViewById(R.id.layoutK2)
        linearLayoutK3 = findViewById(R.id.layoutK3)
        linearLayoutTestPrint = findViewById(R.id.layout_testPrint)
        textViewUser = findViewById(R.id.textViewUser)
        textViewGreeting = findViewById(R.id.textViewGreeting)

        setGreeting()
        loadIp()
        textViewUser.text = "Hi, Mr.${Login.mEmployee}"

        linearLayoutK1.setOnClickListener {
            val intent = Intent(this,K1::class.java)
            startActivity(intent)
        }

        linearLayoutK2.setOnClickListener {
            val intent = Intent(this,K2::class.java)
            startActivity(intent)
        }

        linearLayoutK3.setOnClickListener {
            val intent = Intent(this,K3::class.java)
            startActivity(intent)
        }

        linearLayoutTestPrint.setOnClickListener {
            testPrintDialog()
        }
    }

    fun getTodayDateTime():String {
        var today = "Error"
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(K1.ConnURL)
            val statement = conn.createStatement()
            val sql = "SELECT CONVERT(char(20), Format(getdate(),'yyyyMMddHHmmss'), 120) AS today"
            result = statement.executeQuery(sql)
            while (result.next()) {
                today = result.getString(1)
            }
            conn.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return today
    }

    fun setGreeting(){
        val time = getTodayDateTime().substring(8,14)
        if(Integer.parseInt(time) in 55959..115959){
            //morning
            textViewGreeting.text = "Good Morning"
        }
        else if(Integer.parseInt(time) in 120000..155959){
            //afternoon
            textViewGreeting.text = "Good Afternoon"
        }
        else if (Integer.parseInt(time) in 160000..195959){
            //evening
            textViewGreeting.text = "Good Evening"
        }
        else{
            //night
            textViewGreeting.text = "Good Night"
        }
    }

    fun testPrintDialog(){
        val builder= AlertDialog.Builder(this)
        val inflater=this.layoutInflater
        val view=inflater.inflate(R.layout.test_print, null)
        builder.setView(view)
        dialog =builder.create()
        dialog.show()
        val buttonSave = view.findViewById<Button>(R.id.btn_save)
        val buttonPrint = view.findViewById<Button>(R.id.btn_print)
        linearLayoutEditText = view.findViewById(R.id.linearLayout)
        textViewCurrentPrinterIP = view.findViewById(R.id.textView_CurrentIP)
        editTextIP = view.findViewById(R.id.edt_ip)
        textViewCurrentPrinterIP.text = "Current Printer IP : $printerIp"

        editTextIP.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                   editTextIP.setText(editTextIP.text.toString().replace("@",""))
            }
            false
        })

        buttonSave.setOnClickListener {
            editTextIP.setText(editTextIP.text.toString().replace("@",""))
            if(editTextIP.text.toString() != "" && printerIP(editTextIP.text.toString())){
                setIp(editTextIP.text.toString())
                loadIp()
                textViewCurrentPrinterIP.text = "Current Printer IP : $printerIp"
                changeBlue(this)
            }
            else{
                editTextIP.text.clear()
                changeRed(this,"IP เครื่องพิมพ์ไม่ถูกต้อง") // invalid ip
            }

        }

        buttonPrint.setOnClickListener {
            editTextIP.setText(editTextIP.text.toString().replace("@",""))
            if(editTextIP.text.toString() != ""){
                if(printerIP(editTextIP.text.toString())){
                    setIp(editTextIP.text.toString())
                    loadIp()
                    textViewCurrentPrinterIP.text = "Current Printer IP : $printerIp"
                    AsyncPrint(this).execute()
                }
                else{
                    editTextIP.text.clear()
                    changeRed(this,"IP เครื่องพิมพ์ไม่ถูกต้อง") // invalid ip
                }
            }
            else{
                if(printerIP(printerIp)){
                    textViewCurrentPrinterIP.text = "Current Printer IP : $printerIp"
                    AsyncPrint(this).execute()
                }
                else{
                    changeRed(this,"IP เครื่องพิมพ์ไม่ถูกต้อง") // invalid ip
                }
            }

        }

    }

    private fun printerIP(ip:String):Boolean{
        return Patterns.IP_ADDRESS.matcher(ip).matches()
    }


    private fun changeRed(context: Context, text:String){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border_red)
        editTextIP.setHintTextColor(Color.parseColor("#E71717"))
        editTextIP.hint = text
        val afd: AssetFileDescriptor = context.assets.openFd("buzz.wav")
        val player = MediaPlayer()
        player.setDataSource(
            afd.getFileDescriptor(),
            afd.getStartOffset(),
            afd.getLength()
        )
        player.prepare()
        player.start()
        val handler = Handler()
        handler.postDelayed({ player.stop() }, 1 * 1000.toLong())
    }

    private fun changeBlue(context: Context){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border)
        editTextIP.setHintTextColor(Color.parseColor("#87898E"))
    }

    private fun setIp(v: String){
        var editor = getSharedPreferences("printerIp", Activity.MODE_PRIVATE).edit()
        editor.putString("valPrinterIp", v)
        editor.apply()
    }

    private fun loadIp() {
        var prefs = getSharedPreferences("printerIp", Activity.MODE_PRIVATE)
        printerIp = prefs.getString("valPrinterIp", "").toString()
    }

    private class AsyncPrint(val context: Context) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var connected = false
        var printFlag = false

        override fun doInBackground(vararg params: String?): String {

            connected = isHostReachable(printerIp, 9100, 1000)
            if(connected){
                printTest()
            }

            return "BackGroundThread"
        }

        override fun onPreExecute() {
            pgd = ProgressDialog(context)
            pgd.setMessage("Testing")
            pgd.setTitle("Test Print")
            pgd.show()
            pgd.setCancelable(false)

            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            if(connected && printFlag){
                MainActivity().changeBlue(context)
            }
            else{
                editTextIP.text.clear()
                MainActivity().changeRed(context,"Oops! ไม่สามารถเชื่อมต่อกับเครื่องพิมพ์") // cannot connect to printer
            }
            pgd.dismiss()

        }

        fun isHostReachable(serverAddress: String?, serverTcpPort: Int, timeoutMS: Int): Boolean {
            var socket: Socket?
            try {
                socket = Socket()
                val socketAddress: SocketAddress = InetSocketAddress(serverAddress, serverTcpPort)
                socket.connect(socketAddress, timeoutMS)
                return if (socket.isConnected) {
                    socket.close()
                    true
                } else{
                    false
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            } finally {
                socket = null
            }
        }

        fun printTest():Boolean{
            var v1 = "TH-HMMT5-1805-001"
            var v2 = "HM327"
            var v3 = " - "
            var v4 = "12/11/21"
            var v5 = "E1LSYW"
            var v6 = "-"
            var v7 = "20180614T502"
            var v8 = "43211-0KK10-00"
            var v9 = "2"
            var v10 = "CARRIER ASSY DIFFERENTIAL"
            var v11 = "01/11"
            var qr = "T1B HMT1BB6C275742150-0K650-00-BT-081-001"

            try{
                BTSDK.openport(printerIp, 9100)
                BTSDK.clearbuffer()
                BTSDK.sendcommand("DIRECTION 0,0\n")
                BTSDK.sendcommand("REFERENCE 0,0\n")
                BTSDK.sendcommand("OFFSET 0 mm\n")
                BTSDK.sendcommand("SET REWIND OFF\n")
                BTSDK.sendcommand("SET PEEL OFF\n")
                BTSDK.sendcommand("SET CUTTER OFF\n")
                BTSDK.sendcommand("SET PARTIAL OFF\n")
                BTSDK.sendcommand("SET TEAR ON\n")
                BTSDK.sendcommand("CLS\n")
                BTSDK.sendcommand("BAR 167,0, 3, 399\n")
                BTSDK.sendcommand("BAR 8,316, 613, 3\n")
                BTSDK.sendcommand("BAR 8,132, 613, 3\n")
                BTSDK.sendcommand("BAR 168,35, 453, 3\n")
                BTSDK.sendcommand("BAR 366,132, 3, 184\n")
                BTSDK.sendcommand("BAR 367,222, 254, 3\n")
                BTSDK.sendcommand("BAR 8,53, 159, 3\n")
                BTSDK.sendcommand("BAR 91,0, 3, 54\n")
                BTSDK.sendcommand("BAR 8,223, 160, 3\n")
                BTSDK.sendcommand("CODEPAGE 1254\n")
                BTSDK.sendcommand("TEXT 517,367,\"ROMAN.TTF\",180,1,14,\"$v1\"\n")
                BTSDK.sendcommand("TEXT 100,367,\"ROMAN.TTF\",180,1,16,2,\"$v2\"\n")
                BTSDK.sendcommand("TEXT 100,275,\"ROMAN.TTF\",180,1,16,2,\"$v6\"\n")
                BTSDK.sendcommand("TEXT 100,187,\"ROMAN.TTF\",180,1,16,2,\"$v5\"\n")
                BTSDK.sendcommand("TEXT 529,279,\"ROMAN.TTF\",180,1,14,\"$v3\"\n")
                BTSDK.sendcommand("TEXT 554,184,\"ROMAN.TTF\",180,1,14,\"$v4\"\n")
                BTSDK.sendcommand("TEXT 556,103,\"ROMAN.TTF\",180,1,16,\"$v8\"\n")
                BTSDK.sendcommand("TEXT 365,127,\"ROMAN.TTF\",180,1,8,\"$v7\"\n")
                BTSDK.sendcommand("QRCODE 341,298,L,6,A,180,M2,S7,\"$qr\"\n")
                BTSDK.sendcommand("TEXT 100,121,\"ROMAN.TTF\",180,1,24,2,\"$v9\"\n")
                BTSDK.sendcommand("TEXT 588,53,\"ROMAN.TTF\",180,1,6,\"$v10\"\n")
                BTSDK.sendcommand("TEXT 212,25,\"ROMAN.TTF\",180,1,6,\"$v11\"\n")
                BTSDK.sendcommand("TEXT 588,26,\"ROMAN.TTF\",180,1,6,\"Hino Motors Manufacturing (Thailand) Ltd.\"\n")
                BTSDK.sendcommand("TEXT 88,51,\"ROMAN.TTF\",180,1,6,\"Packing by\"\n")
                BTSDK.sendcommand("TEXT 163,51,\"ROMAN.TTF\",180,1,6,\"Inspector\"\n")
                BTSDK.sendcommand("TEXT 161,131,\"ROMAN.TTF\",180,1,6,\"Q'ty\"\n")
                BTSDK.sendcommand("TEXT 160,307,\"ROMAN.TTF\",180,1,6,\"D/T No.\"\n")
                BTSDK.sendcommand("TEXT 161,218,\"ROMAN.TTF\",180,1,6,\"Dock Code\"\n")
                BTSDK.sendcommand("TEXT 162,393,\"ROMAN.TTF\",180,1,6,\"Part Code\"\n")
                BTSDK.sendcommand("TEXT 584,389,\"ROMAN.TTF\",180,1,6,\"Order No.\"\n")
                BTSDK.sendcommand("TEXT 583,306,\"ROMAN.TTF\",180,1,6,\"Deliver Time\"\n")
                BTSDK.sendcommand("TEXT 584,215,\"ROMAN.TTF\",180,1,6,\"Deliver Date\"\n")
                BTSDK.sendcommand("TEXT 583,124,\"ROMAN.TTF\",180,1,6,\"Part No.\"\n")
                BTSDK.sendcommand("PRINT 1,1\n")
                BTSDK.closeport()

                printFlag = true
                return true
            }catch (e: Exception){
                e.printStackTrace()
                printFlag = false
                return false
            }

        }
    }

}