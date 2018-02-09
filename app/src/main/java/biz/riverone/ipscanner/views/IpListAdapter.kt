package biz.riverone.ipscanner.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import biz.riverone.ipscanner.IpScanner
import biz.riverone.ipscanner.NetUtils
import biz.riverone.ipscanner.R

/**
 * IpListAdapter.kt: IPアドレスの一覧表示用アダプタ
 * Created by kawahara on 2018/02/08.
 */
class IpListAdapter(context: Context) : BaseAdapter() {

    private var layoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val ipList = ArrayList<NetUtils.IpMacAddress>()

    fun setList(list: ArrayList<NetUtils.IpMacAddress>) {
        ipList.clear()
        list.indices.mapTo(ipList) { list[it] }
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return ipList.size
    }

    override fun getItem(position: Int): Any {
        return ipList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView
        if (cv == null) {
            cv = layoutInflater.inflate(R.layout.ip_list_item, parent, false)
        }

        val ip = ipList[position]

        val textViewIp = cv?.findViewById<TextView>(R.id.textViewIp)
        textViewIp?.text = ip.ipAddress

        val textViewMac = cv?.findViewById<TextView>(R.id.textViewMac)
        textViewMac?.text = ip.macAddress

        return cv!!
    }
}