package com.merahputihperkasa.prodigi.models

import android.util.Log
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken


@Entity(
    tableName = "submissions",
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
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val name: String,
    @ColumnInfo(name = "id_number")
    val numberId: String,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "school_name")
    val schoolName: String,
    val answers: List<Answer> = emptyList(),
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
data class Profile(
    @SerializedName("name") val name: String,
    @SerializedName("numberId") val numberId: String,
    @SerializedName("className") val className: String,
    @SerializedName("schoolName") val schoolName: String,
)

@Keep
sealed class Answer {
    @Keep
    data class Single(val answer: Int) : Answer()

    @Keep
    data class Multiple(val answers: List<Int>) : Answer()

    @Keep
    data object None : Answer()
}

class AnswerListConverter {
    private val gson: Gson = Gson()
    private val listType =
        object : TypeToken<List<Any?>>() {}.type // Type for deserializing the mixed list

    @TypeConverter
    fun fromString(value: String): List<Answer> {
        return try {
            val parsedList: List<Any?> = gson.fromJson(value, listType)
            parsedList.map { item ->
                when (item) {
                    is Number -> if (item.toInt() == -1) Answer.None else Answer.Single(
                        item.toInt()
                    )

                    is List<*> -> {
                        val ints = item.mapNotNull { (it as? Number)?.toInt() }
                        if (ints.isEmpty()) Answer.None else Answer.Multiple(ints)
                    }

                    else -> Answer.None
                }
            }
        } catch (e: Exception) {
            Log.e("AnswerListConverter", "Error parsing JSON: $value", e)
            throw IllegalArgumentException(
                "Invalid list format in the stored value: $value",
                e
            )
        }
    }

    @TypeConverter
    fun toString(list: List<Answer>): String {
        val simplifiedList = list.map { answer ->
            when (answer) {
                is Answer.Single -> answer.answer
                is Answer.Multiple -> answer.answers
                is Answer.None -> -1
            }
        }
        val res = gson.toJson(simplifiedList)
        return res
    }
}

@Keep
data class Submission(
    @SerializedName("profile") val profile: Profile,
    @SerializedName("answers") val answers: List<Answer> = emptyList(),
    @SerializedName("correctAnswers") var correctAnswers: Int? = null,
    @SerializedName("totalPoints") var totalPoints: Int? = null,
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
            answers = answers.map { answer ->
                when (answer) {
                    is Answer.Single -> answer.answer
                    is Answer.Multiple -> answer.answers
                    is Answer.None -> -1
                }
            }
        )
    }
}

@Keep
data class SubmissionResult(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Submission,
)

@Keep
data class SubmissionBody(
    @SerializedName("name") val name: String,
    @SerializedName("numberId") val numberId: String,
    @SerializedName("className") val className: String,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("answers") val answers: List<Any>,
)