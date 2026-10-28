package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ScriptPresets
import com.example.data.model.VoiceEmotion
import com.example.data.model.VoicePersona
import com.example.ui.components.AudioPlayerCard
import com.example.ui.components.ElevenVoiceSliders
import com.example.ui.components.VoiceAvatar
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.ElevenVoiceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    viewModel: ElevenVoiceViewModel,
    onNavigateToVoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val scriptText by viewModel.scriptText.collectAsState()
    val quotaRemaining by viewModel.quotaRemaining.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val lastGeneratedClip by viewModel.lastGeneratedClip.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()
    val liveAmplitudes by viewModel.liveAmplitudes.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var areSlidersExpanded by remember { mutableStateOf(false) }
    var showVoiceCloningModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Hero Header with Quota Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ElevenVoice",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STUDIO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "Generative AI Voice Synthesizer",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }

            // Quota Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .clickable { viewModel.resetQuota() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("quota_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Quota",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$quotaRemaining",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " chars",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Voice Card Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Slate850)
                .border(1.dp, Slate700, RoundedCornerShape(18.dp))
                .clickable { onNavigateToVoices() }
                .padding(14.dp)
                .testTag("selected_voice_header_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceAvatar(
                    voice = selectedVoice,
                    size = 50.dp,
                    isPlaying = isPreviewPlaying == selectedVoice.id
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedVoice.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate800)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = selectedVoice.accent,
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = selectedVoice.toneTag,
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.previewVoice(selectedVoice) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .testTag("studio_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Preview voice sample",
                        tint = if (isPreviewPlaying == selectedVoice.id) NeonCyan else Slate200,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        // Instant Voice Clone Action Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F1E36), Color(0xFF1E1336))
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.7f), ElectricViolet.copy(alpha = 0.7f))),
                    RoundedCornerShape(16.dp)
                )
                .clickable { showVoiceCloningModal = true }
                .padding(12.dp)
                .testTag("clone_my_voice_studio_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, ElectricViolet))
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Clone My Voice",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "MY VOICE",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Record 5s sample to generate speech with your voice",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { showVoiceCloningModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate800,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Clone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Emotion / Tone Mode Selector Chips
        Text(
            text = "Emotion & Delivery Style",
            color = Slate200,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VoiceEmotion.values().forEach { emotion ->
                val isSelected = voiceSettings.emotion == emotion
                val chipBg = if (isSelected) ElectricViolet else Slate850
                val chipBorder = if (isSelected) ElectricVioletLight else Slate700

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.updateEmotion(emotion) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("emotion_${emotion.name}")
                ) {
                    Text(
                        text = "${emotion.emoji} ${emotion.label}",
                        color = if (isSelected) Color.White else Slate200,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Script Prompt Input Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Slate850)
                .border(1.dp, Slate700, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Script",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()
                            if (!clip.isNullOrBlank()) {
                                viewModel.setScriptText(clip.text)
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste text",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setScriptText("") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear script",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = scriptText,
                onValueChange = { viewModel.setScriptText(it) },
                placeholder = {
                    Text(
                        text = "Type or paste your text here (English, Roman Urdu, etc.)...\n(e.g. 'Welcome to my podcast. Today we explore AI voice cloning.')",
                        color = Slate700,
                        fontSize = 13.sp
                    )
                },
                minLines = 4,
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_input_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${scriptText.length} / 2500 characters",
                    color = if (scriptText.length > 2200) Color(0xFFF43F5E) else Slate400,
                    fontSize = 11.sp
                )

                val wordCount = scriptText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                Text(
                    text = "$wordCount words",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Starter Script Chips
        Text(
            text = "Try Sample Prompts:",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ScriptPresets.samples.forEach { sample ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .clickable { viewModel.applySampleScript(sample) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = sample.title,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Fine-Tuning Collapsible Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate850)
                .clickable { areSlidersExpanded = !areSlidersExpanded }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice Settings (Stability, Clarity & Speed)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = if (areSlidersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle sliders",
                tint = Slate400
            )
        }

        AnimatedVisibility(
            visible = areSlidersExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                ElevenVoiceSliders(
                    settings = voiceSettings,
                    onStabilityChange = { viewModel.updateStability(it) },
                    onSimilarityChange = { viewModel.updateSimilarity(it) },
                    onStyleChange = { viewModel.updateStyleExaggeration(it) },
                    onSpeedChange = { viewModel.updateSpeed(it) },
                    onPitchChange = { viewModel.updatePitch(it) },
                    onReset = { viewModel.resetSettingsToDefault() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Action: Generate Speech Button
        Button(
            onClick = { viewModel.generateSpeech() },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Slate800
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (!isGenerating) {
                        Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
                    } else {
                        Brush.horizontalGradient(listOf(Slate800, Slate800))
                    }
                )
                .testTag("generate_speech_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Synthesizing AI Audio...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate AI Voice",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Error message banner if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.clearError() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Dismiss error",
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Active Player Card for Newly Synthesized Audio
        lastGeneratedClip?.let { clip ->
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Generated Audio Output",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AudioPlayerCard(
                clip = clip,
                playbackState = playbackState,
                liveAmplitudes = liveAmplitudes,
                onPlayPause = { viewModel.playAudioFile(clip.filePath) },
                onSeek = { viewModel.seekTo(it) },
                onShare = { viewModel.shareClip(clip) },
                onToggleFavorite = { viewModel.toggleFavorite(clip) },
                onSpeedChange = { viewModel.audioEngine.setSpeed(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showVoiceCloningModal) {
        com.example.ui.components.VoiceCloningModal(
            onDismiss = { showVoiceCloningModal = false },
            onVoiceCloned = { name, gender, tone, pitch, speed, audioPath ->
                viewModel.cloneMyVoice(name, gender, tone, pitch, speed, audioPath)
            }
        )
    }
}
