package com.zayaai.app.ads

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.zayaai.app.ui.theme.DarkBorder
import com.zayaai.app.ui.theme.LightBorder
import com.zayaai.app.ui.theme.LocalIsDarkTheme

/**
 * Reusable Compose Adaptive Banner Ad Widget.
 * Responsively renders Google AdMob banner ads with proper lifecycle management.
 */
@Composable
fun BannerAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.getBannerAdUnitId()
) {
    val isDark = LocalIsDarkTheme.current
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    if (isPreview) {
        // Compose Preview fallback
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("AdMob Banner Ad Preview", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    // If ad failed to load, don't leave empty white gap on screen
    if (isAdFailed) {
        return
    }

    val screenWidth = configuration.screenWidthDp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) Color(0xFF181829).copy(alpha = 0.9f) else Color(0xFFF8FAFC)
            )
            .border(
                1.dp,
                if (isDark) DarkBorder.copy(alpha = 0.5f) else LightBorder.copy(alpha = 0.7f),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Unobtrusive "Sponsored" attribution tag per Google Policy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "SPONSORED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)).copy(alpha = 0.7f)
                )
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdUnitId(adUnitId)
                        val adSize = getAdaptiveAdSize(ctx, screenWidth)
                        setAdSize(adSize)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                                isAdFailed = false
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                isAdLoaded = false
                                isAdFailed = true
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { view ->
                    // View update if adUnitId changes
                }
            )
        }
    }
}

/**
 * Calculates adaptive banner size based on device display width.
 */
private fun getAdaptiveAdSize(context: Context, widthDp: Int): AdSize {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    var adWidthPixels = (widthDp * density).toInt()
    if (adWidthPixels == 0) {
        adWidthPixels = displayMetrics.widthPixels
    }
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}

