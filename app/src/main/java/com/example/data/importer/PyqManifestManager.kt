package com.example.data.importer

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class ManifestDatasetEntry(
    val id: String,
    val exam: String,
    val year: Int?,
    val session: String,
    val file: String?,
    val expectedCount: Int,
    val actualCount: Int = 0,
    val verifiedCount: Int = 0,
    val checksum: String? = null,
    val datasetVersion: String = "1.0",
    val status: String = "NOT_IMPORTED", // "NOT_IMPORTED", "PARTIAL", "IMPORTED_UNVERIFIED", "VERIFIED"
    val verificationStatus: String = "UNVERIFIED"
)

/**
 * Manages dataset registration, SHA-256 checksumming, and manifest metadata versioning.
 * Strictly enforces that a dataset can NEVER be marked VERIFIED automatically.
 */
object PyqManifestManager {

    fun calculateSha256(content: String): String {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun parseManifest(manifestJson: String): List<ManifestDatasetEntry> {
        val list = mutableListOf<ManifestDatasetEntry>()
        try {
            val root = JSONObject(manifestJson)
            val arr = root.optJSONArray("datasets") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ManifestDatasetEntry(
                        id = obj.optString("id", ""),
                        exam = obj.optString("exam", "NEET_UG"),
                        year = if (obj.has("year") && !obj.isNull("year")) obj.getInt("year") else null,
                        session = obj.optString("session", "MAIN"),
                        file = if (obj.has("file") && !obj.isNull("file")) obj.getString("file") else null,
                        expectedCount = obj.optInt("expectedCount", 180),
                        actualCount = obj.optInt("actualCount", 0),
                        verifiedCount = obj.optInt("verifiedCount", 0),
                        checksum = if (obj.has("checksum")) obj.getString("checksum") else null,
                        datasetVersion = obj.optString("datasetVersion", "1.0"),
                        status = obj.optString("status", "NOT_IMPORTED"),
                        verificationStatus = obj.optString("verificationStatus", "UNVERIFIED")
                    )
                )
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
        return list
    }

    /**
     * Updates an entry with new dataset metrics and checksum.
     * Enforces that VERIFIED status can only be set if verifiedCount equals or exceeds expectedCount
     * AND official verification has taken place.
     */
    fun updateDatasetEntry(
        currentList: List<ManifestDatasetEntry>,
        datasetId: String,
        newFilePath: String?,
        actualCount: Int,
        verifiedCount: Int,
        contentChecksum: String,
        isExplicitlyVerified: Boolean = false
    ): List<ManifestDatasetEntry> {
        return currentList.map { entry ->
            if (entry.id == datasetId) {
                val newStatus = when {
                    actualCount == 0 -> "NOT_IMPORTED"
                    isExplicitlyVerified && verifiedCount >= entry.expectedCount -> "VERIFIED"
                    verifiedCount > 0 -> "PARTIAL"
                    actualCount < entry.expectedCount -> "PARTIAL"
                    else -> "IMPORTED_UNVERIFIED"
                }

                val newVerificationStatus = if (isExplicitlyVerified && verifiedCount > 0) "VERIFIED" else "UNVERIFIED"

                entry.copy(
                    file = newFilePath ?: entry.file,
                    actualCount = actualCount,
                    verifiedCount = verifiedCount,
                    checksum = contentChecksum,
                    status = newStatus,
                    verificationStatus = newVerificationStatus
                )
            } else {
                entry
            }
        }
    }

    fun serializeManifest(entries: List<ManifestDatasetEntry>): String {
        val root = JSONObject()
        root.put("version", "1.1")
        root.put("generatedDate", "2026-09-13")
        root.put("description", "FOCUSIN Historical PYQ Asset Manifest for AIPMT & NEET UG (2005-2025)")

        val arr = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("exam", entry.exam)
            entry.year?.let { obj.put("year", it) } ?: obj.put("year", JSONObject.NULL)
            obj.put("session", entry.session)
            entry.file?.let { obj.put("file", it) } ?: obj.put("file", JSONObject.NULL)
            obj.put("expectedCount", entry.expectedCount)
            obj.put("actualCount", entry.actualCount)
            obj.put("verifiedCount", entry.verifiedCount)
            entry.checksum?.let { obj.put("checksum", it) }
            obj.put("datasetVersion", entry.datasetVersion)
            obj.put("status", entry.status)
            obj.put("verificationStatus", entry.verificationStatus)
            arr.put(obj)
        }
        root.put("datasets", arr)
        return root.toString(2)
    }
}
