package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object SupabaseRepository {
    private const val TAG = "SupabaseRepository"
    private const val BASE_URL = "https://fuavdydvadklatjnimal.supabase.co/rest/v1/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getAnonKey(): String {
        return try {
            BuildConfig.SUPABASE_ANON_KEY.ifBlank { "sb_publishable_XDrwBS-HXj1xTg-sKgU0YA_8GFw2FtU" }
        } catch (e: Exception) {
            "sb_publishable_XDrwBS-HXj1xTg-sKgU0YA_8GFw2FtU"
        }
    }

    private fun getHeaders(): Map<String, String> {
        val key = getAnonKey()
        return mapOf(
            "apikey" to key,
            "Authorization" to "Bearer $key",
            "Content-Type" to "application/json"
        )
    }

    suspend fun fetchCategories(): List<ServiceItem> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("${BASE_URL}categories?select=*")
            getHeaders().forEach { (k, v) -> reqBuilder.addHeader(k, v) }
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(body)
            val list = mutableListOf<ServiceItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ServiceItem(
                        id = obj.optString("category_id", ""),
                        name = obj.optString("display_name", ""),
                        category = obj.optString("category_id", ""),
                        description = "Service category for ${obj.optString("display_name", "")}",
                        isApproved = obj.optBoolean("is_active", true),
                        suggestedPriceFloor = obj.optDouble("ai_price_floor", 15.0),
                        suggestedPriceCeiling = obj.optDouble("ai_price_floor", 15.0) + 20.0
                    )
                )
            }
            if (list.isEmpty()) {
                listOf(
                    ServiceItem(id = "AC", name = "Air Conditioning", category = "AC", description = "AC servicing & gas fill", isApproved = true),
                    ServiceItem(id = "PLUMBING", name = "Plumbing", category = "Plumbing", description = "Pipe & leak repair", isApproved = true),
                    ServiceItem(id = "ELECTRICAL", name = "Electrical", category = "Electrical", description = "Wiring & breaker repair", isApproved = true)
                )
            } else list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories from Supabase", e)
            listOf(
                ServiceItem(id = "AC", name = "Air Conditioning", category = "AC", description = "AC servicing & gas fill", isApproved = true),
                ServiceItem(id = "PLUMBING", name = "Plumbing", category = "Plumbing", description = "Pipe & leak repair", isApproved = true)
            )
        }
    }

    suspend fun fetchJobs(): List<Job> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("${BASE_URL}jobs?select=*&limit=50")
            getHeaders().forEach { (k, v) -> reqBuilder.addHeader(k, v) }
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(body)
            val list = mutableListOf<Job>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Job(
                        id = obj.optString("job_id", ""),
                        consumerId = obj.optString("consumer_id", ""),
                        consumerName = "Ahmed Al-Balushi",
                        title = obj.optString("description", "Labor Request").take(40),
                        description = obj.optString("description", ""),
                        serviceCategory = obj.optString("category_id", "General"),
                        locationName = "Muscat, Oman",
                        biddingMode = if (obj.optString("posting_kind") == "BOUNTY") "bounty_bargain" else "blind_bidding",
                        bountyPrice = if (obj.isNull("bounty_price")) null else obj.optDouble("bounty_price"),
                        status = obj.optString("status", "open_for_bids").lowercase(),
                        isEscrowProtected = true,
                        escrowAmount = 25.0
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching jobs from Supabase", e)
            emptyList()
        }
    }

    suspend fun fetchNotifications(userId: String): List<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("${BASE_URL}notifications?select=*&user_id=eq.$userId")
            getHeaders().forEach { (k, v) -> reqBuilder.addHeader(k, v) }
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(body)
            val list = mutableListOf<Map<String, String>>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    mapOf(
                        "id" to obj.optString("notification_id", ""),
                        "title" to obj.optString("title", ""),
                        "body" to obj.optString("body", ""),
                        "kind" to obj.optString("kind", "GENERAL")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications from Supabase", e)
            emptyList()
        }
    }
}
