package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "submissions",
    foreignKeys = [
        ForeignKey(
            entity = WorkSheetsEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["worksheet_uuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["worksheet_uuid"])]
)
data class SubmissionEntity (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,

    val name: String,
    @ColumnInfo(name = "id_number")
    val numberId: String,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "school_name")
    val schoolName: String,
    val answers: List<Int> = emptyList(),
    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int? = null,
    @ColumnInfo(name = "total_points")
    val totalPoints: Int? = null,

    @ColumnInfo(name = "worksheet_uuid")
    var worksheetUuid: String,
) {
    fun toSubmission(): Submission {
        return Submission(
            profile = Profile(
                name = name,
                numberId = numberId,
                className = className,
                schoolName = schoolName
            ),
            answers,
            correctAnswers,
            totalPoints,
        )
    }
}

@Keep
data class Profile (
    @SerializedName("name") val name: String,
    @SerializedName("numberId") val numberId: String,
    @SerializedName("className") val className: String,
    @SerializedName("schoolName") val schoolName: String
)

@Keep
data class Submission (
    @SerializedName("profile") val profile: Profile,
    @SerializedName("answers") val answers: List<Int> = emptyList(),
    @SerializedName("correctAnswers") var correctAnswers: Int? = null,
    @SerializedName("totalPoints") var totalPoints: Int? = null
) {
    fun toSubmissionEntity(id: Int, worksheetUuid: String): SubmissionEntity {
        return SubmissionEntity(
            id,
            name = profile.name,
            numberId = profile.numberId,
            className = profile.className,
            schoolName = profile.schoolName,
            answers,
            correctAnswers,
            totalPoints,
            worksheetUuid
        )
    }
    fun toSubmissionBody(): SubmissionBody {
        return SubmissionBody(
            name = profile.name,
            numberId = profile.numberId,
            className = profile.className,
            schoolName = profile.schoolName,
            answers
        )
    }
}

@Keep
data class SubmissionResult (
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Submission,
)

@Keep
data class SubmissionBody (
    @SerializedName("name") val name: String,
    @SerializedName("numberId") val numberId: String,
    @SerializedName("className") val className: String,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("answers") val answers: List<Int> = emptyList()

)