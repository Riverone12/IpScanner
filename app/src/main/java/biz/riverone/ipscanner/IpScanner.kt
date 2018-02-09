package biz.riverone.ipscanner

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * IpScanner.kt: ネットワーク内のIPアドレスとMACアドレスのペアを収集する
 * Created by kawahara on 2018/02/08.
 */
class IpScanner: ArrayList<NetUtils.IpMacAddress>() {

    companion object {
        private const val DUMMY_LISTEN_PORT = 65530
        private const val DUMMY_SEND_PORT = 65531
    }

    interface ScanCompleteListener {
        fun onComplete(result: IpScanner)
    }

    fun scan(completeListener: ScanCompleteListener?) {

        this.clear()

        // 現在のIPアドレスを取得する
        val ipAddressList = NetUtils.currentIpAddress()
        if (ipAddressList.isEmpty()) {
            return
        }
        val ipAddress = ipAddressList[0]
        val byteArrayMyIp = InetAddress.getByName(ipAddress.address).address

        val intMyIp = ((byteArrayMyIp[0].toInt() and 0xff) shl 24) or
                ((byteArrayMyIp[1].toInt() and 0xff) shl 16) or
                ((byteArrayMyIp[2].toInt() and 0xff) shl 8) or
                (byteArrayMyIp[3].toInt() and 0xff)

        var intMask = 0
        for (i in 1..(32 - ipAddress.subnetMask)) {
            intMask = (intMask shl 1) or 1
        }

        // ネットワークアドレスを取得する
        val intNetworkAddress = intMyIp and intMask.inv()

        Thread({
            val udpSocket = DatagramSocket(DUMMY_LISTEN_PORT)
            udpSocket.reuseAddress = true

            val buf = ByteArray(1)

            // ARPテーブルを生成するため、
            // 同一セグメントのすべてのIPアドレス向けにUDPパケットを送信する
            // （最大254回に制限する）
            val maxAddr = if (intMask > 254) 254 else intMask
            val ip = ByteArray(4)
            for (i in 1..maxAddr) {
                val targetIp = intNetworkAddress + i
                if (targetIp != intMyIp) {
                    ip[0] = ((targetIp shr 24) and 0xff).toByte()
                    ip[1] = ((targetIp shr 16) and 0xff).toByte()
                    ip[2] = ((targetIp shr 8) and 0xff).toByte()
                    ip[3] = (targetIp and 0xff).toByte()

                    val address = InetAddress.getByAddress(ip)
                    val sendPacket = DatagramPacket(buf, buf.size, address, DUMMY_SEND_PORT)
                    udpSocket.send(sendPacket)
                }
            }
            udpSocket.close()
            Thread.sleep(500)

            // ARPテーブルを参照し、有効なIPアドレスとMACアドレスの一覧を取得する
            val process = Runtime.getRuntime().exec("ip neigh show")
            readInputStream(process.inputStream)
            process.destroy()

            sortWith(Comparator{
                a,b -> sortCompare(a.ipAddress, b.ipAddress)
            })

            completeListener?.onComplete(this)

        }).start()
    }

    private fun ipStrToLong(ipStr: String): Long {
        val sp = ipStr.split(".")
        if (sp.size < 4) {
            return 0
        }
        return sp[0].toLong() * 1000000000 +
                sp[1].toLong() * 1000000 +
                sp[2].toLong() * 1000 +
                sp[3].toLong()
    }

    private fun sortCompare(ipA: String, ipB: String): Int {
        val longA = ipStrToLong(ipA)
        val longB = ipStrToLong(ipB)
        if (longA < longB) {
            return -1
        }
        if (longA > longB) {
            return 1
        }
        return 0
    }

    private fun readInputStream(inputStream: InputStream) {
        val macRegex = Regex("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}")
        val ipRegex = Regex("(([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])")

        var bufferedReader: BufferedReader? = null
        try {
            val inputReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputReader)

            var line = bufferedReader.readLine()
            while (line != null) {
                val ip = ipRegex.find(line)
                val mac = macRegex.find(line)
                if (ip != null && mac != null) {
                    this.add(NetUtils.IpMacAddress(ip.value, mac.value))
                }
                line = bufferedReader.readLine()
            }
        }
        finally {
            bufferedReader?.close()
        }
    }
}