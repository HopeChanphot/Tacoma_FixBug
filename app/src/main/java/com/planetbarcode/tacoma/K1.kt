package com.planetbarcode.tacoma

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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.planetbarcode.tacoma.Model.K1DetailModel
import com.planetbarcode.tacoma.Model.K1TransactionModel
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class K1 : AppCompatActivity() {
    companion object{
        lateinit var textViewCurrentStep: TextView
        lateinit var textViewPartNo: TextView
        lateinit var textViewSerial: TextView
        lateinit var textViewAddress: TextView
        lateinit var textViewQty:TextView
        lateinit var textViewDate : TextView
        lateinit var textViewShift : TextView
        lateinit var editTextBarcode: EditText
        lateinit var imageViewEdit: View
        lateinit var imageViewPartNo: ImageView
        lateinit var imageViewHome: ImageView
        lateinit var linearLayoutEditText : LinearLayout
        lateinit var linearLayoutComplete: LinearLayout
        lateinit var cardViewBack : CardView
        lateinit var floatingActionButton: FloatingActionButton
        lateinit var dialog:AlertDialog
        lateinit var K1Array:ArrayList<K1DetailModel>
        lateinit var K1Transactions:ArrayList<K1TransactionModel>
        var scanProcess = 0
        var strPdsMode = ""
        var strOrderType = ""
        var strMode = ""
        var partNo = ""
        var recQty = 0
        var pStatus = ""
        var partNoCheck = true
        var ConnURL = ("jdbc:jtds:sqlserver://${Login.databaseServer}:1433/${Login.databaseName};encrypt=fasle;user=${Login.databaseUser};password=${Login.databasePassword};")

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_k1)

        K1Array = ArrayList<K1DetailModel>()
        floatingActionButton = findViewById(R.id.floatingActionButton)
        linearLayoutEditText = findViewById(R.id.linearLayout)
        linearLayoutComplete = findViewById(R.id.layout_complete)
        textViewCurrentStep = findViewById(R.id.txt_current_step)
        imageViewEdit = findViewById(R.id.img_edit)
        imageViewPartNo = findViewById(R.id.img_view_partNo)
        imageViewHome = findViewById(R.id.img_home)
        textViewPartNo = findViewById(R.id.txt_partno)
        textViewSerial = findViewById(R.id.txt_serial)
        textViewAddress = findViewById(R.id.txt_address)
        textViewQty = findViewById(R.id.txt_qty)
        textViewDate = findViewById(R.id.textViewDate)
        textViewShift = findViewById(R.id.textViewShift)
        editTextBarcode = findViewById(R.id.edt_barcode)
        cardViewBack = findViewById(R.id.cardView_back)
        K1Array = ArrayList<K1DetailModel>()
        K1Transactions = ArrayList<K1TransactionModel>()
        findWorkShiftTime()

        editTextBarcode.requestFocus()
        editTextBarcode.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextBarcode.text.toString() != "") {
                    if(partNoCheck){
                        scanProcess = 1
                    }
                    println(scanProcess)
                    matchDoc(editTextBarcode.text.toString())
                    editTextBarcode.nextFocusDownId = editTextBarcode.id
                } else {
                    Toast.makeText(this, "Please Enter Part No", Toast.LENGTH_SHORT).show()
                    editTextBarcode.nextFocusDownId = editTextBarcode.id
                }
            }
            false
        })

        imageViewEdit.setOnClickListener {
            if(textViewPartNo.text.toString() != ""){
                qtyDialog()
            }
        }

        floatingActionButton.setOnClickListener {
            if(textViewCurrentStep.text.toString() != ">> Part No."){
                partNoDialog()
            }
            else{
                textViewCurrentStep.text = ">> Part No."
                editTextBarcode.hint = "Scan Part No."
            }
        }

        imageViewPartNo.setOnClickListener {
            val intent = Intent(this, K1Detail::class.java)
            startActivity(intent)
        }

        imageViewHome.setOnClickListener {
            finish();
            super.onBackPressed();
        }

        cardViewBack.setOnClickListener {
            finish();
            super.onBackPressed();
        }
    }

    private fun defineBarcode(value: String):Int{
        var firstChar:String
        var lastChar:String
        var barcode2D: Array<String>? = null
        var delimiterFac1:String = "|"
        var delimiterFac2:String = ":"
        var delimiter:String = ""
        var barcodeMode:String = "1D"

        if(value != ""){
            firstChar = value.take(1)
            lastChar = value.takeLast(1)


            delimiter = if(Login.factory == "Factory1"){
                delimiterFac1
            } else{
                delimiterFac2
            }

            try{
                barcode2D = value.split(delimiter).toTypedArray()
                if(barcode2D.size>1){
                    barcodeMode = "2D"
                }
            }catch (e: Exception){
                barcodeMode = "1D"
            }

            if(barcodeMode == "2D"){
                if(Login.factory == "FACTORY1"){
                    textViewPartNo.text = ""
                    textViewSerial.text = ""
                    if(barcode2D!!.size == 18){
                        val pageKanban = barcode2D[14].split("/").toTypedArray()
                        val serialKanban = pageKanban[0].toString().padStart(4,'0')
                        textViewPartNo.text = "10${barcode2D[0].substring(2,7)}${barcode2D[0].substring(7,12)}00"
                        textViewSerial.text = barcode2D[11]+"-"+serialKanban
                    }
                    else{
                        if(barcode2D.size == 11){
                            var pageKanban = barcode2D[9].split("/").toTypedArray()
                            var serialKanban = pageKanban[0].toString().padStart(4,'0')
                            textViewPartNo.text = barcode2D[6]
                        }
                    }
                }
                else{
                    textViewPartNo.text = ""
                    textViewSerial.text = ""
                    if(barcode2D!!.size == 14){
                        var pageKanban = barcode2D[14].split("/").toTypedArray()
                        var serialKanban = pageKanban[0].toString().padStart(4,'0')
                        textViewPartNo.text = "10${barcode2D[0].substring(2,7)}${barcode2D[0].substring(7,12)}00"
                        textViewSerial.text = barcode2D[11]+"-"+serialKanban
                    }
                }
                return 6
            }
            return when(firstChar){
                "1" -> 1
                "+" -> 2
                "S" -> 4
                else -> 3
            }
        }
        else{
            return 0
        }
    }

    private fun matchDoc(value: String){
        var pdsNo:String

        var docType:Int = defineBarcode(value)
        when(scanProcess){

            1 -> {
                //PartNo
                linearLayoutComplete.visibility = GONE
                imageViewEdit.visibility = VISIBLE
                changeblue(this)
                editTextBarcode.isEnabled = true
                editTextBarcode.isFocusable = true
                editTextBarcode.isFocusableInTouchMode = true
                editTextBarcode.requestFocus()
                editTextBarcode.hint = "Scan Part No."
                textViewCurrentStep.text = ">> Part No."
                if (docType == 1) {
                    searchPartNo(value)
                }
                else if(docType == 6){
                    var strType = ""
                    strPdsMode = value.trim().split("|")[11].toString()
                    strOrderType = ""
                    strOrderType = value.trim().split("|")[0].substring(0,2).toString()

                    strOrderType = when(strOrderType){
                        "00" -> "MSP"
                        "10" -> "B190"
                        "20" -> "EGYPT"
                        "30" -> "ARM"
                        "TT" -> "TACOMA"
                        else -> "OTHER"
                    }

                    strType = if (strType == "Replace_Kanban"){
                        "PI"
                    } else{
                        ""
                    }

                    when(strType){
                        "PI" -> {
                            //Tacoma
                            strMode = "PI"
                            //lbstock.text = ""
                            searchPartNo(value)
                            if(textViewAddress.text.toString() != "ERROR"){
                                searchSerial(textViewSerial.text.toString())
                            }
                            var kanBanQty = 0
                            kanBanQty = getKanBanQty("${textViewPartNo.text.toString()}-${textViewSerial.text.toString()}")
                            if(kanBanQty > 0){
                                //SHOW ERROR and Go back to Part No
                                scanProcess = 1
                                textViewCurrentStep.text = ">> Part No. "
                                textViewSerial.text = ""
                                textViewPartNo.text = ""
                                textViewQty.text = ""
                                textViewAddress.text = ""
                                changeRed(this,"ใช้ Kanban ซ้ำ มีการรับเข้ามาแล้ว") //Kanban Qty $kanBanQty > 0
                                editTextBarcode.text.clear()
                                editTextBarcode.requestFocus()
                            }
                        }
                        else -> {
                            //Factory 2
                            //lbstock.text = ""
                            searchPartNo(value)
                            if(textViewAddress.text.toString() != "ERROR"){
                                searchSerial(textViewSerial.text.toString())
                            }
                            var kanBanQty = 0
                            kanBanQty = getKanBanQty("${textViewPartNo.text.toString()}-${textViewSerial.text.toString()}")
                            if(kanBanQty > 0){
                                //SHOW ERROR and Go back to Part No
                                scanProcess = 1
                                textViewCurrentStep.text = ">> Part No. "
                                textViewSerial.text = ""
                                textViewPartNo.text = ""
                                textViewQty.text = ""
                                textViewAddress.text = ""
                                changeRed(this,"ใช้ Kanban ซ้ำ มีการรับเข้ามาแล้ว") //Kanban Qty $kanBanQty > 0
                                editTextBarcode.text.clear()
                                editTextBarcode.requestFocus()
                            }

                        }
                    }
                }
                else{
                    //show error part number
                    editTextBarcode.text.clear()
                    editTextBarcode.requestFocus()
                    Toast.makeText(this,"WRONG PART NUMBER",Toast.LENGTH_SHORT).show()
                    changeRed(this,"ไม่ใช่ Part No กรุณาตรวจสอบ") // Wrong Part No
                }
            }

            2 -> {
                changeblue(this)
                editTextBarcode.hint = "Scan Serial"
                textViewCurrentStep.text = ">> Serial"
                if (docType == 2){
                    searchSerial(textViewSerial.text.toString())
                }
                else{
                    //show error serial
                    editTextBarcode.text.clear()
                    editTextBarcode.requestFocus()
                    changeRed(this,"ไม่ใช่ Serial กรุณาตรวจสอบเอกสาร") // Wrong Serial
                }
            }


            3 ->{
                changeblue(this)
                editTextBarcode.hint = "Scan Address"
                textViewCurrentStep.text = ">> Address"
                if(docType == 3){
                    searchAddress(value)
                }
                else{
                    //show error address
                    editTextBarcode.text.clear()
                    editTextBarcode.requestFocus()
                    changeRed(this,"ไม่ใช่เอกสาร Location กรุณาตรวจสอบเอกสา") //Wrong Address
                }
            }
        }
    }

    fun searchPartNo(value: String){
        var location_address = ""
        var delivery_lotsize = ""
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? =null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try{
            partNo = if(value.length >= 14){
                value.substring(2,7)+"-"+value.substring(7,12)+"-"+value.substring(12,14)
            } else{
                value
            }
        }catch(e:Exception){
            e.printStackTrace()
        }

        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.createStatement()

            result = statement.executeQuery("SELECT distinct tbl_Autoparts.part_number, tbl_Autoparts.lotsize as rec_lotsize," +
                    " tbl_Autoparts_Subitem.location_address, tbl_Autoparts_Subitem.lotsize AS delivery_lotsize, tbl_Autoparts.status " +
                    " FROM tbl_Autoparts INNER JOIN" +
                    " tbl_Autoparts_Subitem ON tbl_Autoparts.part_ID = tbl_Autoparts_Subitem.part_ID " +
                    "  WHERE tbl_Autoparts.part_number='$partNo'")
            textViewAddress.text = ""
            if(result != null){
                while(result.next()){
                    try{
                        when(strMode){
                            "PI" -> {
                                textViewQty.text = value.trim().split("|").toTypedArray()[13].toString()
                            }
                            else -> {
                                try{
                                    textViewQty.text = value.trim().split("|").toTypedArray()[13].toString()
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    textViewQty.text = "0"
                                }
                            }
                        }
                    }catch(e:Exception){
                        e.printStackTrace()
                        textViewQty.text = "0"
                    }
                    recQty = Integer.parseInt(textViewQty.text.toString())

                    try{
                        if(textViewAddress.text.isNotEmpty()){
                            textViewAddress.text = textViewAddress.text.toString()+","
                        }
                        try{
                            textViewAddress.text = textViewAddress.text.toString()+result.getString("location_address")+":"+result.getString("delivery_lotsize")
                            location_address = result.getString("location_address").trim()
                        }catch (e:Exception){
                            e.printStackTrace()
                            textViewAddress.text = textViewAddress.text.toString()+"N/A:"+result.getString("delivery_lotsize")
                            location_address = "N/A"
                        }
                        delivery_lotsize = result.getString("delivery_lotsize")
                        K1Array.add(K1DetailModel(location_address,delivery_lotsize))
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try {
                        pStatus = result.getString("status")
                    }catch (e:Exception){
                        e.printStackTrace()
                        pStatus = ""
                    }
                }
            }
            textViewPartNo.text = partNo
            if(textViewAddress.text != ""){
                if(textViewSerial.text == ""){
                    textViewCurrentStep.text = ">> Serial"
                    editTextBarcode.hint = "Scan Serial"
                }
                editTextBarcode.text.clear()
                editTextBarcode.requestFocus()
                scanProcess = 2
                if(pStatus == "Y"){
                    imageViewEdit.visibility = VISIBLE
                }else{
                    imageViewEdit.visibility = INVISIBLE
                }
                partNoCheck = false
            }
            else{
                editTextBarcode.text.clear()
                changeRed(this,"ไม่พบข้อมูล PartNo : $partNo") // Part No not found
                editTextBarcode.requestFocus()
                scanProcess = 1
            }
            conn.close()
        }catch (e: Exception){
            println(e)
            textViewCurrentStep.text = ">> Part No."
            editTextBarcode.text.clear()
            changeRed(this,"ไม่สามารถติดต่อฐานข้อมูลได้") // Connection Error
            editTextBarcode.requestFocus()
            scanProcess = 1
        }
    }

    fun searchSerial(value: String){
        var serial = ""
        var firstChar = ""

        firstChar = value.substring(0,1)
        if(firstChar == "+"){
            serial = value.substring(1,5)
        }
        else{
            serial = value
        }
        textViewSerial.text = serial
        textViewCurrentStep.text = ">> Address"
        editTextBarcode.text.clear()
        editTextBarcode.hint = "Scan Address"
        scanProcess = 3
        partNoCheck = false
    }

    fun searchAddress(value: String){
        var lotSize = 0
        var packLot = 0
        var currentPack = 0
        var currentSupLot = 0
        var foundFlag = false

        for(i in 0 until K1Array.size){
            if(K1Array[i].address.uppercase() == value.uppercase()){
                foundFlag = true
                lotSize = Integer.parseInt(K1Array[i].lotSize)
                packLot = lotSize
                break
            }
        }
        if(!foundFlag){
            textViewCurrentStep.text = ">> Scan Address"
            editTextBarcode.text.clear()
            editTextBarcode.requestFocus()
            scanProcess = 3
            changeRed(this,"จัดเก็บสินค้าไม่ตรงพื้นที่ , NG : $value") // Store Address not found
        }
        else{
            SyncDB(this,layoutInflater).execute()
        }
    }

    private class SyncDB(val context: Context,val layoutInflater: LayoutInflater) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var saveFlag = false
        var message = ""
        var datetime = ""
        var transactionDateTime = ""

        override fun doInBackground(vararg params: String?): String {
            if(!saveFlag){
                var findId = ""
                findId = findDocNo()
                saveFlag = saveData(findId)
                if(!saveFlag){
                    //Show Dialog for new and retry
                }
                else{
                    var qty = 0
                    qty = try {
                        Integer.parseInt(textViewQty.text.toString())
                    }catch (e:Exception){
                        0
                    }
                    updateKanbanQty(textViewPartNo.text.toString()+"-"+ textViewSerial.text.toString(),qty)
                }
            }
            transactionDateTime = datetime.substring(4,6)+"/"+datetime.substring(2,4)+"/"+datetime.substring(0,2)+" "+datetime.substring(6,8)+":"+datetime.substring(8,10)
            println(datetime)

            return "OK"
        }

        override fun onPreExecute() {
            pgd = ProgressDialog(context)
            pgd.setMessage("Please Wait")
            pgd.setTitle("Saving Data")
            pgd.show()
            pgd.setCancelable(false)

            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            if(saveFlag){
                editTextBarcode.text.clear()
                editTextBarcode.requestFocus()
                editTextBarcode.hint = "Scan Part No."
                textViewCurrentStep.text = ">> Part No."
                K1().changeblue(context)
                linearLayoutComplete.visibility = VISIBLE
                imageViewEdit.visibility = INVISIBLE
                partNoCheck = true
                scanProcess = 1
                if(K1Transactions.size == 0){
                    K1Transactions.add(K1TransactionModel(textViewPartNo.text.toString(), transactionDateTime, Integer.parseInt(textViewQty.text.toString())))
                }
                else if(K1Transactions[0].partNo == textViewPartNo.text.toString()){
                    K1Transactions.add(K1TransactionModel(textViewPartNo.text.toString(), transactionDateTime, Integer.parseInt(textViewQty.text.toString())))
                }
                else{
                    K1Transactions.clear()
                    K1Transactions.add(K1TransactionModel(textViewPartNo.text.toString(), transactionDateTime, Integer.parseInt(textViewQty.text.toString())))
                }
                pgd.dismiss()
            }
            else{
                editTextBarcode.text.clear()
                editTextBarcode.requestFocus()
                editTextBarcode.hint = message
                K1().changeRed(context,"บันทึกข้อมูลไม่ได้") // Cannot Save Data
                textViewCurrentStep.text = ">> Scan Address"
                alertDialog("บันทึกข้อมูลไม่ได้","RETRY") // Cannot Save Data
                partNoCheck = false
                scanProcess = 3
                pgd.dismiss()
            }

        }

        fun findDocNo():String{
            var count = 0
            var docNo = ""
            var docType = "IN"
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            datetime = getTodayDateTime().substring(2, 14)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                println("SELECT max(doc_no) FROM tbl_StockTransactionHeader WHERE doc_no like '$docType$datetime%'" )
                result  = statement.executeQuery("SELECT max(doc_no) FROM tbl_StockTransactionHeader WHERE doc_no like '$docType$datetime%'")
                while(result.next()){
                    docNo = result.getString(1)
                }
                conn.close()
            }catch (e: Exception){
                println(e)
                docNo = ""
            }
            if(docNo == "" || docNo == null ){
                docNo = docType+datetime+"000001"
            }
            else{
                count = Integer.parseInt(docNo.substring(14,20))+1
                docNo = docType+datetime+count.toString().padStart(6, '0')
            }
            return docNo
        }


        fun saveData(id:String):Boolean{
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            var current_qty:Int
            var new_qty:Int
            var qty:Int
            var qty_sign:Int
            var doc_date = getToday()

            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                statement.executeUpdate(
                    "INSERT INTO tbl_StockTransactionHeader " +
                            " (doc_no, doc_type, doc_ref, doc_date, remark, doc_datetime, doc_user) " +
                            " VALUES('$id', 'IN', " +
                            " 'PDA_K1', '$doc_date', '', " +
                            " getdate(), '${Login.mUser}')"
                )

                current_qty = getQty(partNo, "K1")
                qty = recQty
                new_qty = current_qty + qty
                qty_sign = 1
                qty *= qty_sign

                try {

                    val statement1 = conn.createStatement()
                    statement1.executeUpdate(
                        "INSERT INTO tbl_StockTransaction (doc_no, item_no, part_number, kanban_serial, photo_spec, location_id, qty_sign, current_qty, in_qty, out_qty, new_qty, doc_datetime, doc_user)" +
                                " VALUES('$id', '1', '$partNo', '${textViewSerial.text.toString()}', '${partNo}-000', 'K1', " +
                                "'$qty_sign', '$current_qty', '$qty', '0', '$new_qty', getdate(), '${Login.mUser}')"
                    )


                    var updateFlag = false
                    if (!updateFlag) {
                        updateFlag = updateStock(partNo, "K1", qty)
                        if (!updateFlag) {
                            //Show Dialog for new or retry
                            message = "Cannot Update Stock"
                            return false
                        }
                        else{
                            return true
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                    message = "Cannot Connect to Database"
                    return false
                }
                conn.close()
                return true
            }catch (e: Exception){
                e.printStackTrace()
                message = "Cannot Connect to Database"
                return false
            }
        }

        fun updateKanbanQty(kanban_no: String, kanban_qty: Int){
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try{
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.prepareCall("{call [dbo].[UpdateKanbanQty](?,?,?)}")
                statement.setString(1, kanban_no)
                statement.setInt(2, kanban_qty)
                statement.setString(3, Login.mUser)
                statement.execute()
                conn.close()
            }catch (e: Exception){
                message = "Cannot connect to database"
                e.printStackTrace()
            }
        }

        fun updateStock(partNo: String, location: String, qty: Int):Boolean{
            var today = "Error"
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()

                if(isStock(partNo, location)){
                    statement.executeUpdate("UPDATE tbl_StockByLocation SET qty=qty+($qty) WHERE part_number= '$partNo' AND location_id= '$location'")
                } else{
                    statement.executeUpdate("INSERT INTO tbl_StockByLocation (part_number, location_id, qty) VALUES('$partNo', '$location', '$qty')")
                }
                conn.close()
                return true
            }catch (e: Exception){
                e.printStackTrace()
                message = "Cannot connect to Database"
                return false
            }
        }

        fun isStock(partNo: String, location: String):Boolean{
            var count:Int = 0
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()

                result = statement.executeQuery("SELECT COUNT(*) FROM tbl_StockByLocation WHERE part_number='$partNo' AND location_id='$location'")
                if(result != null){
                    while(result.next()){
                        count = result.getInt(1)
                    }
                }
                conn.close()
            }catch (e: Exception){
                message = "Cannot connect to database"
                e.printStackTrace()
            }
            return count > 0
        }

        fun getQty(partNo: String, location: String):Int{
            var qty1 = 0
            var qty2 = 0
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                result  = statement.executeQuery("SELECT qty FROM tbl_StockByLocation WHERE part_number= '$partNo' AND location_id='$location'")
                while(result.next()){
                    qty2 = result.getInt(1)
                }
                if(qty2 == null){
                    qty2 = 0
                }
                conn.close()
            }catch (e: Exception){
                message = "Cannot connect to database"
                e.printStackTrace()
            }
            return qty1+qty2
        }
        fun getTodayDateTime():String{
            var today = "Error"
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                val sql = "SELECT CONVERT(char(20), Format(getdate(),'yyyyMMddHHmmss'), 120) AS today"
                result = statement.executeQuery(sql)
                while (result.next()){
                    today = result.getString(1)
                }
                conn.close()
            }catch (e: Exception){
                e.printStackTrace()
            }
            return today
        }

        fun getToday():String{
            var today = "Error"
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                result  = statement.executeQuery("SELECT CONVERT(char(8), getdate(), 112) AS today")
                while(result.next()){
                    today = result.getString(1)
                }
                conn.close()
            }catch (e: Exception){
                e.printStackTrace()
            }
            return today
        }

        fun alertDialog(text: String, button: String) {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.alert_dialog, null)
            builder.setView(view)
            dialog = builder.create()
            dialog.show()
            dialog.setCancelable(false)
            var textViewText = view.findViewById<TextView>(R.id.txt_text)
            var buttonYes = view.findViewById<Button>(R.id.btn_ok)

            textViewText.text = text
            buttonYes.text = button

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

            buttonYes.setOnClickListener {
                if (button == "RETRY") {
                    pgd.dismiss()
                    SyncDB(context, layoutInflater).execute()
                    dialog.dismiss()
                } else {
                    pgd.dismiss()
                    dialog.dismiss()
                }
            }
        }

    }

    fun getKanBanQty(serail:String):Int{
        var qty = 0
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? =null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.createStatement()

            result = statement.executeQuery(
                "SELECT isnull(kanban_qty, 0) as kanban_qty "+
                        " FROM tbl_Kanban_Qty  WHERE kanban_no='$serail'")
            while (result.next()){
                qty = result.getInt(1)
            }
        }catch (e:Exception){
            e.printStackTrace()
            qty = 0
        }
        return qty
    }

    private fun changeRed(context: Context,text:String){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border_red)
        editTextBarcode.setHintTextColor(Color.parseColor("#E71717"))
        editTextBarcode.hint = text
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

    private fun changeblue(context: Context){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border)
        editTextBarcode.setHintTextColor(Color.parseColor("#87898E"))
    }

    fun qtyDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.edit_qty, null)
        builder.setView(view)
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        var buttonOk = view.findViewById<Button>(R.id.btn_ok)
        var buttonCancel = view.findViewById<Button>(R.id.btn_cancel)
        var editTextQty = view.findViewById<EditText>(R.id.edt_qty)

        buttonOk.setOnClickListener {
            if(editTextQty.text.toString() != ""){
                textViewQty.text = editTextQty.text.toString()
                dialog.dismiss()
            }
        }

        buttonCancel.setOnClickListener{
            dialog.dismiss()
        }
    }

    fun partNoDialog(){
        val builder= AlertDialog.Builder(this)
        val inflater=layoutInflater
        val view=inflater.inflate(R.layout.partnodialog, null)
        builder.setView(view)
        dialog =builder.create()
        dialog.show()
        dialog.setCancelable(false)
        var buttonOK = view.findViewById<Button>(R.id.btn_ok)
        var buttonCancel = view.findViewById<Button>(R.id.btn_cancel)

        buttonOK.setOnClickListener {
            editTextBarcode.text.clear()
            editTextBarcode.requestFocus()
            editTextBarcode.hint = "Scan Part No."
            textViewCurrentStep.text = ">> Part No."
            textViewQty.text = ""
            textViewAddress.text = ""
            textViewPartNo.text = ""
            textViewSerial.text = ""
            changeblue(this)
            partNoCheck = true
            scanProcess = 1
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
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
            conn = DriverManager.getConnection(ConnURL)
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

    fun findWorkShiftTime(){
        val date = getTodayDateTime().substring(0,8)
        val time = getTodayDateTime().substring(8,14)
        if(Integer.parseInt(time) in 73000..192959){
            //day
            textViewDate.text = date.substring(6,8)+"/"+date.substring(4,6)
            textViewShift.text = "Day"
        }
        else{
            textViewDate.text = date.substring(6,8)+"/"+date.substring(4,6)
            textViewShift.text = "Night"
            //night
        }
    }

}