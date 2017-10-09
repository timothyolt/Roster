package checkin.timothyolt.com.roster.data

import android.nfc.Tag
import android.nfc.tech.MifareClassic

data class Card (
        var id: String? = null,
        var personId: String? = null,
        var techList: List<String>? = null,
        var type: Int? = null,
        var size: Int? = null,
        var blockCount: Int? = null,
        var sectorCount: Int? = null
) {
    constructor(
            id: ByteArray,
            techList: List<String>?,
            type: Int?,
            size: Int?,
            blockCount: Int?,
            sectorCount: Int?
    ) : this(
            id.joinToString("") { (it.toInt() + 128).toString(16).toLowerCase() },
            null,
            techList,
            type,
            size,
            blockCount,
            sectorCount
    )

    constructor(tag: Tag, mfc: MifareClassic) :
            this (tag.id, tag.techList?.toList(), mfc.type, mfc.size, mfc.blockCount, mfc.sectorCount)
}