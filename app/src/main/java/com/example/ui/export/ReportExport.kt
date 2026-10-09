package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

object ReportExport {
    @Volatile
    private var printingWebView: WebView? = null

    fun sharePlainText(context: Context, title: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Save or share $title"))
    }

    fun printHtml(context: Context, jobName: String, html: String) {
        val webView = WebView(context.applicationContext)
        printingWebView = webView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    ?: return
                val adapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    adapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setMinMargins(PrintAttributes.Margins(36, 36, 36, 36))
                        .build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, wrapHtml(jobName, html), "text/html", "UTF-8", null)
    }

    fun wrapHtml(title: String, inner: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8"/>
              <title>$title</title>
              <style>
                body { font-family: -apple-system, 'Segoe UI', sans-serif; color: #0f172a; font-size: 12px; margin: 16px; }
                h1 { font-size: 18px; margin: 0 0 4px 0; }
                .muted { color: #64748b; font-size: 11px; margin-bottom: 16px; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border-bottom: 1px solid #e2e8f0; padding: 6px 4px; text-align: left; vertical-align: top; }
                th { background: #f8fafc; font-size: 10px; letter-spacing: 0.04em; text-transform: uppercase; color: #475569; }
                .num { text-align: right; font-variant-numeric: tabular-nums; }
                pre { white-space: pre-wrap; font-family: ui-monospace, monospace; font-size: 11px; }
              </style>
            </head>
            <body>
              <h1>$title</h1>
              <div class="muted">TexPro ERP · double-entry mill books · generated for print / save</div>
              $inner
            </body>
            </html>
        """.trimIndent()
    }

    fun htmlFromPlain(body: String): String = "<pre>${escape(body)}</pre>"

    private fun escape(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
