package com.planetbarcode.tacoma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planetbarcode.tacoma.Adapter.K3PdsDetailAdapter

class K3PdsDetail : AppCompatActivity() {
    companion object{
        lateinit var textViewPDS: TextView
        lateinit var textViewTotalRecord: TextView
        lateinit var recyclerView: RecyclerView
        lateinit var cardViewBack : CardView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>
        private lateinit var viewManager: RecyclerView.LayoutManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_k3_pds_detail)

        textViewPDS = findViewById(R.id.txt_pds)
        textViewTotalRecord = findViewById(R.id.txt_total_record)
        recyclerView = findViewById(R.id.recycler1)
        cardViewBack = findViewById(R.id.cardView_back)

        textViewPDS.text = "PDS : "+K3.textViewPDS.text.toString()
        textViewTotalRecord.text = totalRecord()
        viewManager = LinearLayoutManager(this)
        viewAdapter = K3PdsDetailAdapter(K3.K3PDSArray, this)

        recyclerView.apply {

            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        cardViewBack.setOnClickListener {
            finish();
            super.onBackPressed();
        }

        if(K3.currentIndex != -1){
            recyclerView.smoothScrollToPosition(K3.currentIndex)
        }
    }

    fun totalRecord():String{
        var totalK3Scan = 0
        var totalScan = 0
        for (i in 0 until K3.K3PDSArray.size){
            totalK3Scan += K3.K3PDSArray[i].k3Scan
            totalScan += K3.K3PDSArray[i].packQty
        }
        return "$totalK3Scan / $totalScan"
    }
}