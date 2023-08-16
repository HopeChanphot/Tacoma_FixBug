package com.planetbarcode.tacoma.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.planetbarcode.tacoma.K3
import com.planetbarcode.tacoma.Model.K3PdsDetailModel
import com.planetbarcode.tacoma.R

class K3PdsDetailAdapter(private var Dataset:ArrayList<K3PdsDetailModel>, private val context: Context) : RecyclerView.Adapter<K3PdsDetailAdapter.MyViewHolder>() {
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val txtHinoKb = view.findViewById<TextView>(R.id.txt_hino_kb)
        val txtKb = view.findViewById<TextView>(R.id.txt_kb)
        val txtK3Qty = view.findViewById<TextView>(R.id.txt_k3_qty)
        val txtTotalQty = view.findViewById<TextView>(R.id.txt_total_qty)
        val txtPartName = view.findViewById<TextView>(R.id.txt_partname)
        val txtID = view.findViewById<TextView>(R.id.txt_id)
        val layout = view.findViewById<LinearLayout>(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): K3PdsDetailAdapter.MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.k3pds_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: K3PdsDetailAdapter.MyViewHolder, position: Int) {
        val txtHinoKb = holder.txtHinoKb
        val txtKb = holder.txtKb
        val txtK3Qty = holder.txtK3Qty
        val txtTotalQty = holder.txtTotalQty
        val txtPartName = holder.txtPartName
        val txtID = holder.txtID
        val layout = holder.layout

        txtHinoKb.text = Dataset[position].kanbanNo
        txtKb.text= Dataset[position].partNo
        txtTotalQty.text = Dataset[position].packQty.toString()
        txtK3Qty.text = Dataset[position].k3Scan.toString()
        txtPartName.text = Dataset[position].partName
        txtID.text = (position+1).toString()+"."

        if(txtK3Qty.text.toString() == txtTotalQty.text.toString()){
            layout.setBackgroundColor(Color.parseColor("#178BC34A"))
        }
        var index = K3.currentIndex

        if(index != -1){
            if(position == index){
                println("$position/$index")
                if(txtK3Qty.text.toString() == txtTotalQty.text.toString()){
                    layout.setBackgroundColor(Color.parseColor("#178BC34A"))
                }
                else{
                    layout.setBackgroundColor(Color.parseColor("#170099FF"))
                }
            }
            else if(txtK3Qty.text.toString() == txtTotalQty.text.toString()){
                layout.setBackgroundColor(Color.parseColor("#178BC34A"))
            }
            else{
                layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
        }
    }


    override fun getItemCount() = Dataset.size

}