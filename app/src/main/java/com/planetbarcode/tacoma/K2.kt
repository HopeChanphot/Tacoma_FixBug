package com.planetbarcode.tacoma

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.os.StrictMode.VmPolicy
import android.text.method.ScrollingMovementMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.planetbarcode.tacoma.Model.K2GroupPartModel
import com.planetbarcode.tacoma.Model.K2PdsDetailModel
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*


class K2 : AppCompatActivity() {
    companion object {
        lateinit var scrollView: ScrollView
        lateinit var linearLayout: LinearLayout
        lateinit var linearLayout1: LinearLayout
        lateinit var linearLayoutEditText: LinearLayout
        lateinit var linearLayoutLot: LinearLayout
        lateinit var editTextPDS: EditText
        lateinit var editTextBarcode: EditText
        lateinit var textViewEKB: TextView
        lateinit var textViewPTag: TextView
        lateinit var textViewPSerial: TextView
        lateinit var textViewPLabel: TextView
        lateinit var textViewPDS: TextView
        lateinit var textViewScanned: TextView
        lateinit var textViewCurrentStep: TextView
        lateinit var textViewPrinterIP: TextView
        lateinit var textViewLotQty: TextView
        lateinit var textViewEKBQty: TextView
        lateinit var imageViewPDS: ImageView
        lateinit var imageViewHome: ImageView
        lateinit var textViewText: TextView
        lateinit var textViewEcount: TextView
        lateinit var cardViewBack: CardView
        lateinit var floatingActionButton: FloatingActionButton
        lateinit var dialog: AlertDialog
        lateinit var buttonOK: Button
        lateinit var buttonCancel: Button
        lateinit var buttonYes: Button
        lateinit var buttonReprint: Button
        lateinit var K2PDSArray: ArrayList<K2PdsDetailModel>
        lateinit var k2GroupPartArray: ArrayList<K2GroupPartModel>
        var ConnURL =
            ("jdbc:jtds:sqlserver://${Login.databaseServer}:1433/${Login.databaseName};encrypt=fasle;user=${Login.databaseUser};password=${Login.databasePassword};")
        var scannedEKB = ""
        var serial = ""
        var partNo = ""
        var kanban_qty = 0
        var countLotSize = 0
        var totalLotSize = ""
        var scanProcess = 0
        var currentIndex: Int = -1
        var photoQty = 0
        var totalPackQty = 0
        var totalK2Qty = 0
        var checkSave = false
        var STORAGE_PERMISSION_CODE = 1
        //        val BTSDK = TscWifiActivity()
        var saveFlag = false
        var pds = true
        var DOCNO_IN = ""
        var DOCNO_OUT = ""
        var DocItemNo = 0
        var reprint = false
        var async_kanban_no = ""
        var async_kanbanqty = 0
        var async_pds = ""
        var async_photo = ""
        var async_partno = ""
        var async_pserial = ""
        var async_lineno = 0
        var async_ptag = ""
        var async_photoQty = 0
        var async_packsize = 0
        var saveProcess = 1
        var filename = "hprof";
        var filepath = "MyFileDir";
        var pathz = ""
        var printerIp = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_k2)
        scrollView = findViewById(R.id.scrollView)
        linearLayoutEditText = findViewById(R.id.linearLayout)
        linearLayout = findViewById(R.id.linearLayout6)
        linearLayout1 = findViewById(R.id.linearLayout7)
        linearLayoutLot = findViewById(R.id.layout_lot)
        editTextBarcode = findViewById(R.id.edt_barcode)
        textViewPDS = findViewById(R.id.txt_pds)
        textViewScanned = findViewById(R.id.txt_last_scanned)
        textViewEKB = findViewById(R.id.txt_ekanabn)
        textViewPTag = findViewById(R.id.txt_ptag)
        textViewPTag.movementMethod = ScrollingMovementMethod()
        textViewPrinterIP = findViewById(R.id.txt_printer_ip)
        textViewPTag.setHorizontallyScrolling(true)
        textViewPSerial = findViewById(R.id.txt_pserial)
        textViewPLabel = findViewById(R.id.txt_plabel)
        textViewCurrentStep = findViewById(R.id.txt_current_step)
        textViewEcount = findViewById(R.id.txt_eCount)
        textViewLotQty = findViewById(R.id.txt_lot_qty)
        textViewEKBQty = findViewById(R.id.txt_ekb_qty)
        imageViewPDS = findViewById(R.id.img_view_pds)
        imageViewHome = findViewById(R.id.img_home)
        cardViewBack = findViewById(R.id.cardView_back)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        buttonReprint = findViewById(R.id.button_reprint)
        K2PDSArray = ArrayList<K2PdsDetailModel>()
        k2GroupPartArray = ArrayList<K2GroupPartModel>()
        pds = true

        loadIp()
        textViewPrinterIP.text = "IP : $printerIp"
        editTextBarcode.requestFocus()
        editTextBarcode.nextFocusDownId = editTextBarcode.id
        editTextBarcode.showSoftInputOnFocus = false
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);  // This just hide keyboard when activity starts

        editTextBarcode.setOnClickListener {
            editTextBarcode.showSoftInputOnFocus = true
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            requestStoragePermission();
        }

        System.setErr(HProfDumpingStderrPrintStream(System.err))

        imageViewPDS.setOnClickListener {
            val intent = Intent(this, K2PdsDetail::class.java)
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
            if (textViewCurrentStep.text.toString() != ">> PDS Document") {
                pdsDialog()
            } else {
                textViewCurrentStep.text = ">> PDS Document"
                editTextBarcode.hint = "SCAN PDS No."
            }
        }

        buttonReprint.setOnClickListener {
            reprint = true
            saveFlag = true
            AsyncDB(this, layoutInflater, getTodayDateTime()).execute()
        }

        editTextBarcode.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                if (pds) {
                    reprint = false
                    if (editTextBarcode.text.toString() != "") {
                        changeblue(this)
                        textViewScanned.text = editTextBarcode.text.toString()
                        AsyncLoadPDS(
                            this,
                            editTextBarcode.text.toString(),
                            layoutInflater
                        ).execute()
                        editTextBarcode.text.clear()
                        editTextBarcode.nextFocusDownId = editTextBarcode.id
                    } else {
                        changeRed(this, "ไม่ถูกต้อง PDS") // Invalid PDS
                        Toast.makeText(this, "Please Enter PDS Barcode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    changeblue(this)
                    textViewScanned.text = editTextBarcode.text.toString()
                    matchDoc(editTextBarcode.text.toString())
                    editTextBarcode.text.clear()
                    editTextBarcode.nextFocusDownId = editTextBarcode.id
                }
            }
            false
        })


    }

    fun date(): String {
        val sdf = SimpleDateFormat("ddMMyyyyHH:mm:ss")
        return sdf.format(Date())
    }

    private class HProfDumpingStderrPrintStream(destination: OutputStream?) :
        PrintStream(destination) {
        @Synchronized
        override fun println(str: String) {
            super.println(str)
            if (str == "StrictMode VmPolicy violation with POLICY_DEATH; shutting down.") {
                // StrictMode is about to terminate us... don't let it!
                super.println("Trapped StrictMode shutdown notice: logging heap data")
                try {
                    println("DUMP")
                    Debug.dumpHprofData(pathz)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        enableStrictMode()
        val myExternalFile = File(getExternalFilesDir(filepath), filename)
        super.onCreate(savedInstanceState, persistentState)
    }

    private fun enableStrictMode() {
        if (Build.VERSION.SDK_INT >= 9) {
            enableStrictMode()
        }
        if (Build.VERSION.SDK_INT >= 16) {
            //restore strict mode after onCreate() returns.
            Handler().postAtFrontOfQueue { doEnableStrictMode() }
        }
    }

    fun doEnableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build()
        )
    }


    private fun changeRed(context: Context, text: String) {
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
        afd.close()
    }

    private fun changeblue(context: Context) {
        linearLayoutEditText.setBackgroundResource(R.drawable.cardview_border)
        editTextBarcode.setHintTextColor(Color.parseColor("#87898E"))
    }

    fun pdsDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.pdsdialog, null)
        builder.setView(view)
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        buttonOK = view.findViewById(R.id.btn_ok)
        buttonCancel = view.findViewById(R.id.btn_cancel)

        buttonOK.setOnClickListener {
            textViewCurrentStep.text = ">> PDS Document"
            editTextBarcode.hint = "SCAN PDS No."
            textViewPDS.text = ""
            textViewEKB.text = ""
            textViewPTag.text = ""
            textViewLotQty.text = ""
            textViewPLabel.text = ""
            textViewEKBQty.text = ""
            textViewPSerial.text = ""
            textViewScanned.text = ""
            textViewEcount.text = "E : "
            pds = true
            K2PDSArray.clear()
            scanProcess = 0
            linearLayout.visibility = GONE
            linearLayout1.visibility = VISIBLE
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            pds = false
            dialog.dismiss()
        }
    }

    fun searchEkanban(scannedKanban: String) {
        textViewPSerial.text = ""
        textViewPLabel.text = ""
        textViewPTag.text = ""
        textViewCurrentStep.text = ">> E - Kanban"
        editTextBarcode.hint = "SCAN E-KANBAN"
        var pds = textViewPDS.text.toString()
        var partNo = ""
        var lineNo = 0
        var totalKB = ""
        var qty = ""
        var dbEKB = ""
        var pRunning = ""
        var lineAddr = ""
        var kanbanComplete = false
        scannedEKB = scannedKanban.replace("-", "")
        if (scannedEKB.toUpperCase().takeLast(1) == "P") {
            scannedEKB = scannedEKB.dropLast(1)
        }
        for (i in 0 until K2PDSArray.size) {
            dbEKB = K2PDSArray[i].partNo.replace("-", "")
            if (dbEKB == scannedEKB) {
                if (K2PDSArray[i].k2Scan == K2PDSArray[i].packQty) {
                    kanbanComplete = true
                    currentIndex = -1
                    scanProcess = 1
                    textViewEcount.text = "E : "
                } else {
                    kanbanComplete = false
                    changeblue(this)
                    currentIndex = i
                    lineAddr = K2PDSArray[i].lineAddr
                    pRunning = (Integer.parseInt(K2PDSArray[i].k2Scan) + 1).toString().padStart(3, '0')
                    textViewEKB.text = textViewScanned.text.toString()
                    scanProcess = 2
                    textViewCurrentStep.text = ">> Part No/Serial"
                    editTextBarcode.hint = "SCAN P No/Serial"
                    textViewPTag.text = "T${textViewPDS.text}${K2PDSArray[i].partNo}-$lineAddr-$pRunning"
                    textViewEcount.text = "E : ${K2PDSArray[i].k2Scan}/${K2PDSArray[i].packQty}"
                    partNo = K2PDSArray[i].partNo
                    lineNo = K2PDSArray[i].lineNo
                    totalKB = K2PDSArray[i].k2Scan
                    qty = K2PDSArray[i].packSize
                    break
                }
            }
        }
        if(kanbanComplete){
            changeRed(this, "E-Kanban เสร็จสิ้น") // E-Kanban Comppleted
            Toast.makeText(this, "Ekanban Completed", Toast.LENGTH_SHORT).show()
        }

        if (dbEKB != scannedEKB) {
            changeRed(this, "EKanBan ไม่พบ") // E-Kanban Not found
            Toast.makeText(this, "EKanBan Not Found", Toast.LENGTH_SHORT).show()
            textViewEKB.text = ""
            textViewCurrentStep.text = ">> E - Kanban"
            scanProcess = 1
        } else {
            changeblue(this)
            countLotSize = getLotSize(pds, partNo, lineNo, totalKB)
            totalLotSize = qty
            textViewLotQty.text = countLotSize.toString() + "/" + totalLotSize
        }
    }

    fun getLotSize(pds: String, partNo: String, lineNo: Int, totalKB: String): Int {
        var difflot = 0
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        var count = 0
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.createStatement()
            result = statement.executeQuery(
                "SELECT pds_no, part_no, line_no,  K2_LotSize, ISNULL(sum(K2_ItemLot), 0) as ScanLot FROM tbl_PhotoSpecK2K3Transaction " +
                        "WHERE PDS_No = '$pds' AND Part_No='$partNo' AND line_no='$lineNo' group by pds_no, part_no, line_no, K2_LotSize "
            )
            while (result.next()) {
                difflot = result.getInt(5) - (Integer.parseInt(totalKB) * (result.getInt(4)))
            }
            conn.close()
        } catch (e: Exception) {
            println(e)
            difflot = 0
        }
        return difflot
    }

    private fun searchPartNo(value: String) {
        try {
            var partNo = ""
            var searchChar = ""
            if (value.length >= 14) {
                partNo =
                    value.substring(2, 7) + "-" + value.substring(7, 12) + "-" + value.substring(
                        12,
                        14
                    )
            } else {
                partNo = value
            }
            if (K2PDSArray[currentIndex].partNo != partNo) {
                textViewPSerial.text = ""
            } else {
                textViewPSerial.text = partNo
                textViewCurrentStep.text = ">> Part No/Serial"
                editTextBarcode.hint = "SCAN P No/Serial"
                scanProcess = 3
                editTextBarcode.requestFocus()
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    fun searchSerial(value: String) {
        var kanBan_PdsNo: Array<String>
        val splitted = value.split("-").toTypedArray()

        try{
            serial = if (value.length == 4) {
                value
            } else if (value.length == 18) {
                value
            } else if (value.split("-").toTypedArray()[0].length == 2) {
                value
            } else {
                value.substring(3, 5)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        val partNo = editTextBarcode.text.toString().substring(2,14)
        if( partNo == textViewEKB.text.toString().replace("-","")){
            textViewPSerial.text = "$serial"
            var pkQty: Int = Integer.parseInt(K2PDSArray[currentIndex].packSize) - countLotSize
            kanban_qty = getKanbanQty("${K2PDSArray[currentIndex].partNo}-$serial")

            if (kanban_qty == 0) {
                changeRed(this, "มีความผิดพลาด Kanban ถูกเบิกจนเหลือศูนย์แล้ว") //Part No/Serial is Zero
                textViewCurrentStep.text = ">> Part No/Serial"
                scanProcess = 2
                editTextBarcode.requestFocus()
            } else {
                changeblue(this)
                if (kanban_qty <= pkQty) {
                    photoQty = kanban_qty
                    kanban_qty = 0
                } else {
                    photoQty = pkQty
                    kanban_qty = kanban_qty - pkQty
                }

                val groupPart = K2PDSArray[currentIndex].partLabel
                changeblue(this)
                if (groupPart == "None") {
                    textViewCurrentStep.text = ">> Save Data"
                    saveAll()
                } else {
                    textViewCurrentStep.text = ">> Part label ($photoQty)"
                    editTextBarcode.hint = "SCAN P Label"
                    scanProcess = 4
                }
            }
        }
        else{
            changeRed(this, "Part No is not the same") //Part No not same
            textViewCurrentStep.text = ">> Part No/Serial"
            scanProcess = 2
            editTextBarcode.requestFocus()
        }
    }

    private fun saveAll() {
        partNo = "${scannedEKB.substring(0, 5)}-${scannedEKB.substring(5, 10)}-${
            scannedEKB.substring(
                10,
                12
            )
        }"
        var lineNo = K2PDSArray[currentIndex].lineNo
        async_kanban_no = "${K2PDSArray[currentIndex].partNo}-$serial"
        async_kanbanqty = kanban_qty
        async_pds = textViewPDS.text.toString()
        async_photo = "$partNo-000"
        async_partno = partNo
        async_pserial = textViewPSerial.text.toString()
        async_lineno = lineNo
        async_ptag = textViewPTag.text.toString()
        async_photoQty = photoQty
        async_packsize = Integer.parseInt(K2PDSArray[currentIndex].packSize)
        AsyncDB(this, this.layoutInflater, getTodayDateTime()).execute()

    }

    private fun getKanbanQty(doc: String): Int {
        //get Kanban Qty
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        var qty = 0
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            )
            result = statement.executeQuery(
                "SELECT isnull(kanban_qty, 0) as kanban_qty FROM tbl_Kanban_Qty " +
                        "WHERE kanban_no = '$doc';"
            )
            result.beforeFirst()
            while (result.next()) {
                qty = try {
                    result.getInt(1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
            conn.close()
        } catch (e: Exception) {
            println(e)
            qty = 0
        }
        return qty
    }

    private fun saveGroupPartTag(
        pds: String,
        lineNo: Int,
        partNo: String,
        qty: Int,
        groupPartTag: String,
        groupPartDate: String
    ) {
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.prepareCall("{call [dbo].[SaveGroupPartTag](?,?,?,?,?,?)}")
            statement.setString(1, pds)
            statement.setInt(2, lineNo)
            statement.setString(3, partNo)
            statement.setInt(4, qty)
            statement.setString(5, groupPartTag)
            statement.setString(6, groupPartDate)
            statement.execute()
            conn.close()
        } catch (e: Exception) {
            println(e)
        }
        println("DONE")
    }


    @SuppressLint("SetTextI18n")
    fun matchDoc(value: String) {
        var docType: Int

        docType = defineBarcode(value)

        if (docType == 8) {
            textViewPrinterIP.text = "IP : " + value.substring(1, value.length)
            printerIp = value.substring(1, value.length)
            setIp(printerIp)
        } else {
            when (scanProcess) {
                0 -> {
                    buttonReprint.visibility = GONE
                    linearLayoutLot.visibility = VISIBLE
                    reprint = false
                    DOCNO_IN = ""
                    DOCNO_OUT = ""
                    DocItemNo = 0
                }

                1 -> {
                    //EKanBan
                    buttonReprint.visibility = GONE
                    linearLayoutLot.visibility = VISIBLE
                    reprint = false
                    editTextBarcode.isEnabled = true
                    editTextBarcode.isFocusable = true
                    editTextBarcode.isFocusableInTouchMode = true
                    editTextBarcode.requestFocus()
                    if (docType == 1) {
                        searchEkanban(value)
                    } else {
                        editTextBarcode.requestFocus()
                        editTextBarcode.text.clear()
                        scanProcess = 1
                        textViewEcount.text = "E :"
                        changeRed(this, "EKanBan ไม่พบ") // Not found
                        Toast.makeText(this, "EKanBan Not Found", Toast.LENGTH_SHORT).show()
                    }
                }

                2 -> {
                    println(2)
                    println(docType)
                    reprint = false
                    editTextBarcode.isEnabled = true
                    editTextBarcode.isFocusable = true
                    editTextBarcode.isFocusableInTouchMode = true
                    editTextBarcode.requestFocus()
                    if (docType == 4) {
                        searchPartNo(value)
                    } else if (docType == 6) {
                        lateinit var barcode2D: Array<String>
                        var delimiterFac1: String = "|"
                        var delimiterFac2: String = ";"
                        var delimiter: String = ""
                        var barcodeMode: String = "1D"
                        var serial2D = ""

                        delimiter = if (Login.factory == "Factory1") {
                            delimiterFac1
                        } else {
                            delimiterFac2
                        }
                        try {
                            barcode2D = value.split(delimiter).toTypedArray()
                            println("barcode $barcode2D")
                            if (barcode2D.size > 1) {
                                barcodeMode = "2D"
                            }
                        } catch (e: Exception) {
                            barcodeMode = "1D"
                        }

                        if (Login.factory == "Factory1") {
                            if (barcode2D.size == 18) {
                                var pagekanban = barcode2D[14].split("/")
                                var serialkanban = pagekanban[0].padStart(4, '0')
                                textViewPSerial.text = "10${barcode2D[0].substring(2, 7)}${
                                    barcode2D[0].substring(
                                        7,
                                        12
                                    )
                                }00"
                                serial2D = "${barcode2D[11]}-$serialkanban"
                                println(textViewPTag.text.toString())
                                println(serial2D)
                            }
                        } else {
                            if (barcode2D.size == 14) {
                                textViewPSerial.text = "10${barcode2D[4].substring(0, 10)}00"
                                serial2D = barcode2D[1]
                            }
                        }
                        searchPartNo(textViewPSerial.text.toString())
                        if (textViewPSerial.text.toString() != "ERROR") {
                            searchSerial(serial2D)
                        }
                    } else {
                        textViewPSerial.text = "ERROR"
//                    alertDialog(this, "Part Serial Not Found", this.layoutInflater)
                        editTextBarcode.requestFocus()
                        editTextBarcode.text.clear()
                        changeRed(this, "Part Serial ไม่พบ") // Part Serial not found
                        Toast.makeText(this, "Part Serial Not Found", Toast.LENGTH_SHORT).show()
                    }
                }

                3 -> {
                    reprint = false
                    editTextBarcode.isEnabled = true
                    editTextBarcode.isFocusable = true
                    editTextBarcode.isFocusableInTouchMode = true
                    editTextBarcode.requestFocus()
                    if (docType == 5) {
                        println("HERERERERERER")
                        searchSerial(value)
                    } else {
                        editTextBarcode.requestFocus()
                        editTextBarcode.text.clear()
                        changeRed(this, "Part Serial ไม่พบ") // not found
                        Toast.makeText(this, "Part Serial Not Found", Toast.LENGTH_SHORT).show()
                    }
                }

                4 -> {
                    reprint = false
                    editTextBarcode.isEnabled = true
                    editTextBarcode.isFocusable = true
                    editTextBarcode.isFocusableInTouchMode = true
                    editTextBarcode.requestFocus()
                    searchGroupPartTag(value)
                }
            }
        }
    }

    private fun searchGroupPartTag(value: String) {
        var groupPart = ""
        var partCode = ""
        var groupPartTag = ""
        var groupPartSerial = ""
        var compPartCode = ""

        println("PL $value")

        if (value.length >= 11) {
            groupPart = K2PDSArray[currentIndex].partLabel
            partCode = K2PDSArray[currentIndex].partCode

            compPartCode = value.substring(0, 3)
            when (groupPart) {
                "BD20" -> {
                    groupPartSerial = value.substring(value.length - 4, value.length)
                }
                "BD22" -> {
                    groupPartSerial = value.substring(value.length - 3, value.length)
                }
                "S20" -> {
                    groupPartSerial = value.substring(value.length - 5, value.length)
                }
                "Axle" -> {
                    groupPartSerial = value.substring(value.length - 5, value.length)
                }
            }
            var workingDate: String
            if (partCode != compPartCode) {
                textViewCurrentStep.text = ">> Scan Part Label"
                editTextBarcode.requestFocus()
                editTextBarcode.text.clear()
                changeRed(this, "เอกสาร Part Label ข้อมูลไม่ตรงกัน") // Part Label not same
                Toast.makeText(this, "Part Label Not Same", Toast.LENGTH_SHORT).show()
            } else {
                workingDate = findWorkDate()
                if (checkGroupPartDup(value, workingDate)) {
                    textViewCurrentStep.text = ">> Scan Part Label"
                    editTextBarcode.requestFocus()
                    editTextBarcode.text.clear()
                    changeRed(
                        this,
                        "Part Label ซ้ำ โปรแกรมบันทึกข้อมูลนี้แล้ว กรุณายิง Label อื่นใหม่!!"
                    ) // Part Label Duplicated
                    Toast.makeText(this, "Part Label Duplicated", Toast.LENGTH_SHORT).show()
                    scanProcess = 4
                } else {
                    k2GroupPartArray.add(
                        K2GroupPartModel(
                            value,
                            workingDate,
                            Integer.parseInt(K2PDSArray[currentIndex].k2Scan) + 1
                        )
                    )
                    var itemLotQty: Int
                    var groupPartTagQty: Int

                    itemLotQty = photoQty
                    groupPartTagQty = k2GroupPartArray.size

                    if (itemLotQty == groupPartTagQty) {
                        changeblue(this)
                        textViewCurrentStep.text = ">> Save Data"
                        saveAll()
                    } else {
                        changeblue(this)
                        scanProcess = 4
                        println("$itemLotQty and $groupPartTagQty")
                        textViewCurrentStep.text = ">> Part label (${itemLotQty - groupPartTagQty})"
                        editTextBarcode.hint = "Scan P Label"
                    }
                }
            }
        } else {
            textViewCurrentStep.text = ">> Scan Part Label"
            editTextBarcode.requestFocus()
            editTextBarcode.text.clear()
            changeRed(this, "ข้อมูล Part Label ในบาร์โค้ดยาวไม่เพียงพอ") // Part Label short
            Toast.makeText(this, "Wrong Part Label", Toast.LENGTH_SHORT).show()
        }
    }

    fun findWorkDate(): String {
        var todayDateTime = getTodayDateTime().split(" ")
        var todayDate = todayDateTime[0].split("-")
        var workingDate: String

        println("today" + todayDateTime)
        if (todayDateTime[1] > "07:30:00" && todayDateTime[1] < "19:30:00") {
            workingDate = todayDate[0] + todayDate[1] + todayDate[2]
        } else {
            if (todayDateTime[1] > "19:30:01" && todayDateTime[1] < "23:59:59") {
                workingDate = getTomorrow().replace("-", "")
            } else {
                workingDate = getYesterday().replace("-", "")
            }
        }
        return workingDate
    }

    fun getTodayDateTime(): String {
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

    fun getTomorrow(): String {
        var tomorrow = "Error"
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
            result =
                statement.executeQuery("SELECT CONVERT(char(10), getdate()+1, 120) AS tomorrow")
            while (result.next()) {
                tomorrow = result.getDate(1).toString()
            }
            conn.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tomorrow
    }

    fun getYesterday(): String {
        var yesterday = "Error"
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
            result =
                statement.executeQuery("SELECT CONVERT(char(10), getdate()-1, 120) AS tomorrow")
            while (result.next()) {
                yesterday = result.getDate(1).toString()
            }
            conn.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return yesterday
    }


    fun checkGroupPartDup(groupPartTag: String, workingDate: String): Boolean {
        if (!checkGroupPartDupInArray(groupPartTag)) {
            if (!checkGroupPartDupInDatabase(groupPartTag, workingDate)) {
                return false
            }
        }
        return true
    }

    fun checkGroupPartDupInArray(groupPartTag: String): Boolean {
        try {
            for (i in 0 until k2GroupPartArray.size) {
                if (groupPartTag == k2GroupPartArray[i].barcode) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun checkGroupPartDupInDatabase(groupPartTag: String, workingDate: String): Boolean {
        val classs = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        var count = 0
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(classs)
            conn = DriverManager.getConnection(ConnURL)
            val statement = conn.createStatement()
            result = statement.executeQuery(
                "SELECT count(*) FROM tbl_PartLabel " +
                        "WHERE GroupPartTag = '$groupPartTag' " +
                        "AND GroupPartYYYYMMDD = '$workingDate';"
            )
            while (result.next()) {
                count = result.getInt(1)
            }
            conn.close()
        } catch (e: Exception) {
            println(e)
            count = 0
        }
        return count > 0
    }

    fun defineBarcode(value: String): Int {
        var firstChar: String
        var lastChar: String
        var barcode2D: Array<String>
        var delimiterFac1: String = "|"
        var delimiterFac2: String = ";"
        var delimiter: String = ""
        var barcodeMode: String = "1D"

        if (value != "") {
            firstChar = value.take(1)
            lastChar = value.takeLast(1)

            delimiter = delimiterFac1

            try {
                barcode2D = value.split(delimiter).toTypedArray()
                if (barcode2D.size > 1) {
                    barcodeMode = "2D"
                }
            } catch (e: Exception) {
                barcodeMode = "1D"
            }

            if (barcodeMode == "2D") {
                return 6
            }
            when (firstChar) {
                "T" -> return 2
                "S" -> return 3
                "1" -> return 4
                "+" -> return 5
                "@" -> return 8
                else -> {
                    var eKB = value.split("-")
                    if (eKB.size == 3) {
                        return 1
                    } else if (value.length == 12) {
                        return 1
                    } else {
                        return 7
                    }
                }
            }
        } else {
            return 0
        }

    }

    private class AsyncCheckDup(val context: Context) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var saveFlag = false

        override fun doInBackground(vararg params: String?): String {

            isHostReachable("192.168.1.3", 9100, 1000)

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

        }

        fun isHostReachable(serverAddress: String?, serverTCPport: Int, timeoutMS: Int): Boolean {
            var connected = false
            var socket: Socket?
            try {
                socket = Socket()
                val socketAddress: SocketAddress = InetSocketAddress(serverAddress, serverTCPport)
                socket.connect(socketAddress, timeoutMS)
                if (socket.isConnected()) {
                    println(true)
                    connected = true
                    socket.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                socket = null
            }
            return connected
        }
    }

    private class AsyncDB(
        val context: Context,
        val layoutInflater: LayoutInflater,
        val today: String
    ) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var message = ""
        var dupFlag = false

        override fun doInBackground(vararg params: String?): String {
            if (reprint) {
                printLabel()
            } else {
                do {
                    when (saveProcess) {
                        1 -> {
                            if (!saveFlag) {
                                savePhoto(
                                    async_pds,
                                    async_photo,
                                    async_partno,
                                    async_pserial,
                                    async_lineno,
                                    async_ptag,
                                    async_photoQty,
                                    async_packsize
                                )
                                if (!saveFlag) {
                                    println("Save photo Failed")
                                    message = "Save Photo Failed"
                                    break
                                } else {
                                    saveProcess = 2
                                }
                            }
                        }
                        2 -> {
                            updateKanbanQty(async_kanban_no, async_kanbanqty)
                            if (!saveFlag) {
                                println("Kanban Update Failed")
                                message = "Kanban Update Failed"
                                break
                            } else {
                                saveProcess = 3
                            }
                        }
                        3 -> {
                            saveGroupPartTagFromList(async_pds, async_lineno, async_partno)
                            if (!saveFlag) {
                                println("save gpart failed")
                                message = "save gpart failed"
                                break
                            } else {
                                countLotSize += photoQty
                                if(countLotSize == Integer.parseInt(totalLotSize)){
                                    saveProcess = 4
                                }
                                else{
                                    saveProcess = 1
                                    break
                                }
                            }
                        }

                        4 -> {
                            saveFlag = syncDB(K2PDSArray[currentIndex].lineNo)
                            if (!saveFlag) {
                                println("Save Failed")
                                message = "Save DB Failed"
                                break
                            } else {
                                saveProcess = 5
                            }
                        }
                        5 -> {
                            saveFlag = verifyDB(K2PDSArray[currentIndex].lineNo)
                            if (!saveFlag) {
                                println("Verify Failed")
                                message = "Verify DB Failed"
                                break
                            } else {
                                saveProcess = 6
                            }
                        }
                        6 -> {
                            DocItemNo += 1
                            if (DOCNO_OUT == "") {
                                saveFlag = false
                                if (!saveFlag) {
                                    DOCNO_OUT = findDocNo("OU")
                                    saveFlag = saveHeader(
                                        "K1",
                                        DOCNO_OUT,
                                        "OUT",
                                        textViewPDS.text.toString(),
                                        "K1->K2"
                                    )
                                    if (!saveFlag) {
                                        message = "Error K1 OUT - Doc Headder"
                                        break
                                    } else {
                                        saveProcess = 7
                                    }
                                }
                            }
                        }
                        7 -> {
                            saveFlag = false
                            if (!saveFlag) {
                                saveFlag =
                                    saveData("K1", DOCNO_OUT, "OUT", currentIndex)
                                if (!saveFlag) {
                                    message = "Error K1 OUT - Doc Detail"
                                    break
                                } else {
                                    saveProcess = 8
                                }
                            }
                        }
                        8 -> {
                            if (DOCNO_IN == "") {
                                saveFlag = false
                                if (!saveFlag) {
                                    DOCNO_IN = findDocNo("IN")
                                    saveFlag = saveHeader(
                                        "K2",
                                        DOCNO_IN,
                                        "IN",
                                        textViewPDS.text.toString(),
                                        "K1->K2"
                                    )
                                    if (!saveFlag) {
                                        message = "Error K2 IN - Doc Headder"
                                        break
                                    } else {
                                        saveProcess = 9
                                    }
                                }
                            }
                        }
                        9 -> {
                            saveFlag = false
                            if (!saveFlag) {
                                saveFlag = saveData("K2", DOCNO_IN, "IN", currentIndex)
                                if (!saveFlag) {
                                    message = "Error K2 IN - Doc Detail"
                                    break
                                } else {
                                    K2PDSArray[currentIndex].k2Scan =
                                        (Integer.parseInt(K2PDSArray[currentIndex].k2Scan) + 1).toString()
                                    totalK2Qty += 1
                                    saveProcess = 10
                                    printLabel()
                                }
                            }
                        }

                    }
                } while (saveProcess != 10)
            }

            return "OK"
        }

        private fun updateKanbanQty(kanban_no: String, kanban_qty: Int) {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.prepareCall("{call [dbo].[UpdateKanbanQty](?,?,?)}")
                statement.setString(1, kanban_no)
                statement.setInt(2, kanban_qty)
                statement.setString(3, Login.mUser)
                statement.execute()
                conn.close()
                saveFlag = true
            } catch (e: Exception) {
                e.printStackTrace()
                saveFlag = false
            }
        }

        private fun savePhoto(
            pds: String,
            photo: String,
            partNo: String,
            serial: String,
            lineNo: Int,
            partTag: String,
            photoQty: Int,
            packSize: Int
        ) {
            var arrPart = photo.split("-")
            var partLabelCount = k2GroupPartArray.size
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            var qty = 0
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                //Save Photo
                println("serial" + serial)
                println("photos" + arrPart[3])
                println("partTag" + partTag)
                println("K2ItemLot" + photoQty)
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement =
                    conn.prepareCall("{call [dbo].[SavePhotoSpecK2K3Transaction](?,?,?,?,?,?,?,?,?,?)}")
                statement.setString(1, pds)
                statement.setString(2, partNo)
                statement.setString(3, serial)//
                statement.setString(4, arrPart[3])//
                statement.setInt(5, lineNo)
                statement.setString(6, partTag)//
                statement.setInt(7, photoQty)//
                statement.setInt(8, packSize)
                statement.setString(9, Login.mUser)
                statement.setInt(10, partLabelCount)
                statement.execute()
                conn.close()
                saveFlag = true
                dupFlag = false
            } catch (e: Exception) {
                e.printStackTrace()
                saveFlag = false
                dupFlag = true
            }
        }

        private fun saveGroupPartTagFromList(pds: String, lineNo: Int, partNo: String) {
            //save GPTag
            try {
                for (i in 0 until k2GroupPartArray.size) {
                    saveGroupPartTag(
                        pds,
                        lineNo,
                        partNo,
                        k2GroupPartArray[i].qty,
                        k2GroupPartArray[i].barcode,
                        k2GroupPartArray[i].date
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun saveGroupPartTag(
            pds: String,
            lineNo: Int,
            partNo: String,
            qty: Int,
            groupPartTag: String,
            groupPartDate: String
        ) {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.prepareCall("{call [dbo].[SaveGroupPartTag](?,?,?,?,?,?)}")
                statement.setString(1, pds)
                statement.setInt(2, lineNo)
                statement.setString(3, partNo)
                statement.setInt(4, qty)
                statement.setString(5, groupPartTag)
                statement.setString(6, groupPartDate)
                statement.execute()
                conn.close()
                saveFlag = true
            } catch (e: Exception) {
                println(e)
                saveFlag = false
            }
        }

        fun findDocNo(docType: String): String {
            var count = 0
            var datetime = ""
            var docNo = ""
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            datetime = today.substring(2, 14)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                result =
                    statement.executeQuery("SELECT max(doc_no) FROM tbl_StockTransactionHeader WHERE doc_no like '$docType$datetime%'")
//                conn.close()20051
                if (result != null) {
                    while (result.next()) {
                        docNo = result.getString(1)
                    }
                } else {
                    docNo = ""
                }
                conn.close()
            } catch (e: Exception) {
                println(e)
                docNo = ""
            }
            if (docNo == "" || docNo == null) {
                docNo = docType + datetime + "000001"
            } else {
                count = Integer.parseInt(docNo.substring(14, 20)) + 1
                docNo = docType + datetime + count.toString().padStart(6, '0')
            }
            return docNo
        }

        fun saveHeader(
            location: String,
            id: String,
            docType: String,
            pds: String,
            msg: String
        ): Boolean {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
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
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
                println(e)
            }
        }

        fun saveData(location: String, id: String, docType: String, i: Int): Boolean {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            var current_qty: Int
            var new_qty: Int
            var qty: Int
            var in_qty: Int
            var out_qty: Int
            var qty_sign: Int
            var partNo = K2PDSArray[i].partNo

            current_qty = getQty(partNo, location)
            qty = Integer.parseInt(K2PDSArray[i].packSize) //change pack qty to pack size
            if (docType == "IN") {
                qty_sign = 1
                in_qty = qty
                out_qty = 0

            } else {
                qty_sign = -1
                in_qty = 0
                out_qty = qty
            }
            new_qty = current_qty + (qty * qty_sign)

            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                statement.execute(
                    "INSERT INTO tbl_StockTransaction (doc_no, item_no, part_number, kanban_serial, photo_spec, location_id, qty_sign, current_qty, in_qty, out_qty, new_qty, doc_datetime, doc_user)" +
                            " VALUES('$id', '$DocItemNo', '$partNo', '${textViewPSerial.text.toString()}', '${partNo}-000', '$location', " +
                            "'$qty_sign', '$current_qty', '$in_qty', '$out_qty', '$new_qty', getdate(), '${Login.mUser}')"
                )
                var updateFlag = false
                if (docType == "IN") {
                    updateFlag = false
                    if (!updateFlag) {
                        updateFlag = updateStock(partNo, location, qty)
                        if (!updateFlag) {
                            message = "Error Update Stock"
                        }
                    }
                    println(message)
                } else {
                    updateFlag = false
                    if (!updateFlag) {
                        updateFlag = updateStock(partNo, location, (qty * -1))
                        if (!updateFlag) {
                            message = "Error Update Stock"
                        }
                        println(message)
                    }
                }
                conn.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                message = "ERROR"
                println(message)
                return false
            }
        }

        fun updateStock(partNo: String, location: String, qty: Int): Boolean {
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
                if (isStock(partNo, location)) {
                    statement.executeUpdate("UPDATE tbl_StockByLocation SET qty=qty+($qty) WHERE part_number= '$partNo' AND location_id= '$location'")
                } else {
                    statement.executeUpdate("INSERT INTO tbl_StockByLocation (part_number, location_id, qty) VALUES('$partNo', '$location', '$qty')")
                }
                conn.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
                println(e)
            }
        }

        fun isStock(partNo: String, location: String): Boolean {
            println(partNo + location)
            var count: Int = 0
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

                result =
                    statement.executeQuery("SELECT COUNT(*) FROM tbl_StockByLocation WHERE part_number='$partNo' AND location_id='$location'")
                if (result != null) {
                    while (result.next()) {
                        count = result.getInt(1)
                    }
                } else {
                    count = 0
                }
                conn.close()
            } catch (e: Exception) {
                println(e)
            }

            return count > 0
        }

        fun getQty(partNo: String, location: String): Int {
            var qty1 = 0
            var qty2 = 0
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
                result =
                    statement.executeQuery("SELECT qty FROM tbl_StockByLocation WHERE part_number= '$partNo' AND location_id='$location'")
                if (result != null) {
                    while (result.next()) {
                        qty2 = result.getInt(1)
                    }
                } else {
                    qty2 = 0
                }
                if (qty2 == null) {
                    qty2 = 0
                }
                conn.close()
            } catch (e: Exception) {
                println(e)
            }
            return qty1 + qty2
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
            if (dupFlag) {
                reprint = false
                saveFlag = false
                alertDialog(
                    "duplicate part no/serial in K2K3Trans!",
                    "OK"
                ) // Duplicate Key in K2K3Transacrion
                scanProcess = 2
                editTextBarcode.requestFocus()
                textViewCurrentStep.text = "Part No./Serial"
            } else {
                if (!saveFlag) {
                    reprint = false
                    alertDialog(message, "RETRY") // Cannot save data
                } else {
                    textViewLotQty.text = countLotSize.toString() + "/" + totalLotSize
                    if (countLotSize != Integer.parseInt(totalLotSize)) {
                        scanProcess = 2
                        pds = false
                        reprint = false
                        textViewCurrentStep.text = "Part No (Again)"
                        editTextBarcode.hint = "SCAN Part No"
//                        textViewEcount.text = "E : "
                        editTextBarcode.requestFocus()
                        k2GroupPartArray.clear()
                        buttonReprint.visibility = GONE
                        linearLayoutLot.visibility = VISIBLE
                        saveFlag = false
                    } else {
                        DOCNO_OUT = ""
                        DOCNO_IN = ""
                        saveProcess = 1
                        textViewEKBQty.text = "$totalK2Qty / $totalPackQty"
                        if (totalK2Qty == totalPackQty) {
                            scanProcess = 0
                            pds = true
                            textViewPSerial.text = ""
                            textViewPLabel.text = ""
                            textViewPTag.text = ""
                            textViewEKB.text = ""
                            textViewCurrentStep.text = ">> PDS Document"
                            textViewEcount.text = "E : "
                            editTextBarcode.hint = "SCAN PDS No."
                            saveFlag = false
                            linearLayout.visibility = VISIBLE
                            linearLayout1.visibility = GONE
                            editTextBarcode.requestFocus()
                            buttonReprint.visibility = VISIBLE
                            linearLayoutLot.visibility = GONE
                            k2GroupPartArray.clear()
                        } else {
                            scanProcess = 1
                            pds = false
                            reprint = true
                            textViewPSerial.text = ""
                            textViewPLabel.text = ""
                            textViewPTag.text = ""
                            textViewEKB.text = ""
                            textViewCurrentStep.text = ">> E - Kanban"
                            editTextBarcode.hint = "SCAN E-KANABN"
                            textViewEcount.text = "E : "
                            editTextBarcode.requestFocus()
                            k2GroupPartArray.clear()
                            buttonReprint.visibility = VISIBLE
                            linearLayoutLot.visibility = GONE
                            saveFlag = false
                        }
                        if (!checkSave) {
                            alertDialog(
                                "ไม่สามารถส่งพิมพ์ Part Tag ได้ กรุณาพิมพ์อีกครั้ง",
                                "CLOSE"
                            ) // Cannot print
                        }

                    }
                }
            }

        }

        fun alertDialog(text: String, button: String) {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.alert_dialog, null)
            builder.setView(view)
            dialog = builder.create()
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
                if (button == "RETRY") {
                    pgd.dismiss()
                    AsyncDB(context, layoutInflater, today).execute()
                    dialog.dismiss()
                }
                if (button == "OK") {
                    pgd.dismiss()
                    dialog.dismiss()
                } else {
                    pgd.dismiss()
                    dialog.dismiss()
                    if (text == "PDS Completed") {
//                        MainActivity().pdsDialog(context, layoutInflater)
                    }
                }
            }

        }

        private fun syncDB(line: Int): Boolean {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            var partNoSerial =
                K2PDSArray[currentIndex].partNo + "-" + textViewPSerial.text.toString()
                    .substring(3, 7)
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                val update = statement.executeUpdate(
                    "UPDATE tbl_Transactions " +
                            "SET " +
                            "k2_scan = '${Integer.parseInt(K2PDSArray[currentIndex].k2Scan) + 1}', " +
                            "k2_serial = '$partNoSerial', " +
                            "k2_user = '${Login.mUser}', " +
                            "k2_datetime = getdate() " +
                            "WHERE PDS_number = '${textViewPDS.text}' " +
                            "AND ekb_order_no = '${K2PDSArray[currentIndex].ekbOrderNo}' " +
                            "AND line_no = '${K2PDSArray[currentIndex].lineNo}' " +
                            "AND part_no = '${K2PDSArray[currentIndex].partNo}'; "
                )

                conn.close()
                return true
            } catch (e: Exception) {
                println(e)
                return false
            }
        }

        private fun verifyDB(line: Int): Boolean {
            var k2_scan = ""
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
                result = statement.executeQuery(
                    "SELECT k2_scan FROM tbl_Transactions " +
                            "WHERE PDS_number = '${textViewPDS.text}' " +
                            "AND ekb_order_no = '${K2PDSArray[currentIndex].ekbOrderNo}' " +
                            "AND line_no = '${K2PDSArray[currentIndex].lineNo}' " +
                            "AND part_no = '${K2PDSArray[currentIndex].partNo}'; "
                )
//                conn.close()
                if (result != null) {
                    while (result.next()) {
                        k2_scan = result.getString(1)
                    }
                } else {
                    k2_scan = "0"
                }
                conn.close()
                return Integer.parseInt(k2_scan) == Integer.parseInt(K2PDSArray[currentIndex].k2Scan) + 1
            } catch (e: Exception) {
                println(e)
                return false
            }
        }

        fun printLabel() {
            if (saveFlag) {
                textViewCurrentStep.text = "Print Part Tag -> E-KB"
                if (isHostReachable(printerIp, 9100, 1000)) {
                    printTest()
                } else {
                    checkSave = false
                    println("Print Fail")
                }
            }
        }

        fun isHostReachable(serverAddress: String?, serverTCPport: Int, timeoutMS: Int): Boolean {
            var connected = false
            var socket: Socket?
            try {
                socket = Socket()
                val socketAddress: SocketAddress = InetSocketAddress(serverAddress, serverTCPport)
                socket.connect(socketAddress, timeoutMS)
                if (socket.isConnected) {
                    println(true)
                    connected = true
                    socket.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                checkSave = false
            } finally {
                socket = null
            }
            return connected
        }

        fun printTest(): Boolean {
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
            var pRunning = K2PDSArray[currentIndex].k2Scan.padStart(3, '0')

            v1 = K2PDSArray[currentIndex].ekbOrderNo
            v2 = K2PDSArray[currentIndex].kbNo
            v3 = K2PDSArray[currentIndex].collectTime
            v4 = K2PDSArray[currentIndex].collectDate
            v5 = K2PDSArray[currentIndex].receivePlace
            v7 = textViewPDS.text.toString()
            v8 = K2PDSArray[currentIndex].partNo
            v9 = K2PDSArray[currentIndex].packSize
            v10 = K2PDSArray[currentIndex].partName
            v11 = K2PDSArray[currentIndex].k2Scan + "/" + K2PDSArray[currentIndex].packQty
            qr =
                "T" + "$v7" + "$v8" + "-" + "${K2PDSArray[currentIndex].lineAddr}" + "-" + "$pRunning"
            val command = "DIRECTION 0,0\n" +
                    "REFERENCE 0,0\n" +
                    "OFFSET 0 mm\n" +
                    "SET REWIND OFF\n" +
                    "SET PEEL OFF\n" +
                    "SET CUTTER OFF\n" +
                    "SET PARTIAL OFF\n" +
                    "SET TEAR ON\n" +
                    "CLS\n" +
                    "BAR 167,0, 3, 399\n" +
                    "BAR 8,316, 613, 3\n" +
                    "BAR 8,132, 613, 3\n" +
                    "BAR 168,35, 453, 3\n" +
                    "BAR 366,132, 3, 184\n" +
                    "BAR 367,222, 254, 3\n" +
                    "BAR 8,53, 159, 3\n" +
                    "BAR 91,0, 3, 54\n" +
                    "BAR 8,223, 160, 3\n" +
                    "CODEPAGE 1254\n" +
                    "TEXT 517,367,\"ROMAN.TTF\",180,1,14,\"$v1\"\n" +
                    "TEXT 100,367,\"ROMAN.TTF\",180,1,16,2,\"$v2\"\n" +
                    "TEXT 100,275,\"ROMAN.TTF\",180,1,16,2,\"$v6\"\n" +
                    "TEXT 100,187,\"ROMAN.TTF\",180,1,16,2,\"$v5\"\n" +
                    "TEXT 529,279,\"ROMAN.TTF\",180,1,14,\"$v3\"\n" +
                    "TEXT 554,184,\"ROMAN.TTF\",180,1,14,\"$v4\"\n" +
                    "TEXT 556,103,\"ROMAN.TTF\",180,1,16,\"$v8\"\n" +
                    "TEXT 365,127,\"ROMAN.TTF\",180,1,8,\"$v7\"\n" +
                    "QRCODE 341,298,L,6,A,180,M2,S7,\"$qr\"\n" +
                    "TEXT 100,121,\"ROMAN.TTF\",180,1,24,2,\"$v9\"\n" +
                    "TEXT 588,53,\"ROMAN.TTF\",180,1,6,\"$v10\"\n" +
                    "TEXT 212,25,\"ROMAN.TTF\",180,1,6,\"$v11\"\n" +
                    "TEXT 588,26,\"ROMAN.TTF\",180,1,6,\"Hino Motors Manufacturing (Thailand) Ltd.\"\n" +
                    "TEXT 88,51,\"ROMAN.TTF\",180,1,6,\"Packing by\"\n" +
                    "TEXT 163,51,\"ROMAN.TTF\",180,1,6,\"Inspector\"\n" +
                    "TEXT 161,131,\"ROMAN.TTF\",180,1,6,\"Q'ty\"\n" +
                    "TEXT 160,307,\"ROMAN.TTF\",180,1,6,\"D/T No.\"\n" +
                    "TEXT 161,218,\"ROMAN.TTF\",180,1,6,\"Dock Code\"\n" +
                    "TEXT 162,393,\"ROMAN.TTF\",180,1,6,\"Part Code\"\n" +
                    "TEXT 584,389,\"ROMAN.TTF\",180,1,6,\"Order No.\"\n" +
                    "TEXT 583,306,\"ROMAN.TTF\",180,1,6,\"Deliver Time\"\n" +
                    "TEXT 584,215,\"ROMAN.TTF\",180,1,6,\"Deliver Date\"\n" +
                    "TEXT 583,124,\"ROMAN.TTF\",180,1,6,\"Part No.\"\n" +
                    "PRINT 1,1\n"

            var socket: Socket? = null
            var outputStream: OutputStream? = null
            try {
                socket = Socket(printerIp, 9100)
                socket.soTimeout = 5000
                outputStream = socket!!.getOutputStream()
                outputStream.write(command.toByteArray())
                outputStream.flush()
                socket.shutdownOutput()

                outputStream.close()
                socket.close()
                checkSave = true
                return true
            } catch (e: Exception) {
                checkSave = false
                e.printStackTrace()
                return false
            }
        }

//            try {
//                BTSDK.openport(printerIp, 9100)
//                BTSDK.clearbuffer()
//                BTSDK.sendcommand(command)
//                BTSDK.closeport()
//                checkSave = true
//                return true
//            } catch (e: Exception) {
//                checkSave = false
//                e.printStackTrace()
//                output.write("${e}\n".toByteArray())
//                println("Print Fail")
//                return false
//            }
//
//        }

    }


    private class AsyncLoadPDS(
        val context: Context,
        val pds: String,
        val layoutInflater: LayoutInflater
    ) : AsyncTask<String, String, String>() {
        lateinit var pgd: ProgressDialog
        var connectionError = false

        override fun doInBackground(vararg params: String?): String {
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
            if (connectionError) {
                textViewPDS.text = ""
                textViewEKB.text = ""
                textViewPLabel.text = ""
                textViewPSerial.text = ""
                textViewPTag.text = ""
//                textViewScanned.text = ""
                textViewEKBQty.text = "0 / 0"
                textViewCurrentStep.text = ">> ไม่สามารถติดต่อฐานข้อมูลได้"
                Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
                K2().alertDialog(
                    context,
                    "ไม่สามารถติดต่อฐานข้อมูลได้",
                    layoutInflater
                ) // connection error
                scanProcess = 0
//                case0()
            } else {
                textViewPDS.text = ""
                textViewEKB.text = ""
                textViewPLabel.text = ""
                textViewPSerial.text = ""
                textViewPTag.text = ""
//                textViewScanned.text = ""
                if (K2PDSArray.size == 0) {
                    textViewEKBQty.text = "0 / 0"
                    textViewCurrentStep.text = ">>PDS Document"
//                    editTextBarcode.hint = "SCAN PDS No."
                    K2().changeRed(context, "PDS ไม่พบ")
                    editTextBarcode.requestFocus()
                    Toast.makeText(context, "PDS not found", Toast.LENGTH_SHORT).show()
//                    K2().alertDialog(context, "PDS not found", layoutInflater)
                    linearLayout.visibility = GONE
                    linearLayout1.visibility = VISIBLE
                    scanProcess = 0
                    Companion.pds = true
//                    case0()
                } else {
                    textViewEKBQty.text = "$totalK2Qty / $totalPackQty"
                    if (totalK2Qty == totalPackQty) {
                        textViewPDS.text = pds
                        textViewCurrentStep.text = ">> PDS Document"
                        editTextBarcode.hint = "SCAN PDS No."
                        editTextBarcode.requestFocus()
                        K2().changeblue(context)
                        Toast.makeText(context, "COMPLETE", Toast.LENGTH_SHORT).show()
                        scanProcess = 0
                        Companion.pds = true
//                        case0()
                        K2().alertDialog(
                            context,
                            "เอกสารตรวจสอบสมบรูณ์แล้ว",
                            layoutInflater
                        ) //PDS Completed
                        linearLayout.visibility = VISIBLE
                        linearLayout1.visibility = GONE
                    } else {
                        textViewCurrentStep.text = ">> E-KanBan"
                        editTextBarcode.hint = "SCAN E-KANBAN"
                        linearLayout.visibility = GONE
                        linearLayout1.visibility = VISIBLE
                        Companion.pds = false
                        editTextBarcode.requestFocus()
                        K2().changeblue(context)
                        Toast.makeText(context, "E-KanBan", Toast.LENGTH_SHORT).show()
                        textViewPDS.text = pds
                        scanProcess = 1
                        case1()
                    }

                }
            }
            super.onPostExecute(result)
        }

        fun case1() {
            editTextBarcode.isEnabled = true
            editTextBarcode.isFocusable = true
            editTextBarcode.isFocusableInTouchMode = true
            editTextBarcode.requestFocus()
        }

        fun connect(): Connection? {
            k2GroupPartArray.clear()
            K2PDSArray.clear()
            val classs = "net.sourceforge.jtds.jdbc.Driver"

            var conn: Connection? = null
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                val sql =
                    "SELECT line_no, part_no, Part_Name, Kanban_no, Line_addr, Packing_size, pack_qty, unit_qty, Receiving_place, ekb_order_no, " +
                            "k2_scan, ISNULL(matching_scan,0) as matching_scan, collect_time, CONVERT(char(12), collect_date, 103) AS collect_date " +
                            "FROM tbl_Transactions " +
                            "WHERE (PDS_number = '${pds.replace("'", "")}') " +
                            "ORDER BY Line_addr"
                val result: ResultSet = statement.executeQuery(sql)
                totalK2Qty = 0
                totalPackQty = 0
                while (result.next()) {
                    val ans = result.getString(1)
                    var sk_scan = "0"
                    if (result.getString(11) == null) {
                        sk_scan = "0"
                    } else {
                        sk_scan = result.getString(11)
                    }
                    var GP = connect1(result.getString(2)).split("|")
                    K2PDSArray.add(
                        K2PdsDetailModel(
                            result.getInt(1),
                            result.getString(2),
                            result.getString(3),
                            result.getString(4),
                            result.getString(5),
                            result.getString(6),
                            result.getString(7),
                            result.getString(8),
                            result.getString(9),
                            result.getString(10),
                            sk_scan,
                            result.getString(12),
                            result.getString(13),
                            result.getString(14),
                            GP[0],
                            GP[1]
                        )
                    )
                    totalPackQty += Integer.parseInt(result.getString(7))
                    totalK2Qty += Integer.parseInt(sk_scan)
                }
                conn.close()
                connectionError = false
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            return conn
        }

        fun connect1(partNo: String): String {
            val classs = "net.sourceforge.jtds.jdbc.Driver"
            var conn: Connection? = null
            var result: ResultSet? = null
            var gp = "None|None"
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                Class.forName(classs)
                conn = DriverManager.getConnection(ConnURL)
                val statement = conn.createStatement()
                val sql =
                    "SELECT isnull([sebango],'None') +'|' + isnull([group_part],'None') FROM tbl_Autoparts WHERE part_number ='$partNo'"
                result = statement.executeQuery(sql)
                while (result.next()) {
                    gp = result.getString(1)
                }
                conn.close()
                connectionError = false
            } catch (e: Exception) {
                println(e)
                connectionError = true
            }
            return gp
        }
    }


    fun alertDialog(context: Context, text: String, layoutInflater: LayoutInflater) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.alert_dialog, null)
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
            afd.getFileDescriptor(),
            afd.getStartOffset(),
            afd.getLength()
        )
        player.prepare()
        player.start()
        val handler = Handler()
        handler.postDelayed({ player.stop() }, 1 * 1000.toLong())

        buttonYes.setOnClickListener {
            dialog.dismiss()
            if (text == "PDS COMPLETED") {
            }

        }

    }

    private fun setIp(v: String) {
        var editor = getSharedPreferences("printerIp", Activity.MODE_PRIVATE).edit()
        editor.putString("valPrinterIp", v)
        editor.apply()
    }

    private fun loadIp() {
        var prefs = getSharedPreferences("printerIp", Activity.MODE_PRIVATE)
        printerIp = prefs.getString("valPrinterIp", "").toString()
    }


    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}