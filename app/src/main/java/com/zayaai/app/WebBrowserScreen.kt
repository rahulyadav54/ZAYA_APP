package com.zayaai.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple
import com.zayaai.app.ui.theme.TextPrimary

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebBrowserScreen(
    url: String = "https://www.zayacodehub.online/", // Changed default to https
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    showHeader: Boolean = true
) {
    var currentUrl by remember { mutableStateOf(url) }
    var addressBarValue by remember { mutableStateOf(TextFieldValue(url)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    BackHandler(enabled = canGoBack) { // Handle back navigation within WebView
        webViewRef?.goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F0F1A) else MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for ZAYA EDU (Integrated Controls) - optional to avoid duplicating app bar
            if (showHeader) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = if (isDark) Color(0xFF1A1A2E).copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title and Controls Group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Optional menu to open the app drawer when web header is shown standalone
                            IconButton(
                                onClick = { /* placeholder for optional drawer opener */ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                            }

                            IconButton(
                                onClick = { webViewRef?.goBack() },
                                enabled = canGoBack,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (canGoBack) NeonCyan else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = "ZAYA EDU",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimary
                            )
                        }

                        // Status and Refresh Group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            IconButton(
                                onClick = { webViewRef?.reload() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh",
                                    tint = NeonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // WebView
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                var isOffline by remember { mutableStateOf(false) }
                AndroidView(
                    factory = { factoryContext: Context -> // Explicitly typed context
                        WebView(factoryContext).apply {
                            webViewRef = this // Assign webViewRef here
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36 ZayaApp/1.0"
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                displayZoomControls = false
                                setSupportZoom(true)
                                builtInZoomControls = true
                            }
                            
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            // Correctly pass the WebView instance
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                cookieManager.setAcceptThirdPartyCookies(this, true)
                            }
                            
                            webChromeClient = WebChromeClient()
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    errorMessage = null
                                    if (url != null) {
                                        currentUrl = url
                                        addressBarValue = TextFieldValue(url) // Update TextFieldValue here
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                    // Ensure cookies are saved after page load
                                    CookieManager.getInstance().flush()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        errorMessage = error?.description?.toString() ?: "Unknown error"
                                        isOffline = !isNetworkAvailable(context) // Use context from LocalContext.current
                                    }
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() // Ensure url is a String
                                    if (url != null) {
                                        if (url.startsWith("http://")) {
                                            val httpsUrl = "https://" + url.substringAfter("http://")
                                            view?.loadUrl(httpsUrl)
                                            return true // Indicate that we handled the URL loading
                                        } else if (url.startsWith("https://")) {
                                            // Already HTTPS, let WebView handle it
                                            return false
                                        }
                                    }
                                    return false // Let WebView handle it normally for other schemes or null url
                                }
                            }
                            
                            if (isNetworkAvailable(context)) { // Use context from LocalContext.current
                                loadUrl(currentUrl)
                                isOffline = false
                            } else {
                                isOffline = true
                            }
                        }
                    },
                    update = { webView ->
                        // WebView management
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Error Message Overlay
                if (!isLoading && errorMessage != null && !isOffline) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Error Loading Page",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                errorMessage ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    errorMessage = null
                                    if (isNetworkAvailable(context)) { // Use context from LocalContext.current
                                        webViewRef?.reload()
                                        isOffline = false
                                    } else {
                                        isOffline = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                // No Internet Overlay
                if (isOffline) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No Internet Connection",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Please check your connection and try again.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (isNetworkAvailable(context)) { // Use context from LocalContext.current
                                        errorMessage = null
                                        webViewRef?.reload()
                                        isOffline = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
