package com.planetbarcode.tacoma.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.planetbarcode.tacoma.Model.K1TransactionModel
import com.planetbarcode.tacoma.R

class K1TransactionAdapter(private var Dataset:ArrayList<K1TransactionModel>, private val context: Context) : RecyclerView.Adapter<K1TransactionAdapter.MyViewHolder>() {
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val txtId = view.findViewById<TextView>(R.id.txt_id)
        val txtDate = view.findViewById<TextView>(R.id.txt_date)
        val txtQty = view.findViewById<TextView>(R.id.txt_qty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): K1TransactionAdapter.MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.partno_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: K1TransactionAdapter.MyViewHolder, position: Int) {
        val txtId = holder.txtId
        val txtDate = holder.txtDate
        val txtQty = holder.txtQty

        txtId.text = (position+1).toString()+"."
        txtDate.text= Dataset[position].dateTime
        txtQty.text = Dataset[position].qty.toString()
    }


    override fun getItemCount() = Dataset.size

}