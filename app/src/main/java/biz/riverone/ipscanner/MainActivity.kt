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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * IPスキャナ
 * Copyright (C) 2018 J.Kawahara
 * 2018.2.8 J.Kawahara 新規作成
 * 2018.2.9 J.Kawahara ver.1.00 初版公開
 * 2018.2.16 J.Kawahara ver.1.01 丸型アイコンを更新
 * 2018.3.10 J.Kawahara ver.1.02 AdMob 追加
 * 2018.3.20 J.Kawahara ver.1.03 スキャンのキャンセル直後に再スキャンすると強制終了する不具合を修正
 */
class MainActivity : AppCompatActivity() {

    private val scanner = IpScanner()
    private var progressDialog: MyProgressDialog? = null
    private lateinit var ipListAdapter: IpListAdapter

    private val buttonRefresh by lazy { findViewById<Button>(R.id.buttonRefresh) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ipListAdapter = IpListAdapter(this)

        initializeControls()

        // AdMob
        MobileAds.initialize(applicationContext, "ca-app-pub-1882812461462801~4821961708")
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun initializeControls() {

        val listViewIp = findViewById<ListView>(R.id.listViewIp)
        listViewIp.adapter = ipListAdapter

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
        buttonRefresh.isEnabled = false

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

            runOnUiThread {
                buttonRefresh.isEnabled = true
            }
        }
    }
}
