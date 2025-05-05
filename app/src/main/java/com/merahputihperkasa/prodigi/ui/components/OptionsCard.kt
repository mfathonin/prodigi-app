package com.merahputihperkasa.prodigi.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.Answer

val optionTFLabel = listOf("True", "False")
val optionsLabel = listOf("A", "B", "C", "D", "E")

@Composable
fun OptionsCard(
    answers: MutableState<List<Answer>>,
    option: Int,
    index: Int,
    modes: Int,
    modifier: Modifier = Modifier,
    onChanged: () -> Unit = {},
) {
    val selectedOption by remember {
        derivedStateOf {
            answers.value.getOrNull(index) ?: Answer.None
        }
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 5.dp)
                .height(30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "No. ${index + 1}", style = TextStyle(fontSize = 16.sp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alpha(alpha = 0.8f)
            ) {
                if (modes == 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(color = MaterialTheme.colorScheme.primary)
                            .padding(vertical = 3.dp, horizontal = 7.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "info icon",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )

                        Text(
                            stringResource(R.string.worksheet_choose_more_than_one),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            ),

                            )
                    }
                }
                if (selectedOption !is Answer.None) {
                    OutlinedIconButton(
                        onClick = {
                            answers.value = answers.value.toMutableList().apply {
                                removeAt(index)
                                add(index, Answer.None)
                            }
                            onChanged.invoke()
                        },
                        shape = MaterialTheme.shapes.small,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        border = BorderStroke(width = Dp.Hairline, Color.Transparent),
                        modifier = Modifier
                            .padding(all = 2.dp)
                            .size(22.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Clear,
                            contentDescription = null,
                            Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (ops in 0..<option) {
                var isSelected = false
                if (selectedOption is Answer.Single && (selectedOption as Answer.Single).answer == ops)
                    isSelected = true
                if (selectedOption is Answer.Multiple && ops in (selectedOption as Answer.Multiple).answers)
                    isSelected = true

                val colors = if (isSelected) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
                val border = if (isSelected) {
                    null
                } else {
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface
                            .copy(alpha = .3f)
                    )
                }

                Button(
                    onClick = {
                        answers.value = answers.value.toMutableList().apply {
                            if (modes == 1) {
                                val answersVal = answers.value[index]
                                if (answersVal is Answer.None) {
                                    removeAt(index)
                                    add(index, Answer.Multiple(listOf(ops)))
                                } else if (answersVal is Answer.Multiple) {
                                    val oldAnswer = answersVal.answers
                                    if (ops in oldAnswer) {
                                        if (oldAnswer.size == 1) return@apply
                                        removeAt(index)
                                        add(index, Answer.Multiple(oldAnswer - ops))
                                        return@apply
                                    }
                                    removeAt(index)
                                    val newAnswer = oldAnswer + listOf(ops)
                                    add(index, Answer.Multiple(newAnswer.sorted()))
                                }
                            }
                            if (modes == 0) {
                                removeAt(index)
                                add(index, Answer.Single(ops))
                            }
                        }
                        onChanged.invoke()
                        Log.i("Prodigi.OptionsCard", "answers: $answers")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = colors,
                    border = border
                ) {
                    val textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = if (!isSelected) {
                            MaterialTheme.colorScheme.onBackground
                        } else
                            MaterialTheme.colorScheme.onPrimary
                    )

                    if (option == 2) {
                        Text(text = optionTFLabel[ops], style = textStyle)
                    } else {
                        Text(text = optionsLabel[ops], style = textStyle)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OptionsCardPreview() {
    // TODO: Update to `Answer` class
    val answers = remember {
        mutableStateOf(
            listOf(
                Answer.Single(0),
                Answer.Single(1),
                Answer.Multiple(listOf(1, 3)),
            )
        )
    }
    OptionsCard(
        answers = answers,
        option = 5,
        index = 2,
        modes = 1,
    )
}