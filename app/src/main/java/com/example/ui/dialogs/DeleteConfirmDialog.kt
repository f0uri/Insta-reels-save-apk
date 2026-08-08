package com.example.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources

@Composable
fun DeleteConfirmDialog(
    language: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141720),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = StringResources.getString("delete_confirm_title", language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF453A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = StringResources.getString("delete_confirm_message", language),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = StringResources.getString("cancel", language),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = StringResources.getString("delete_video", language),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

