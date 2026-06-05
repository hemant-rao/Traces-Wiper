package com.example.wipe

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppTexts
import com.example.ui.theme.*

@Composable
fun DeepWipeScreen(vm: DeepWipeViewModel, isSystemBusy: Boolean = false) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val showWarningNote by vm.showWarning.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deep Overwrite Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TerminalCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WIPE FREE SPACE",
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = AppTexts.DEEP_WIPE_SUBTITLE,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // What this does panel (Cyber Slate Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = AppTexts.DEEP_WIPE_HOW_IT_WORKS_TITLE,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "• ${AppTexts.DEEP_WIPE_GUIDELINE_1}\n" +
                           "• ${AppTexts.DEEP_WIPE_GUIDELINE_2}\n" +
                           "• ${AppTexts.DEEP_WIPE_GUIDELINE_3}",
                    color = TextPrimary.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Warning banner
        if (showWarningNote) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElectricAmber.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 36.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ElectricAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppTexts.DEEP_WIPE_CHARGE_HEADER,
                                color = ElectricAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppTexts.DEEP_WIPE_CHARGE_WARNING,
                                color = TextPrimary.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = { vm.dismissWarning() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Power Warning",
                            tint = ElectricAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Active State Display Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (val s = state) {
                    is DeepWipeViewModel.WipeState.Idle -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(TerminalCyan.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.5.dp, TerminalCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = TerminalCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SYSTEM READY",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Tap the button below to start cleaning empty space",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = vm::startFreeSpaceWipe,
                            enabled = !isSystemBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = "START EMPTY SPACE CLEANING",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    is DeepWipeViewModel.WipeState.Running -> {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { s.fraction },
                                color = ElectricAmber,
                                trackColor = SlateBorder,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(80.dp),
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${(s.fraction * 100).toInt()}%",
                                color = ElectricAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CLEANING EMPTY STORAGE SPACE...",
                                color = ElectricAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${s.mbWritten} MB cleaned so far",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { s.fraction },
                                color = ElectricAmber,
                                trackColor = SlateBorder,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            OutlinedButton(
                                onClick = vm::cancel,
                                border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LaserRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(38.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = AppTexts.ABORT_ACTION,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    is DeepWipeViewModel.WipeState.Done -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.5.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CLEAN COMPLETED BENEFICIALLY",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Successfully cleaned ${s.mbWritten} MB of empty space.",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = vm::startFreeSpaceWipe,
                            enabled = !isSystemBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = "RUN ERASE CYCLE AGAIN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    is DeepWipeViewModel.WipeState.Cancelled -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(ElectricAmber.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.5.dp, ElectricAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ElectricAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CLEANING STOPPED",
                                color = ElectricAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Cleaning process was canceled. Empty storage remains unchanged.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = vm::startFreeSpaceWipe,
                            enabled = !isSystemBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = AppTexts.START_OVERWRITE,
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    is DeepWipeViewModel.WipeState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(LaserRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.5.dp, LaserRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LaserRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "WIPE SUSPENDED (ERROR)",
                                color = LaserRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = s.message,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        Button(
                            onClick = vm::startFreeSpaceWipe,
                            enabled = !isSystemBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = "RETRY SECTOR SCRUB",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
