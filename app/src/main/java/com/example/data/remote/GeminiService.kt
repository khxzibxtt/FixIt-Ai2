package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY.ifBlank { "" }
    }

    /**
     * Module 01: AI Rewrite Button
     * Converts rough slang/colloquial descriptions (Arabic, Urdu, Hindi, English)
     * into a professional, technical job ticket that vendors can accurately price.
     * Uses gemini-3.1-flash-lite-preview for rapid turnaround.
     */
    suspend fun rewriteJobTicket(roughText: String, category: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "Technical Scope for $category:\nIssue: $roughText\nRequired: Professional diagnostic, labor assessment, and standard safety testing. Estimated service window: 1-2 hours."
        }

        val prompt = """
            You are FixIt AI Job Ticket Rewriter.
            The user provided this rough description for a '$category' task:
            "$roughText"

            Rewrite this into a concise, professional technical job ticket.
            Preserve critical details, translate any colloquial slang from Arabic, Urdu, Hindi, or informal English into clear technical terms.
            Include:
            1. Identified Problem
            2. Likely Root Cause & Required Labor Scope
            3. Recommended Inspection Points for the technician
            Keep it under 100 words, direct, and actionable.
        """.trimIndent()

        val response = callModel(
            model = "gemini-3.1-flash-lite-preview",
            contentsJson = createSingleTurnContents(prompt),
            systemInstruction = "You are an expert technical dispatcher for FixIt physical labor marketplace."
        )

        response ?: "Standard repair ticket: $roughText"
    }

    /**
     * Module 01: Internal AI Price Estimator
     * Calculates fair market range and floor price in OMR to protect the marketplace from sabotage bids.
     * Uses gemini-3.5-flash for general intelligence.
     */
    suspend fun estimateMarketPriceRange(title: String, category: String, description: String): Pair<Double, Double> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Pair(15.0, 30.0)
        }

        val prompt = """
            You are FixIt Internal Price Floor Estimator for the Muscat / GCC on-demand labor market.
            Service: $category
            Title: $title
            Description: $description

            Respond ONLY in strictly valid JSON:
            {"floorPrice": 15.0, "ceilingPrice": 30.0, "rationale": "short explanation"}
            Values in OMR (Omani Rial).
        """.trimIndent()

        val text = callModel(
            model = "gemini-3.5-flash",
            contentsJson = createSingleTurnContents(prompt),
            responseMimeType = "application/json"
        )

        try {
            if (text != null) {
                val json = JSONObject(text.trim())
                val floor = json.optDouble("floorPrice", 12.0)
                val ceil = json.optDouble("ceilingPrice", 28.0)
                return@withContext Pair(floor, ceil)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse price estimate JSON: $text", e)
        }
        Pair(15.0, 30.0)
    }

    /**
     * Module 06 & 07: High Thinking Dispute & Triple-Verify Arbiter
     * Uses gemini-3.1-pro-preview with thinkingLevel = "HIGH"
     * Handles complex arbitration between consumer and vendor statements and photos.
     */
    suspend fun arbitrateDisputeHighThinking(
        jobTitle: String,
        category: String,
        laborFee: Double,
        disputeReason: String,
        consumerClaim: String,
        vendorDefense: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "Dispute Arbitration Analysis:\n- Evaluation: Escrow of ${laborFee} OMR is held securely.\n- Recommendation: Vendor must schedule an on-site re-fix within 24 hours per peer warranty clause. If unresponsive, re-dispatch replacement tradesperson using held funds."
        }

        val prompt = """
            FixIt Escalation Tribunal - In-Depth Arbitration:
            Job: $jobTitle ($category)
            Labor Fee held in Escrow: $laborFee OMR
            Dispute Trigger: $disputeReason
            Consumer Claim: $consumerClaim
            Vendor Defense: $vendorDefense

            Please perform a comprehensive step-by-step adjudication:
            1. Fact synthesis & credibility assessment
            2. Scope verification (did the work meet the agreed Khidmah standards?)
            3. Warranty Clause & Escrow Ruling: (Release 80% to vendor, Full refund to consumer, or mandatory 24h Re-Fix dispatch)
            4. Concrete corrective instructions for both parties
        """.trimIndent()

        val response = callModel(
            model = "gemini-3.1-pro-preview",
            contentsJson = createSingleTurnContents(prompt),
            thinkingLevel = "HIGH",
            systemInstruction = "You are the Chief Arbitrator for FixIt Escrow & Quality Assurance. You deliver impartial, legally sound, and contractually binding resolutions."
        )

        response ?: "Arbitration completed. Full escrow of ${laborFee} OMR held pending on-site inspection."
    }

    /**
     * Search Grounding with gemini-3.5-flash (with googleSearch tool)
     * Queries up-to-date market prices for parts, replacement hardware, and standard repair costs.
     */
    suspend fun searchMarketPartsGrounding(partQuery: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "Current standard market price for $partQuery is approximately 4.50 to 12.00 OMR depending on brand and grade."
        }

        val prompt = "What is the typical retail price, specifications, and common local brands for $partQuery in Oman and GCC hardware markets? Provide concise, realistic pricing."
        val toolsArray = JSONArray().apply {
            put(JSONObject().put("googleSearch", JSONObject()))
        }

        val response = callModel(
            model = "gemini-3.5-flash",
            contentsJson = createSingleTurnContents(prompt),
            toolsJson = toolsArray
        )

        response ?: "Estimated $partQuery cost: 5.0 - 15.0 OMR."
    }

    /**
     * Maps Grounding with gemini-3.5-flash (with googleMaps tool)
     * For Escort Mode (Module 05): finds nearby hardware, plumbing, or AC supply shops.
     */
    suspend fun findNearbySupplyStoresMaps(location: String, category: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "Top Recommended Supply Centers near $location for $category:\n1. Ruwi Hardware & Sanitary Mart (Sultan Qaboos St)\n2. Muscat Electrical & Plumbing Supplies (Al Khuwair Commercial Area)\n3. Modern Technical Tools Center (Ghala Industrial Area)\nAll shops support direct consumer payment in Escort Mode."
        }

        val prompt = "Find reputable hardware, plumbing, electrical, and spare parts supply stores near $location for $category materials. Provide names, neighborhood locations, and what they specialize in."
        val toolsArray = JSONArray().apply {
            put(JSONObject().put("googleMaps", JSONObject()))
        }

        val response = callModel(
            model = "gemini-3.5-flash",
            contentsJson = createSingleTurnContents(prompt),
            toolsJson = toolsArray
        )

        response ?: "Local supply stores available in Ruwi and Al Khuwair."
    }

    /**
     * Gemini Chatbot: Multi-turn conversation with system instructions
     * Maintains conversation history and thread.
     */
    suspend fun chatWithAssistant(
        history: List<Pair<String, String>>, // (role, message)
        newMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "FixIt Assistant: I am here to help you scope your repair, navigate blind bidding vs bounty bargains, or calculate escrow top-up bonuses. How can I assist you with your home or vehicle repair today?"
        }

        val contents = JSONArray()
        for ((role, text) in history) {
            val roleName = if (role == "user") "user" else "model"
            contents.put(JSONObject().apply {
                put("role", roleName)
                put("parts", JSONArray().put(JSONObject().put("text", text)))
            })
        }
        contents.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", newMessage)))
        })

        val systemInstruction = """
            You are the FixIt Senior Concierge & Technical Advisor.
            FixIt is a hyper-local reverse-auction marketplace for physical labor (Khidmah).
            Key tenets:
            1. 100% Escrow Lock: Money stays frozen through job and warranty.
            2. Peer-negotiated warranty: Consumer and vendor mutually agree on duration.
            3. Zero contact before payment: Phone numbers masked until escrow locked.
            4. Labor-first bidding: Parts sorted on-site via Multi-Receipt, Escort Mode, or No Parts.
            5. Dual Wallets: Compounding consumer top-up bonuses, vendor accumulation ledger.
            Provide friendly, knowledgeable, and concise advice.
        """.trimIndent()

        val response = callModel(
            model = "gemini-3.5-flash",
            contentsJson = contents,
            systemInstruction = systemInstruction
        )

        response ?: "I understand. How else can I help manage your FixIt order?"
    }

    private fun createSingleTurnContents(prompt: String): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            })
        }
    }

    private suspend fun callModel(
        model: String,
        contentsJson: JSONArray,
        systemInstruction: String? = null,
        thinkingLevel: String? = null,
        toolsJson: JSONArray? = null,
        responseMimeType: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.w(TAG, "GEMINI_API_KEY is not set.")
            return@withContext null
        }

        try {
            val url = "$BASE_URL$model:generateContent?key=$apiKey"
            val requestBodyJson = JSONObject().apply {
                put("contents", contentsJson)

                val generationConfig = JSONObject()
                if (responseMimeType != null) {
                    generationConfig.put("responseMimeType", responseMimeType)
                }
                if (thinkingLevel != null) {
                    val thinkingConfig = JSONObject().apply {
                        put("thinkingLevel", thinkingLevel)
                    }
                    generationConfig.put("thinkingConfig", thinkingConfig)
                    // Note: prompt specifically instructs: "Do not set maxOutputTokens" when thinking is high
                }

                if (generationConfig.length() > 0) {
                    put("generationConfig", generationConfig)
                }

                if (systemInstruction != null) {
                    val sysObj = JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                    }
                    put("systemInstruction", sysObj)
                }

                if (toolsJson != null) {
                    put("tools", toolsJson)
                }
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ($model): ${response.code} $responseBody")
                return@withContext null
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text")
                        if (text.isNotBlank()) {
                            sb.append(text)
                        }
                    }
                    if (sb.isNotEmpty()) {
                        return@withContext sb.toString()
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API: ${e.message}", e)
            null
        }
    }

    /**
     * AI-Scoped Requirements Generator
     * Generates technical checkpoints, tools/parts needed, and safety protocols.
     */
    suspend fun generateAiScopedRequirements(title: String, category: String, description: String): List<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext listOf(
                "Isolate system power / water supply line before commencing work",
                "Perform physical diagnostic inspection and verify component specs",
                "Execute precision labor replacement using standard industry trade tools",
                "Perform 15-minute operational and pressure/load test under operating conditions",
                "Document completed work with high-resolution in-app verification photos"
            )
        }

        val prompt = """
            You are FixIt Master Technical Dispatcher.
            Task: "$title"
            Category: "$category"
            Description: "$description"

            Provide 4 to 5 concise, actionable, technical checkpoints and requirements for this labor job.
            Respond ONLY in valid JSON array of strings:
            ["Checkpoint 1", "Checkpoint 2", "Checkpoint 3", "Checkpoint 4"]
        """.trimIndent()

        val text = callModel(
            model = "gemini-3.1-flash-lite-preview",
            contentsJson = createSingleTurnContents(prompt),
            responseMimeType = "application/json"
        )

        try {
            if (text != null) {
                val array = JSONArray(text.trim())
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse requirements JSON: ${e.message}")
        }

        listOf(
            "Isolate system power / water supply line before commencing work",
            "Perform physical diagnostic inspection and verify component specs",
            "Execute precision labor replacement using standard industry trade tools",
            "Perform 15-minute operational test under full operating conditions"
        )
    }

    /**
     * Anti-Disintermediation Contact Filter:
     * Detects attempts to share phone numbers, emails, social handles, or off-platform contact info.
     */
    suspend fun checkContactLeakWithAi(text: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Pair(false, "")
        }

        val prompt = """
            Analyze the following message in a physical labor marketplace chat:
            "$text"

            Does this message attempt to share personal contact details (phone number, email address, WhatsApp, Telegram, social media handles, or instructions to communicate/pay off-platform)?
            Respond ONLY with valid JSON:
            {"containsContact": true|false, "reason": "brief explanation"}
        """.trimIndent()

        val response = callModel(
            model = "gemini-3.1-flash-lite-preview",
            contentsJson = createSingleTurnContents(prompt),
            responseMimeType = "application/json"
        )

        try {
            if (response != null) {
                val json = JSONObject(response.trim())
                val contains = json.optBoolean("containsContact", false)
                val reason = json.optString("reason", "Contact info detected")
                return@withContext Pair(contains, reason)
            }
        } catch (e: Exception) {
            Log.w(TAG, "AI Contact check exception: ${e.message}")
        }
        Pair(false, "")
    }
}
