package biz.riverone.ipscanner

import java.net.NetworkInterface
import java.util.*
import java.util.regex.Pattern

/**
 * NetUtils.kt: ネットワーク関連の関数群
 * Created by kawahara on 2018/02/08.
 */
object NetUtils {

    data class IpAddress(
            var address: String,
            var subnetMask: Short
    )

    data class IpMacAddress(
            var ipAddress: String,
            var macAddress: String
    )

    // 現在のIPアドレスとサブネットマスクのペアを取得する
    fun currentIpAddress(): List<IpAddress> {
        val result = ArrayList<IpAddress>()

        try {
            val networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaceEnumeration.hasMoreElements()) {
                val networkInterface = networkInterfaceEnumeration.nextElement()
                if (networkInterface.isLoopback) {
                    continue
                }
                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val ipAddress = interfaceAddress.address.hostAddress
                    if (isIpv4Address(ipAddress)) {
                        val mask = interfaceAddress.networkPrefixLength
                        result.add(IpAddress(ipAddress, mask))
                    }
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // 現在のIPアドレスを取得する
    fun getIpAddress(): String {
        val ipList = currentIpAddress()
        if (ipList.isEmpty()) {
            return ""
        }
        return (ipList[0].address)
    }

    private val ipAddressPattern = Pattern.compile("(([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])")

    // 引数の文字列はIPv4アドレスの形式か？
    private fun isIpv4Address(strAddress: String): Boolean =
            (ipAddressPattern.matcher(strAddress).matches())
}