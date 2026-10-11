package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Job
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportMetadata(
    val appName: String = "FixIt",
    val applicationId: String = "com.aistudio.fixit.mktpl",
    val appVersion: String = "1.0.0",
    val platform: String = "Android OS (Native)",
    val generatedAt: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val operatorEmail: String = "khxzialt1@gmail.com",
    val dateRangeLabel: String = "Past 30 Days",
    val startDate: String,
    val endDate: String,
    val statusFilter: String = "All Issues"
)

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f

    fun generateReport(
        context: Context,
        metadata: ReportMetadata,
        jobs: List<Job>
    ): File {
        val document = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN

        // Helper to draw page header
        fun drawPageHeader(c: Canvas, isFirstPage: Boolean) {
            // Top Accent Bar (FixIt Deep Blue)
            paint.color = Color.rgb(30, 64, 175)
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 12f, paint)

            if (!isFirstPage) {
                boldPaint.color = Color.rgb(15, 23, 42)
                boldPaint.textSize = 10f
                c.drawText("FixIt • Issue Audit & Resolution Log (Cont.)", MARGIN, 26f, boldPaint)

                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.textSize = 8f
                c.drawText("Report Range: ${metadata.startDate} to ${metadata.endDate}", PAGE_WIDTH - MARGIN - 180f, 26f, textPaint)

                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 1f
                c.drawLine(MARGIN, 32f, PAGE_WIDTH - MARGIN, 32f, paint)
            }
        }

        // Helper to draw page footer
        fun drawPageFooter(c: Canvas, pageNum: Int) {
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 1f
            c.drawLine(MARGIN, PAGE_HEIGHT - 32f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 32f, paint)

            textPaint.color = Color.rgb(148, 163, 184)
            textPaint.textSize = 8f
            c.drawText("Certified by FixIt Decentralized Escrow Engine • Confidential Audit Report", MARGIN, PAGE_HEIGHT - 18f, textPaint)

            val pageLabel = "Page $pageNum"
            c.drawText(pageLabel, PAGE_WIDTH - MARGIN - boldPaint.measureText(pageLabel), PAGE_HEIGHT - 18f, boldPaint)
        }

        // --- PAGE 1: HEADER & APPLICATION METADATA ---
        drawPageHeader(canvas, isFirstPage = true)
        y = 28f

        // Brand Banner Card
        paint.color = Color.rgb(15, 23, 42) // Dark Navy
        val bannerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f)
        canvas.drawRoundRect(bannerRect, 8f, 8f, paint)

        // Accent Shield stripe
        paint.color = Color.rgb(245, 158, 11) // Amber
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + 6f, y + 54f), 4f, 4f, paint)

        // Title
        boldPaint.color = Color.WHITE
        boldPaint.textSize = 17f
        canvas.drawText("FixIt • Issue Audit & Resolution Report", MARGIN + 18f, y + 24f, boldPaint)

        textPaint.color = Color.rgb(203, 213, 225)
        textPaint.textSize = 9f
        canvas.drawText("Peer-to-Peer Labor Marketplace • Dual-Escrow Protection & Warranty Verification", MARGIN + 18f, y + 42f, textPaint)

        y += 66f

        // Metadata Card (Two Columns)
        paint.color = Color.rgb(248, 250, 252)
        val metaRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 84f)
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Left Column Metadata
        boldPaint.color = Color.rgb(30, 41, 59)
        boldPaint.textSize = 9f
        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 9f

        var metaY = y + 16f
        canvas.drawText("Application:", MARGIN + 12f, metaY, boldPaint)
        canvas.drawText("${metadata.appName} (v${metadata.appVersion})", MARGIN + 90f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Package ID:", MARGIN + 12f, metaY, boldPaint)
        canvas.drawText(metadata.applicationId, MARGIN + 90f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Target Platform:", MARGIN + 12f, metaY, boldPaint)
        canvas.drawText(metadata.platform, MARGIN + 90f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Generated At:", MARGIN + 12f, metaY, boldPaint)
        canvas.drawText(metadata.generatedAt, MARGIN + 90f, metaY, textPaint)

        // Right Column Metadata
        metaY = y + 16f
        val col2X = (PAGE_WIDTH / 2f) + 10f
        canvas.drawText("Account / User:", col2X, metaY, boldPaint)
        canvas.drawText(metadata.operatorEmail, col2X + 85f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Date Range:", col2X, metaY, boldPaint)
        canvas.drawText("${metadata.startDate}  →  ${metadata.endDate}", col2X + 85f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Range Preset:", col2X, metaY, boldPaint)
        canvas.drawText(metadata.dateRangeLabel, col2X + 85f, metaY, textPaint)

        metaY += 15f
        canvas.drawText("Status Filter:", col2X, metaY, boldPaint)
        canvas.drawText(metadata.statusFilter, col2X + 85f, metaY, textPaint)

        y += 94f

        // --- EXECUTIVE SUMMARY KPI TILES ---
        val totalIssues = jobs.size
        val completedCount = jobs.count { it.status == "completed" || it.status == "warranty_active" }
        val activeCount = jobs.count { it.status == "in_progress" || it.status == "price_locked" || it.status == "on_the_way" }
        val openCount = jobs.count { it.status == "open_for_bids" }
        val disputedCount = jobs.count { it.status == "disputed" }
        val totalEscrowVolume = jobs.sumOf { it.agreedLaborFee }

        val tileWidth = (PAGE_WIDTH - (MARGIN * 2) - 24f) / 4f
        val tileHeight = 44f

        fun drawKpiTile(x: Float, label: String, value: String, accentColor: Int) {
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(RectF(x, y, x + tileWidth, y + tileHeight), 6f, 6f, paint)

            paint.color = accentColor
            canvas.drawRoundRect(RectF(x, y, x + tileWidth, y + 3f), 2f, 2f, paint)

            boldPaint.color = Color.rgb(15, 23, 42)
            boldPaint.textSize = 14f
            canvas.drawText(value, x + 8f, y + 22f, boldPaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            canvas.drawText(label, x + 8f, y + 36f, textPaint)
        }

        drawKpiTile(MARGIN, "Total Issues", totalIssues.toString(), Color.rgb(37, 99, 235))
        drawKpiTile(MARGIN + tileWidth + 8f, "Resolved / Active", completedCount.toString(), Color.rgb(16, 185, 129))
        drawKpiTile(MARGIN + (tileWidth * 2) + 16f, "Open / Bidding", (openCount + activeCount).toString(), Color.rgb(245, 158, 11))
        drawKpiTile(MARGIN + (tileWidth * 3) + 24f, "Escrow Volume", "$${String.format("%.2f", totalEscrowVolume)}", Color.rgb(99, 102, 241))

        y += tileHeight + 16f

        // SECTION HEADER: Log of Reported Issues
        boldPaint.color = Color.rgb(15, 23, 42)
        boldPaint.textSize = 12f
        canvas.drawText("Log of Reported Issues & Resolution Workflow", MARGIN, y, boldPaint)

        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 9f
        canvas.drawText("Showing ${jobs.size} recorded incidents within selected timeframe", MARGIN + 260f, y, textPaint)

        y += 10f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        y += 12f

        // --- DRAW ISSUE ITEMS ---
        for ((index, job) in jobs.withIndex()) {
            val itemHeight = 110f

            // Check if item fits on current page
            if (y + itemHeight > PAGE_HEIGHT - 45f) {
                drawPageFooter(canvas, currentPageNumber)
                document.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas

                drawPageHeader(canvas, isFirstPage = false)
                y = 48f
            }

            // Item Background Card
            val isEven = index % 2 == 0
            paint.color = if (isEven) Color.rgb(255, 255, 255) else Color.rgb(248, 250, 252)
            val cardRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + itemHeight - 6f)
            canvas.drawRoundRect(cardRect, 6f, 6f, paint)

            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(cardRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Status Pill Color & Label
            val (statusText, statusBg, statusFg) = when (job.status) {
                "open_for_bids" -> Triple("OPEN FOR BIDS", Color.rgb(219, 234, 254), Color.rgb(29, 78, 216))
                "price_locked", "in_progress" -> Triple("IN PROGRESS", Color.rgb(254, 243, 199), Color.rgb(180, 83, 9))
                "warranty_active" -> Triple("WARRANTY ACTIVE (${job.agreedWarrantyDays}d)", Color.rgb(209, 250, 229), Color.rgb(4, 120, 87))
                "completed" -> Triple("COMPLETED & PAID", Color.rgb(220, 252, 231), Color.rgb(21, 128, 61))
                "disputed" -> Triple("DISPUTED / TRIBUNAL", Color.rgb(254, 226, 226), Color.rgb(185, 28, 28))
                else -> Triple(job.status.uppercase(), Color.rgb(241, 245, 249), Color.rgb(71, 85, 105))
            }

            // Draw Category & ID
            boldPaint.color = Color.rgb(15, 23, 42)
            boldPaint.textSize = 10f
            canvas.drawText("${index + 1}. [${job.serviceCategory.ifBlank { "General" }}] ${job.title.ifBlank { "Reported Issue #${job.id.take(8)}" }}", MARGIN + 10f, y + 16f, boldPaint)

            // Status Badge
            val badgeWidth = boldPaint.measureText(statusText) + 14f
            val badgeX = PAGE_WIDTH - MARGIN - badgeWidth - 10f
            paint.color = statusBg
            canvas.drawRoundRect(RectF(badgeX, y + 6f, badgeX + badgeWidth, y + 21f), 4f, 4f, paint)

            boldPaint.color = statusFg
            boldPaint.textSize = 7.5f
            canvas.drawText(statusText, badgeX + 7f, y + 17f, boldPaint)

            // Description Snippet
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 8.5f
            val desc = if (job.description.length > 95) "${job.description.take(95)}..." else job.description.ifBlank { "Client requested immediate on-demand repair under dual escrow terms." }
            canvas.drawText(desc, MARGIN + 10f, y + 30f, textPaint)

            // Resolution Steps Timeline
            paint.color = Color.rgb(241, 245, 249)
            val stepsRect = RectF(MARGIN + 10f, y + 38f, PAGE_WIDTH - MARGIN - 10f, y + 74f)
            canvas.drawRoundRect(stepsRect, 4f, 4f, paint)

            boldPaint.color = Color.rgb(51, 65, 85)
            boldPaint.textSize = 7.5f
            canvas.drawText("Resolution Workflow & Audit Trail:", MARGIN + 14f, y + 49f, boldPaint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 7.5f

            val step1 = "1. AI Scoped Floor (~$${String.format("%.1f", job.aiFloorPrice)})"
            val step2 = if (job.selectedVendorName != null) "2. Locked Escrow to ${job.selectedVendorName}" else "2. Bidding Open"
            val step3 = if (job.status == "completed" || job.status == "warranty_active") "3. Verified Photos & Released Payout" else "3. On-Site Inspection Pending"
            val step4 = if (job.agreedWarrantyDays > 0) "4. ${job.agreedWarrantyDays}-Day Peer Warranty Active" else "4. Resolution In Progress"

            canvas.drawText("$step1  •  $step2", MARGIN + 14f, y + 61f, textPaint)
            canvas.drawText("$step3  •  $step4", MARGIN + 14f, y + 71f, textPaint)

            // Bottom Financials & Timestamps row
            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(148, 163, 184)
            val dateStr = if (job.createdAt != null) {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(job.createdAt.seconds * 1000))
            } else {
                "Logged recently"
            }
            canvas.drawText("Location: ${job.locationName}  •  Reported: $dateStr  •  Ticket: #${job.id.take(10)}", MARGIN + 10f, y + 88f, textPaint)

            boldPaint.color = Color.rgb(16, 185, 129)
            boldPaint.textSize = 9f
            val feeText = if (job.agreedLaborFee > 0) "Escrow Fee: $${String.format("%.2f", job.agreedLaborFee)}" else "Bounty Floor: $${String.format("%.2f", job.aiFloorPrice)}"
            val feeWidth = boldPaint.measureText(feeText)
            canvas.drawText(feeText, PAGE_WIDTH - MARGIN - feeWidth - 10f, y + 88f, boldPaint)

            y += itemHeight
        }

        // Draw footer on last page
        drawPageFooter(canvas, currentPageNumber)
        document.finishPage(page)

        // Save PDF to cache/reports directory
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(reportsDir, "FixIt_Audit_Report_$timestamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    fun openOrSharePdf(context: Context, file: File, isShare: Boolean = false) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = if (isShare) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FixIt Issue & Escrow Audit Report")
                    putExtra(Intent.EXTRA_TEXT, "Detailed FixIt application audit report encompassing logged issues, resolution steps, and timestamps.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            val chooser = Intent.createChooser(intent, if (isShare) "Share PDF Report" else "Open PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Generated: ${file.name}. Saved to device.", Toast.LENGTH_LONG).show()
        }
    }
}
