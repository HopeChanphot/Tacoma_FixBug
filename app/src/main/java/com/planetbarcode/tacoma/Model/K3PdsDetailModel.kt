package com.planetbarcode.tacoma.Model

class K3PdsDetailModel {
    var lineNo:Int = 0
    var kanbanNo = ""
    var partNo:String = ""
    var partName:String = ""
    var k3Scan:Int = 0
    var packQty:Int = 0
    var packSzie:Int = 0
    var recevingPlace:String = ""
    var matchScan:Int = 0

    constructor(lineNo: Int, kanbanNo: String, partNo: String, partName: String, k3Scan: Int, packQty: Int, packSzie: Int, recevingPlace: String, matchScan: Int) {
        this.lineNo = lineNo
        this.kanbanNo = kanbanNo
        this.partNo = partNo
        this.partName = partName
        this.k3Scan = k3Scan
        this.packQty = packQty
        this.packSzie = packSzie
        this.recevingPlace = recevingPlace
        this.matchScan = matchScan
    }
}