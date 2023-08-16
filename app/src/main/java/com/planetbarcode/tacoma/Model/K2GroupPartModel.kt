package com.planetbarcode.tacoma.Model

class K2GroupPartModel {
    var barcode = ""
    var date = ""
    var qty = 0

    constructor(barcode: String, date: String, qty: Int) {
        this.barcode = barcode
        this.date = date
        this.qty = qty
    }
}