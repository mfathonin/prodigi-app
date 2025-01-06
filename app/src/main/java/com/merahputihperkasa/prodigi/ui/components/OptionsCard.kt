package com.merahputihperkasa.prodigi.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merahputihperkasa.prodigi.R

val optionTFLabel = listOf("True", "False")
val optionsLabel = listOf("A", "B", "C", "D", "E")

@Composable
fun OptionsCard(answers: MutableState<List<Int>>, option: Int, index: Int, modifier: Modifier = Modifier) {
    val selectedOption by remember { derivedStateOf { answers.value[index] } }

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

            Button(
                onClick = {
                    answers.value = answers.value.toMutableList().apply {
                        removeAt(index)
                        add(index, -1)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(),
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp),
            ) {
                Text(text = stringResource(R.string.worksheet_clear_answer), style = TextStyle(fontSize = 20.sp))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (ops in 0..<option) {
                val isSelected = selectedOption == ops
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
                        MaterialTheme.colorScheme.onSurfaceVariant
                            .copy(alpha = .1f)
                    )
                }

                Button(
                    onClick = {
                        answers.value = answers.value.toMutableList().apply {
                            removeAt(index)
                            add(index, ops)
                        }
                        Log.i("Prodigi.OptionsCard", "answers: $answers")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors =  colors,
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
    val answers = remember { mutableStateOf(listOf(0,1,2,3,4)) }
    OptionsCard(
        answers = answers,
        option = 5,
        index = 2,
    )
}