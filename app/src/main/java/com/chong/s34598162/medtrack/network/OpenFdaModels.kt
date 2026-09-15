package com.chong.s34598162.medtrack.network

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results") val results: List<DrugLabelResult>
)

data class DrugLabelResult(
    @SerializedName("purpose") val purpose: List<String>?,
    @SerializedName("warnings") val warnings: List<String>?,
    @SerializedName("dosage_and_administration") val dosageAndAdministration: List<String>?,
    @SerializedName("openfda") val openfda: OpenFdaMeta?
)

data class OpenFdaMeta(
    @SerializedName("brand_name") val brandName: List<String>?,
    @SerializedName("generic_name") val genericName: List<String>?
)

data class DrugInfo(
    val brandName: String,
    val genericName: String,
    val purpose: String,
    val warnings: String,
    val dosage: String
)