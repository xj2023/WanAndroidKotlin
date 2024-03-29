package com.kx.kotlin.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.kx.kotlin.R
import com.kx.kotlin.base.BaseActivity
import com.kx.kotlin.base.BaseObserver
import com.kx.kotlin.constant.Constant
import com.kx.kotlin.ext.showToast
import com.kx.kotlin.http.RetrofitHelper
import com.kx.kotlin.util.RxUtil
import kotlinx.android.synthetic.main.activity_articles_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ArticlesDetailActivity : BaseActivity() {

    private lateinit var shareTitle: String
    private lateinit var shareUrl: String
    private var shareId: Int = 0

    companion object {
        fun start(context :Context? ,id: Int, title: String, url: String) {
            context ?: return
            val intent = Intent(context, ArticlesDetailActivity::class.java)
            intent.run {
                putExtra(Constant.CONTENT_ID_KEY, id)
                putExtra(Constant.CONTENT_TITLE_KEY,title)
                putExtra(Constant.CONTENT_URL_KEY, url)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_detail)

        intent.extras?.let {
            shareId = it.getInt(Constant.CONTENT_ID_KEY, -1)
            shareTitle = it.getString(Constant.CONTENT_TITLE_KEY, "")
            shareUrl = it.getString(Constant.CONTENT_URL_KEY, "")
        }

        toolbar.apply {
            title = ""//getString(R.string.loading)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener {
                finish()
            }
            //StatusBarUtil2.setPaddingSmart(this@ContentActivity, toolbar)
        }
        tv_title.apply {
            text = "正在加载中..."
            visibility = View.VISIBLE
            tv_title.isSelected = true
//            postDelayed({
//                tv_title.isSelected = true
//            }, 2000)
        }

        webView.run {
            val settings = webView.settings
            settings.javaScriptEnabled = true
            loadUrl(shareUrl)
            webViewClient = this@ArticlesDetailActivity.webViewClient
            webChromeClient = this@ArticlesDetailActivity.webChromeClient

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private val webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            try {
                //  拦截 H5 页面的恶意跳转
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view?.loadUrl(url)
                } else {
                    val host =  getHost(shareUrl)
                    return false
//                    if (!TextUtils.isEmpty(host) && url.contains(host)){
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                        startActivity(intent)
//                    }else{
//                        return false
//                    }
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            showToast("加载失败: ${error?.description}")
        }
    }

    private val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            title.let {
                // toolbar.title = it
                tv_title.text = it
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_content, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                Intent().run {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(
                            R.string.share_article_url,
                            getString(R.string.app_name),
                            shareTitle,
                            shareUrl
                        )
                    )
                    type = Constant.CONTENT_SHARE_TYPE
                    startActivity(Intent.createChooser(this, getString(R.string.action_share)))
                }
                return true
            }
            R.id.action_like -> {
                if (isLogin) {
                    addCollectArticle(shareId)
                } else {
                    Intent(this, LoginActivity::class.java).run {
                        startActivity(this)
                    }
                    showToast(resources.getString(R.string.login_tint))
                }
                return true
            }
            R.id.action_browser -> {
                Intent().run {
                    action = "android.intent.action.VIEW"
                    data = Uri.parse(shareUrl)
                    startActivity(this)
                }
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun addCollectArticle(id: Int) {
        addDisposable(
            RetrofitHelper.service.addCollectArticle(id)
                .compose(RxUtil.ioMain())
                .subscribeWith(object : BaseObserver<Any>() {
                    override fun onSuccess() {
                        showToast(getString(R.string.collect_success))
                    }
                    override fun onError(errorMsg: String) {
                        showToast(errorMsg)
                    }
                })
        )
    }

    /**
     * 正则获取url的Host
     *  eg.   https://www.jianshu.com/p/2c106b682cfb?utm_source=desktop&amp;utm_medium=timeline   返回  www.jianshu.com
     */
    fun  getHost(url : String ): String {
        val pattern = Pattern.compile("^http[s]?:\\/\\/(.*?)([:\\/]|$)")// 匹配的模式
        val matcher = pattern.matcher(url)
        while (matcher.find()) {
            return matcher.group(1)
        }
        return ""
    }
}
