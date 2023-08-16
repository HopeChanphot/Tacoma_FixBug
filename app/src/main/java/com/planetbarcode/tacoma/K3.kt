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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.planetbarcode.tacoma.Model.K2GroupPartModel
import com.planetbarcode.tacoma.Model.K3PdsDetailModel
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Locale

class K3 : AppCompatActivity() {

    companion object{
        lateinit var linearLayout: LinearLayout
        lateinit var linearLayoutKanban: LinearLayout
        lateinit var linearLayoutEditText: LinearLayout
        lateinit var view: View
        lateinit var editTextPDS: EditText
        lateinit var editTextBarcode: EditText
        lateinit var textViewEKB: TextView
        lateinit var textViewECount: TextView
        lateinit var textViewPDS: TextView
        lateinit var textViewScanned: TextView
        lateinit var textViewCurrentStep: TextView
        lateinit var textViewLotQty : TextView
        lateinit var textViewEKBQty: TextView
        lateinit var imageViewPDS: ImageView
        lateinit var imageViewHome : ImageView
        lateinit var textViewText: TextView
        lateinit var cardViewBack : CardView
        lateinit var floatingActionButton: FloatingActionButton
        lateinit var dialog: AlertDialog
        lateinit var buttonOK: Button
        lateinit var buttonCancel: Button
        lateinit var buttonYes: Button
        lateinit var K3PDSArray:ArrayList<K3PdsDetailModel>
        lateinit var k2GroupPartArray:ArrayList<K2GroupPartModel>
        var ConnURL = ("jdbc:jtds:sqlserver://${Login.databaseServer}:1433/${Login.databaseName};encrypt=fasle;user=${Login.databaseUser};password=${Login.databasePassword};")
        var partNo = ""
        var countLotSize = 0
        var totalLotSize = ""
        var workingFactory = "FACTORY1"
        var scanProcess = 0
        var currentIndex:Int = -1
        var photoQty = 0
        var totalPackQty = 0
        var totalK3Qty = 0
        var checkSave = false
        var STORAGE_PERMISSION_CODE = 1
        var saveFlag = false
        var pds = true
        var DOCNO_IN = ""
        var DOCNO_OUT = ""
        var DocItemNo = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_k3)

        pds = true

        linearLayout = findViewById(R.id.linearLayout6)
        linearLayoutKanban = findViewById(R.id.linearLayout_kanban)
        linearLayoutEditText = findViewById(R.id.linearLayout)
        view = findViewById(R.id.view1)
        editTextBarcode = findViewById(R.id.edt_barcode)
        textViewPDS = findViewById(R.id.txt_pds)
        textViewScanned = findViewById(R.id.txt_last_scanned)
        textViewEKB = findViewById(R.id.txt_ekanabn)
        textViewECount = findViewById(R.id.txt_eCount)
        textViewCurrentStep = findViewById(R.id.txt_current_step)
        textViewLotQty = findViewById(R.id.txt_lot_qty)
        textViewEKBQty = findViewById(R.id.txt_ekb_qty)
        imageViewPDS = findViewById(R.id.img_view_pds)
        imageViewHome = findViewById(R.id.img_home)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        cardViewBack = findViewById(R.id.cardView_back)
        K3PDSArray = ArrayList<K3PdsDetailModel>()
        k2GroupPartArray = ArrayList<K2GroupPartModel>()
        editTextBarcode.requestFocus()
        editTextBarcode.nextFocusDownId = editTextBarcode.id

        editTextBarcode.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                if (pds) {
                    if (editTextBarcode.text.toString() != "") {
                        textViewScanned.text = editTextBarcode.text.toString()
                        AsyncLoadPDS(this, editTextBarcode.text.toString(), layoutInflater).execute()
                        editTextBarcode.text.clear()
                        editTextBarcode.nextFocusDownId = editTextBarcode.id
                    } else {
                        Toast.makeText(this, "Please Enter PDS Barcode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    textViewScanned.text = editTextBarcode.text.toString()
                    matchDoc(editTextBarcode.text.toString())
                    editTextBarcode.text.clear()
                    editTextBarcode.nextFocusDownId = editTextBarcode.id
                }
            }
            false
        })

        imageViewPDS.setOnClickListener {
            val intent = Intent(this, K3PdsDetail::class.java)
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

        floatingActionButton.setOnClickListener {
            if(textViewCurrentStep.text.toString() != "PDS Document"){
                pdsDialog()
            }
            else{
                textViewCurrentStep.text = "PDS Document"
                editTextBarcode.hint = "SCAN PDS No."
            }
        }
    }

    fun visible(){
        linearLayout.visibility = VISIBLE
        linearLayoutKanban.visibility = GONE
        view.visibility = GONE
    }

    fun gone(){
        linearLayout.visibility = GONE
        linearLayoutKanban.visibility = VISIBLE
        view.visibility = VISIBLE
    }

    fun pdsDialog(){
        val builder= AlertDialog.Builder(this)
        val inflater=layoutInflater
        val view=inflater.inflate(R.layout.pdsdialog, null)
        builder.setView(view)
        dialog =builder.create()
        dialog.show()
        dialog.setCancelable(false)
        buttonOK = view.findViewById(R.id.btn_ok)
        buttonCancel = view.findViewById(R.id.btn_cancel)

        buttonOK.setOnClickListener {
            textViewCurrentStep.text = ">> PDS Document"
            editTextBarcode.hint = "SCAN PDS No."
            textViewPDS.text = ""
            textViewEKB.text = ""
            textViewLotQty.text = ""
            textViewEKBQty.text = ""
            textViewScanned.text = ""
            editTextBarcode.requestFocus()
            textViewECount.text = "E : "
            changeBlue(this)
            pds = true
            K3PDSArray.clear()
            scanProcess = 0
            gone()
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            pds = false
            dialog.dismiss()
        }
    }

    fun matchDoc(value: String){
        var docType:Int
        var pdsNo:String

        docType = defineBarcode(value)

        if(docType == 8){
            println("IP")
        }

        DOCNO_OUT = ""
        when(scanProcess){
            0 -> {
                DOCNO_IN = ""
                DocItemNo = 0
            }

            1 -> {
                //EKanBan
                editTextBarcode.isEnabled = true
                editTextBarcode.isFocusable = true
                editTextBarcode.isFocusableInTouchMode = true
                editTextBarcode.requestFocus()
                if (docType == 1) {
                    changeBlue(this)
                    searchEkanban(value)
                } else {
                    textViewEKB.text = ""
                    textViewCurrentStep.text =">> E - Kanban"
                    scanProcess = 1
                    textViewECount.text = "E : "
                    changeRed(this,"E - Kanban ไม่พบ") // not found
                    Toast.makeText(this, "E - KanBan Not Found", Toast.LENGTH_SHORT).show()
                }
            }

            2 -> {
                editTextBarcode.isEnabled = true
                editTextBarcode.isFocusable = true
                editTextBarcode.isFocusableInTouchMode = true
                if (docType == 2){
                    changeBlue(this)
                    searchPartTag(value)
                }
                else{
                    changeRed(this,"PartTag ไม่พบ") // not found
                    Toast.makeText(this, "PartTag Not Found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun defineBarcode(value: String):Int{
        var firstChar:String
        var lastChar:String
        var barcode2D:Array<String>
        var delimiterFac1:String = "|"
        var delimiterFac2:String = ";"
        var delimiter:String = ""
        var barcodeMode:String = "1D"

        if(value != ""){
            firstChar = value.take(1)
            lastChar = value.takeLast(1)

            when {
                lastChar == "P" -> {
                    return 1
                }
                lastChar == "/" -> {
                    return 6
                }
                firstChar == "1" && value.length == 12 ->{
                    return 1
                }
            }

            when(firstChar){
                "T" -> return 2
                "S" -> return 3
                "1" -> return 4
                else->{
                    var eKB = value.split("-")
                    if(eKB.size == 3){
                        return 1
                    }
                    else if(value.length == 12){
                        return 1
                    }
                }
            }
        }
        return 0
    }

    fun searchEkanban(scannedKanban: String){
        textViewCurrentStep.text =">> E - Kanban"
        editTextBarcode.hint = "SCAN E-KANBAN"
        var dbEKB = ""
        var kanbanComplete = false
        var scannedEKB = scannedKanban.replace("-", "")
        if(scannedEKB.takeLast(1) == "P"){
            scannedEKB=scannedEKB.dropLast(1)
        }
        for(i in 0 until K3PDSArray.size){
            dbEKB = K3PDSArray[i].partNo.replace("-", "")
            if(dbEKB == scannedEKB){
                if(K3PDSArray[i].k3Scan == K3PDSArray[i].packQty){
                    kanbanComplete = true
                    currentIndex = -1
                    scanProcess = 1
                    textViewECount.text = "E : "
                }
                else{
                    kanbanComplete = false
                    currentIndex = i
                    textViewEKB.text = textViewScanned.text.toString()
                    scanProcess = 2
                    textViewCurrentStep.text =">> PART TAG"
                    editTextBarcode.hint = "SCAN Part Tag"
                    textViewECount.text = "E : ${K3PDSArray[i].k3Scan}/${K3PDSArray[i].packQty}"
                    break
                }
            }
        }

        if(kanbanComplete){
            changeRed(this, "E-Kanban เสร็จสิ้น") // E-Kanban Completed
            Toast.makeText(this, "Ekanban Completed", Toast.LENGTH_SHORT).show()
        }
        if(dbEKB != scannedEKB){
            textViewEKB.text = ""
            textViewCurrentStep.text =">> E - Kanban"
            scanProcess = 1
            textViewECount.text = "E :"
            editTextBarcode.requestFocus()
            editTextBarcode.text.clear()
            changeRed(this,"E - Kanban ไม่พบ") // not found
        }
    }

    fun searchPartTag(value:String){
        value.lowercase(Locale.getDefault())
        var tagPDS = value.substring(1,16)
        var partTag = value.substring(16,30)

        if(textViewPDS.text.toString() != tagPDS){
            editTextBarcode.requestFocus()
            editTextBarcode.text.clear()
            changeRed(this,"เอกสาร Part Tag ผิด PDS") // Wrong part tag document pds
        }
        else{
            if(K3PDSArray[currentIndex].partNo != partTag){
                editTextBarcode.requestFocus()
                editTextBarcode.text.clear()
                changeRed(this,"Part Tag ไม่พบข้อมูล") // not found

            }
            else{
                if(K3PDSArray[currentIndex].lineNo.toString() != ""){
                    var i = K3PDSArray[currentIndex].lineNo
                    var k2Scan = 0
                    var k3Scan = 0

                    k2Scan = getK2Scan(i)
                    k3Scan = K3PDSArray[currentIndex].k3Scan+1
                    if(k3Scan <= k2Scan){
                        var flagDup = 0
                        flagDup = checkDupPartTag(value)
                        when (flagDup) {
                            1 -> {
                                editTextBarcode.requestFocus()
                                editTextBarcode.text.clear()
                                changeRed(this,"เอกสาร PartTag ซ้ำ กรุณายิงใหม่") // duplicate
                            }
                            -1 -> {
                                editTextBarcode.requestFocus()
                                editTextBarcode.text.clear()
                                changeRed(this,"Part Tag ไม่พบข้อมูล") // not found
                            }
                            else -> {
                                savePartTag(textViewPDS.text.toString(), K3PDSArray[currentIndex].partNo, value)
                                AsyncDB(this,layoutInflater,getTodayDateTime(),value).execute()
                            }
                        }
                    }
                    else{
                        Toast.makeText(this,"This PartNo did not passed K2",Toast.LENGTH_SHORT).show()
                        changeRed(this,"PartNo ยังไม่ผ่าน K2") // did not pass k2
                        scanProcess = 1
                        textViewCurrentStep.text = "E-KANBAN"
                        editTextBarcode.text.clear()
                        editTextBarcode.requestFocus()
                    }
                }
            }
        }
    }

    fun getK2Scan(i:Int) : Int{
        var qty:Int = 0
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

            result = statement.executeQuery("SELECT ISNULL(k2_scan,0) as k2_scan " +
                    " FROM tbl_Transactions  "+
                    " WHERE PDS_number = '${textViewPDS.text.toString()}'" +
                    " AND Kanban_no='${K3PDSArray[currentIndex].kanbanNo}'"+
                    " AND line_no= '${K3PDSArray[currentIndex].lineNo}'" +
                    " AND part_no='${K3PDSArray[currentIndex].partNo}'")
            while(result.next()){
                qty = result.getInt(1)
            }
            conn.close()
        }catch (e: Exception){
            println(e)
        }
        return qty
    }

    fun checkDupPartTag(value:String):Int{
        var flag:Int = -1
        var checkPartNo:String? = ""
        var modify:String? = ""
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

            result = statement.executeQuery("SELECT distinct pds_no, part_no, k2_parttag, k3last_usermodify " +
                    "  FROM tbl_PhotoSpecK2K3Transaction " +
                    "  WHERE pds_no='${textViewPDS.text.toString()}' and part_no='${K3PDSArray[currentIndex].partNo}' and k2_parttag='$value' " +
                    "  group by pds_no, part_no, k2_parttag, k3last_usermodify ")
            while(result.next()){
                partNo = result.getString(2)
                modify = result.getString(4)
            }

            flag = if(partNo == null || partNo == ""){
                -1
            } else{
                if(modify == null || modify == ""){
                    0
                } else{
                    1
                }
            }
            conn.close()
        }catch (e: Exception){
            flag = -1
            println(e)
        }
        return flag
    }

    fun savePartTag(pds:String, partNo: String, partTag:String){
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? =null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.prepareCall("{call [dbo].[UpdatePartTagK2K3](?,?,?,?)}")
            statement.setString("@PDS_No", pds)
            statement.setString("@Part_No", partNo)
            statement.setString("@K2_PartTag", partTag)
            statement.setString("@K3Last_UserModify", "${Login.mUser}")
            statement.execute()
            conn.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private class AsyncDB(val context: Context, val layoutInflater: LayoutInflater, val today: String, val partTag: String) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var message = ""

        override fun doInBackground(vararg params: String?): String {
            if (!saveFlag){
                //saveDB
                saveFlag = syncDB(K3PDSArray[currentIndex].lineNo)
                if(!saveFlag){
                    //verifyDB
                    saveFlag = verifyDB(K3PDSArray[currentIndex].lineNo)
                    if(!saveFlag){
                        checkSave = false
                    }
                }
            }

            if(saveFlag){
                DocItemNo += 1
                if(DOCNO_OUT == ""){
                    saveFlag = false
                    if(!saveFlag){
                        DOCNO_OUT = findDocNo("OU")
                        saveFlag = saveHeader("K2", DOCNO_OUT, "OUT", textViewPDS.text.toString(), "K2->K3")
                        if(!saveFlag){
                            checkSave = false
                            message = "Error K2 OUT - Doc Header"
                        }
                        else{
                            saveFlag = false
                            if(!saveFlag){
                                while(!verifyStockTransaction(DOCNO_OUT)){
                                    saveFlag = saveData("K2", DOCNO_OUT, "OUT", currentIndex)
                                }
                                if(!saveFlag){
                                    checkSave = false
                                    message = "Error K2 OUT - Doc Detail"
                                }
                            }
                        }
                    }
                }
                println(message)
            }
            return "OK"
        }

        fun findDocNo(docType: String):String{
            var count = 0
            var datetime = ""
            var docNo = ""
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            datetime = today.substring(2, 14)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
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

        fun saveHeader(location: String, id: String, docType: String, pds: String, msg: String):Boolean{
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.prepareCall("{call [dbo].[SaveHeader](?,?,?,?,?)}")
                statement.setString(1, id)
                statement.setString(2, docType)
                statement.setString(3, pds)
                statement.setString(4, msg)
                statement.setString(5, Login.mUser)
                statement.execute()
                conn.close()
                checkSave = true
                return true
            }catch (e: Exception){
                e.printStackTrace()
                return false
                println(e)
            }
        }

        fun saveData(location: String, id: String, docType: String, i: Int):Boolean{
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            var current_qty:Int
            var new_qty:Int
            var qty:Int
            var in_qty:Int
            var out_qty:Int
            var qty_sign:Int
            var partNo = K3PDSArray[i].partNo

//            try {
//                Class.forName(classs)
//                conn = DriverManager.getConnection(ConnURL)
//                val statement = conn.createStatement()
//                result = statement.executeQuery("SELECT Part_No,PhotoSerial,Serial_No FROM tbl_PhotoSpecK2K3Transaction " +
//                        "WHERE PDS_No = '${textViewPDS.text.toString()}' AND Part_No = '$partNo' AND K2_PartTag = '$partTag'")
//            }catch (e:Exception){
//                e.printStackTrace()
//            }

            current_qty = getQty(partNo, location)
            qty = K3PDSArray[i].packSzie // change pack qty to pack size
            if(docType == "IN"){
                qty_sign = 1
                in_qty = qty
                out_qty = 0
            }
            else{
                qty_sign = -1
                in_qty = 0
                out_qty = qty
            }
            println("$current_qty  $qty")
            new_qty = current_qty + (qty * qty_sign)

            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                statement.execute("INSERT INTO tbl_StockTransaction (doc_no, item_no, part_number, kanban_serial, photo_spec, location_id, qty_sign, current_qty, in_qty, out_qty, new_qty, doc_datetime, doc_user)" +
                        " VALUES('$id', '$DocItemNo', '$partNo', '', '${partNo}-000', '$location', " +
                        "'$qty_sign', '$current_qty', '$in_qty', '$out_qty', '$new_qty', getdate(), '${Login.mUser}')")

                var updateFlag = false
                if(docType == "IN"){
                    updateFlag = false
                    if(!updateFlag){
                        if(verifyStockTransaction(id)){
                            updateFlag = updateStock(partNo, location, qty)
                            if(!updateFlag){
                                message = "Error Update Stock"
                            }
                        }
                    }
                    println(message)
                }
                else{
                    updateFlag = false
                    if(!updateFlag){
                        if(verifyStockTransaction(id)) {
                            updateFlag = updateStock(partNo, location, (qty * -1))
                            if (!updateFlag) {
                                message = "Error Update Stock"
                            }
                            println(message)
                        }
                    }
                }
                conn.close()
                checkSave = true
                return true
            }catch (e: Exception){
                e.printStackTrace()
                message = "ERROR"
                println(message)
                return false
                println(e)
            }
        }

        fun verifyStockTransaction(id:String):Boolean{
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? =null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            try{
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                result = statement.executeQuery("SELECT * FROM tbl_StockTransaction WHERE doc_no = '$id' AND location_id = 'K2'")
                return result.next()

            }catch (e:java.lang.Exception){
                e.printStackTrace()
                return false
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
                return false
                println(e)
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
                while(result.next()){
                    count = result.getInt(1)
                }
                conn.close()
            }catch (e: Exception){
                println(e)
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
                println("SELECT qty FROM tbl_StockByLocation WHERE part_number= '$partNo' AND location_id='$location'")
                result  = statement.executeQuery("SELECT qty FROM tbl_StockByLocation WHERE part_number= '$partNo' AND location_id='$location'")
                while (result.next()){
                    qty2 = result.getInt(1)
                }

                if(qty2 == null){
                    qty2 = 0
                }
                conn.close()
            }catch (e: Exception){
                println(e)
            }
            return qty1+qty2
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
            pgd.dismiss()
            if(!saveFlag){
                alertDialog("บันทึกข้อมูลไม่ได้!", "RETRY")
            }
            else{
                textViewECount.text = "E : "
                K3PDSArray[currentIndex].k3Scan = K3PDSArray[currentIndex].k3Scan+1
                totalK3Qty += 1
                textViewEKBQty.text = "$totalK3Qty / $totalPackQty"
                if(totalK3Qty == totalPackQty){
                    scanProcess = 0
                    pds = true
                    textViewEKB.text = ""
                    textViewCurrentStep.text = ">> PDS Document"
                    editTextBarcode.hint = "Scan PDS NO."
                    saveFlag = false
                    editTextBarcode.requestFocus()
                    K3().visible()
//                    alertDialog("PDS Completed","OK")
                }
                else{
                    scanProcess = 1
                    pds = false
                    textViewEKB.text = ""
                    textViewCurrentStep.text = ">> E - Kanban"
                    editTextBarcode.hint = "SCAN E-KANBAN"
                    editTextBarcode.requestFocus()
                    K3().gone()
                    saveFlag = false

                }
            }

        }

        fun alertDialog(text: String, button: String){
            val builder= AlertDialog.Builder(context)
            val inflater=layoutInflater
            val view=inflater.inflate(R.layout.alert_dialog, null)
            builder.setView(view)
            dialog =builder.create()
            dialog.show()
            dialog.setCancelable(false)
            textViewText = view.findViewById(R.id.txt_text)
            buttonYes = view.findViewById(R.id.btn_ok)

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
                if(button == "RETRY"){
                    pgd.dismiss()
                    AsyncDB(context, layoutInflater, today,partTag).execute()
                    dialog.dismiss()
                }
                else{
                    pgd.dismiss()
                    dialog.dismiss()
                    if(text == "PDS Completed"){
//                        MainActivity().pdsDialog(context, layoutInflater)
                    }
                }
            }

        }

        private fun syncDB(line: Int):Boolean{
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
                println("${K3PDSArray[currentIndex].k3Scan}")
                println("${textViewPDS.text}")
                println("${K3PDSArray[currentIndex].lineNo}")
                println("${K3PDSArray[currentIndex].partNo}")
                val update  = statement.executeUpdate("UPDATE tbl_Transactions " +
                        "SET " +
                        "k3_scan = '${K3PDSArray[currentIndex].k3Scan + 1}', " +
                        "k3_user = '${Login.mUser}', " +
                        "k3_datetime = getdate() " +
                        "WHERE PDS_number = '${textViewPDS.text}' " +
                        "AND Kanban_no = '${K3PDSArray[currentIndex].kanbanNo}' " +
                        "AND line_no = '${K3PDSArray[currentIndex].lineNo}' " +
                        "AND part_no = '${K3PDSArray[currentIndex].partNo}'; ")

                conn.close()
                return true
            }catch (e: Exception){
                println(e)
                return false
            }
        }

        private fun verifyDB(line: Int):Boolean{
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
                result  = statement.executeQuery("SELECT k3_scan FROM tbl_Transactions " +
                        "WHERE PDS_number = '${textViewPDS.text}' " +
                        "AND Kanban_no = '${K3PDSArray[currentIndex].kanbanNo}' " +
                        "AND line_no = '${K3PDSArray[currentIndex].lineNo}' " +
                        "AND part_no = '${K3PDSArray[currentIndex].partNo}'; ")
                while (result.next()){
                    val k3_scan = result.getString(1)
                    return Integer.parseInt(k3_scan) == K3PDSArray[currentIndex].k3Scan+1
                }

                conn.close()
                return true
            }catch (e: Exception){
                println(e)
                return false
            }
        }
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


    private class AsyncLoadPDS(val context: Context, val pds: String, val layoutInflater: LayoutInflater) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var connectionError = false

        override fun doInBackground(vararg params: String?): String {
            DOCNO_OUT = ""
            connect()
            return "OK"
        }

        override fun onPreExecute() {
            pgd = ProgressDialog(context)
            pgd.setMessage("Please Wait")
            pgd.setTitle("Loading Data")
            pgd.show()
            pgd.setCancelable(false)

            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            pgd.dismiss()
            currentIndex = -1
            if(connectionError){
                textViewPDS.text = ""
                textViewEKB.text = ""
                textViewEKBQty.text = "0 / 0"
                textViewCurrentStep.text = ">> Connection Error"
                Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
                K3().alertDialog(context, "Connection Error", layoutInflater)
                K3().gone()
                scanProcess = 0
            }
            else{
                textViewPDS.text = ""
                textViewEKB.text = ""
                if(K3PDSArray.size == 0){
                    textViewEKBQty.text = "0 / 0"
                    textViewCurrentStep.text = ">> PDS not found"
                    editTextBarcode.hint = "SCAN PDS No."
                    editTextBarcode.requestFocus()
                    Toast.makeText(context, "PDS not found", Toast.LENGTH_SHORT).show()
                    K3().alertDialog(context, "PDS not found", layoutInflater)
                    K3().gone()
                    scanProcess = 0
                    Companion.pds = true
                }
                else{
                    textViewEKBQty.text = "$totalK3Qty / $totalPackQty"
                    if(totalK3Qty== totalPackQty){
                        textViewPDS.text = pds
                        textViewCurrentStep.text = ">> PDS Document"
                        editTextBarcode.hint = "SCAN PDS No."
                        editTextBarcode.requestFocus()
                        Toast.makeText(context, "COMPLETE", Toast.LENGTH_SHORT).show()
                        scanProcess = 0
                        Companion.pds = true
                        K3().alertDialog(context, "PDS COMPLETED", layoutInflater)
                        K3().visible()
                    }
                    else{
                        textViewCurrentStep.text = ">> E-KanBan"
                        editTextBarcode.hint = "SCAN E-KANBAN"
                        Companion.pds = false
                        editTextBarcode.requestFocus()
                        Toast.makeText(context, "E-KanBan", Toast.LENGTH_SHORT).show()
                        K3().gone()
                        textViewPDS.text = pds
                        scanProcess = 1
                        case1()
                    }

                }
            }
            super.onPostExecute(result)
        }

        fun case1(){
            editTextBarcode.isEnabled = true
            editTextBarcode.isFocusable = true
            editTextBarcode.isFocusableInTouchMode = true
            editTextBarcode.requestFocus()
        }

        fun connect(): Connection? {
            k2GroupPartArray.clear()
            K3PDSArray.clear()
            val classs = "net.sourceforge.jtds.jdbc.Driver"

            var conn: Connection? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)

                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                val sql = "SELECT line_no, part_no, Kanban_no, Line_addr, Packing_size, pack_qty, unit_qty, Receiving_place, ekb_order_no, k3_scan, ISNULL(matching_scan,0) as matching_scan, Part_Name "+
                        " FROM tbl_Transactions " +
                        " WHERE (PDS_number = '${pds.replace("'", "")}')" +
                        " ORDER BY line_no"
                val result: ResultSet = statement.executeQuery(sql)
                totalK3Qty = 0
                totalPackQty = 0
                while (result.next()){
                    val ans = result.getString(1)
                    var skscan = 0
                    skscan = if(result.getInt(10) == null){
                        0
                    } else{
                        result.getInt(10)
                    }
                    K3PDSArray.add(
                        K3PdsDetailModel(
                            result.getInt(1),
                            result.getString(3),
                            result.getString(2),
                            result.getString(12),
                            skscan,
                            result.getInt(6),
                            result.getInt(5),
                            result.getString(8),
                            result.getInt(11)
                        )
                    )
                    totalPackQty += result.getInt(6)
                    totalK3Qty += skscan
                }
//                println()
                conn.close()
                connectionError = false
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            return conn
        }
    }

    fun alertDialog(context: Context, text: String, layoutInflater: LayoutInflater) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.alert_dialog, null)
        builder.setView(view)
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        textViewText = view.findViewById(R.id.txt_text)
        buttonYes = view.findViewById(R.id.btn_ok)

        textViewText.text = text

        val afd: AssetFileDescriptor = context.assets.openFd("buzz.wav")
        val player = MediaPlayer()
        player.setDataSource(
            afd.fileDescriptor,
            afd.startOffset,
            afd.length
        )
        player.prepare()
        player.start()
        val handler = Handler()
        handler.postDelayed({ player.stop() }, 1 * 1000.toLong())

        buttonYes.setOnClickListener {
            dialog.dismiss()
            if (text == "PDS COMPLETED") {
//                pdsDialog(context, layoutInflater)
            }

        }
    }

    private fun changeRed(context: Context,text:String){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border_red)
        editTextBarcode.setHintTextColor(Color.parseColor("#E71717"))
        editTextBarcode.hint = text
        val afd: AssetFileDescriptor = context.assets.openFd("buzz.wav")
        val player = MediaPlayer()
        player.setDataSource(
            afd.fileDescriptor,
            afd.startOffset,
            afd.length
        )
        player.prepare()
        player.start()
        val handler = Handler()
        handler.postDelayed({ player.stop() }, 1 * 1000.toLong())
    }

    private fun changeBlue(context: Context){
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border)
        editTextBarcode.setHintTextColor(Color.parseColor("#87898E"))
    }
}