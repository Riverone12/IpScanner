package biz.riverone.ipscanner

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import biz.riverone.ipscanner.views.IpListAdapter
import biz.riverone.ipscanner.views.MyProgressDialog

/**
 * IPスキャナ
 * Copyright (C) 2018 J.Kawahara
 * 2018.2.8 J.Kawahara 新規作成
 * 2018.2.9 J.Kawahara ver.1.00 初版公開
 */
class MainActivity : AppCompatActivity() {

    private val scanner = IpScanner()
    private var progressDialog: MyProgressDialog? = null
    private lateinit var ipListAdapter: IpListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ipListAdapter = IpListAdapter(this)

        initializeControls()
    }

    private fun initializeControls() {

        val listViewIp = findViewById<ListView>(R.id.listViewIp)
        listViewIp.adapter = ipListAdapter

        val buttonRefresh = findViewById<Button>(R.id.buttonRefresh)
        buttonRefresh.setOnClickListener {
            startScan()
        }

    }

    override fun onResume() {
        super.onResume()

        // 現在のIPアドレスを表示する
        val textViewThisIp = findViewById<TextView>(R.id.textViewThisIp)
        textViewThisIp.text = NetUtils.getIpAddress()

        // スキャンを開始する
        startScan()
    }

    // スキャンを開始する
    private fun startScan() {
        // プログレスダイアログを表示する
        val message = resources.getString(R.string.progress_message)
        progressDialog = MyProgressDialog.create(Handler(), message)
        progressDialog?.show(supportFragmentManager, "dialog")

        scanner.scan(scanListener)
    }

    private val scanListener = object : IpScanner.ScanCompleteListener {
        override fun onComplete(result: IpScanner) {
            runOnUiThread {
                ipListAdapter.setList(result)
            }

            // プログレスダイアログを閉じる
            progressDialog?.cancel()
        }
    }
}