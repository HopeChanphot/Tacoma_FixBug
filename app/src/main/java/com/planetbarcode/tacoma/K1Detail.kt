package com.planetbarcode.tacoma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planetbarcode.tacoma.Adapter.K1TransactionAdapter
import java.lang.Exception

class K1Detail : AppCompatActivity() {
    companion object{
        lateinit var textViewPartNo: TextView
        lateinit var textViewTotalRecord: TextView
        lateinit var recyclerView: RecyclerView
        lateinit var cardViewBack : CardView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>
        private lateinit var viewManager: RecyclerView.LayoutManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_k1_detail)

        textViewPartNo = findViewById(R.id.txt_partNo)
        textViewTotalRecord = findViewById(R.id.txt_total_record)
        recyclerView = findViewById(R.id.recycler1)
        cardViewBack = findViewById(R.id.cardView_back)

        textViewPartNo.text = "P/N : "+getPartNo()
        textViewTotalRecord.text = totalRecord()
        viewManager = LinearLayoutManager(this)
        viewAdapter = K1TransactionAdapter(K1.K1Transactions, this)

        recyclerView.apply {

            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        cardViewBack.setOnClickListener {
            finish();
            super.onBackPressed();
        }
    }

    fun totalRecord():String{
        var totalRec = 0
        for (i in 0 until K1.K1Transactions.size){
            totalRec += K1.K1Transactions[i].qty
        }
        return totalRec.toString()
    }

    fun getPartNo():String{
        return try{
            K1.K1Transactions[0].partNo
        }catch (e:Exception){
            ""
        }
    }
}