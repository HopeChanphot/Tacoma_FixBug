package com.planetbarcode.tacoma.Model

class K1TransactionModel {
    var partNo:String = ""
    var dateTime:String = ""
    var qty = 0

    constructor(partNo: String, dateTime: String, qty: Int) {
        this.partNo = partNo
        this.dateTime = dateTime
        this.qty = qty
    }
}