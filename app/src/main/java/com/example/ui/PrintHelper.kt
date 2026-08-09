package com.example.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.domain.DistributionResult
import com.example.data.entities.DistributionSettings

object PrintHelper {
    fun printDistributionResult(context: Context, result: DistributionResult, settings: DistributionSettings?) {
        val webView = WebView(context)
        
        val schoolName = settings?.schoolName?.takeIf { it.isNotBlank() } ?: "جدول التوزيع"
        val preparer = settings?.preparerName?.takeIf { it.isNotBlank() } ?: ""
        
        var html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <title>جدول التوزيع</title>
                <style>
                    body { font-family: sans-serif; padding: 20px; }
                    .header { text-align: center; margin-bottom: 20px; }
                    .school-name { font-size: 24px; font-weight: bold; }
                    .preparer { font-size: 16px; color: #555; }
                    .teacher-section { margin-bottom: 30px; border: 1px solid #ccc; padding: 10px; border-radius: 8px; }
                    .teacher-name { font-size: 18px; font-weight: bold; background-color: #f0f0f0; padding: 8px; margin-top: 0; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: right; }
                    th { background-color: #f2f2f2; }
                    .summary { margin-top: 10px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="school-name">$schoolName</div>
                    ${if (preparer.isNotBlank()) "<div class='preparer'>إعداد: $preparer</div>" else ""}
                </div>
        """.trimIndent()

        val assignmentsByTeacher = result.assignments.groupBy { it.teacherId }
        
        assignmentsByTeacher.forEach { (_, assignments) ->
            val teacherName = assignments.first().teacherName
            val totalWorkload = assignments.sumOf { it.workload }
            html += """
                <div class="teacher-section">
                    <h3 class="teacher-name">المعلم: $teacherName</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>الصف</th>
                                <th>الفصل</th>
                                <th>النصاب (حصص)</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent()
            
            assignments.forEach { a ->
                html += """
                    <tr>
                        <td>${a.gradeName}</td>
                        <td>${a.className}</td>
                        <td>${a.workload}</td>
                    </tr>
                """.trimIndent()
            }
            
            html += """
                        </tbody>
                    </table>
                    <div class="summary">إجمالي الحصص: $totalWorkload</div>
                </div>
            """.trimIndent()
        }

        html += """
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = view.createPrintDocumentAdapter("Distribution_Report")
                printManager.print("Distribution Report", printAdapter, PrintAttributes.Builder().build())
            }
        }
        
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }
}
