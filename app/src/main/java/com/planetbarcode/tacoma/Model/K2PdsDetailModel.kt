package com.planetbarcode.tacoma.Model

class K2PdsDetailModel {
    var lineNo:Int = 0
    var partNo:String = ""
    var partName:String = ""
    var kbNo:String = ""
    var lineAddr:String = ""
    var packSize:String = ""
    var packQty:String = ""
    var unitQty:String = ""
    var receivePlace:String = ""
    var ekbOrderNo:String = ""
    var k2Scan:String = ""
    var matchScan:String = ""
    var collectTime:String = ""
    var collectDate:String = ""
    var partCode:String = ""
    var partLabel:String=""

    constructor(
        lineNo: Int,
        partNo: String,
        partName: String,
        kbNo: String,
        lineAddr: String,
        packSize: String,
        packQty: String,
        unitQty: String,
        receivePlace: String,
        ekbOrderNo: String,
        k2Scan: String,
        matchScan: String,
        collectTime: String,
        collectDate: String,
        partCode: String,
        partLabel: String
    ) {
        this.lineNo = lineNo
        this.partNo = partNo
        this.partName = partName
        this.kbNo = kbNo
        this.lineAddr = lineAddr
        this.packSize = packSize
        this.packQty = packQty
        this.unitQty = unitQty
        this.receivePlace = receivePlace
        this.ekbOrderNo = ekbOrderNo
        this.k2Scan = k2Scan
        this.matchScan = matchScan
        this.collectTime = collectTime
        this.collectDate = collectDate
        this.partCode = partCode
        this.partLabel = partLabel
    }
}