package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.Translations
import com.example.ui.viewmodel.EcommerceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SouqnaApp(viewModel: EcommerceViewModel) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val language by viewModel.activeLanguage.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val activeCurrencyCode by viewModel.currencyCode.collectAsState()

    // Determine layout direction based on language
    val layoutDirection = if (language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalLayoutDirection provides layoutDirection
    ) {
        val backgroundGradient = Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1C1E32),
                    Color(0xFF1B0B2E)
                )
            } else {
                listOf(
                    Color(0xFFBACBE9),
                    Color(0xFFE8EDF2),
                    Color(0xFFE3D2F7)
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundGradient)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Elegant custom geometric icon drawn on canvas
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .drawBehind {
                                            drawCircle(
                                                color = Color(0xFFD97706),
                                                radius = size.minDimension / 4,
                                                center = Offset(size.width / 2, size.height / 2)
                                            )
                                            drawRect(
                                                color = Color(0xFF0F766E),
                                                topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                                size = Size(size.width * 0.2f, size.height * 0.2f)
                                            )
                                        }
                                )
                                Column {
                                    Text(
                                        text = Translations.get("app_title", language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = Translations.get("tagline", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        actions = {
                            // Language Selector
                            IconButton(
                                onClick = { viewModel.toggleLanguage() },
                                modifier = Modifier.testTag("language_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Toggle Language",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Light/Dark Theme selector
                            IconButton(
                                onClick = { viewModel.isDarkTheme.value = !isDark },
                                modifier = Modifier.testTag("theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Dark Mode",
                                    tint = MaterialTheme.colorScheme.primaryColorOpt()
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.40f)
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                // Roles Workspace Switcher Panel
                RoleSwitchPanel(
                    currentRole = role,
                    language = language,
                    onRoleSelected = { viewModel.setRole(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Render dynamic view depending on active role
                AnimatedContent(
                    targetState = role,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "RoleViewSwap"
                ) { targetRole ->
                    when (targetRole) {
                        UserRole.CUSTOMER -> CustomerView(viewModel, language, activeCurrencyCode)
                        UserRole.MERCHANT -> MerchantView(viewModel, language)
                        UserRole.ADMIN -> AdminView(viewModel, language)
                    }
                }
            }
        }
        }

        // Voice Search Dialogue overlay
        val voiceOn by viewModel.voiceSearchActive.collectAsState()
        val voiceLabel by viewModel.voiceDialogLabel.collectAsState()
        if (voiceOn) {
            VoiceSearchDialog(voiceLabel = voiceLabel, language = language) {
                viewModel.voiceSearchActive.value = false
            }
        }

        // Invoice Receipt overlay
        val activeOrderForReceipt by viewModel.printedOrder.collectAsState()
        if (activeOrderForReceipt != null) {
            InvoiceReceiptDialog(
                order = activeOrderForReceipt!!,
                language = language,
                onDismiss = { viewModel.printedOrder.value = null }
            )
        }
    }
}

// Extension to colorize icons safely
@Composable
fun ColorScheme.primaryColorOpt() = this.primary

// -----------------------------------------------------------------------------
// COMPONENT: Role Switching panel
// -----------------------------------------------------------------------------
@Composable
fun RoleSwitchPanel(
    currentRole: UserRole,
    language: AppLanguage,
    onRoleSelected: (UserRole) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Translations.get("role_select", language),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val roles = listOf(
                    Triple(UserRole.CUSTOMER, Icons.Default.ShoppingCart, Translations.get("customer", language).take(12)),
                    Triple(UserRole.MERCHANT, Icons.Default.Store, Translations.get("merchant", language).take(12)),
                    Triple(UserRole.ADMIN, Icons.Default.Shield, Translations.get("admin", language).take(12))
                )

                roles.forEach { (urole, icon, label) ->
                    val isSelected = currentRole == urole
                    Button(
                        onClick = { onRoleSelected(urole) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_btn_${urole.name.lowercase()}"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
                            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// USER INTERFACE: customer (Shopper) Screen
// -----------------------------------------------------------------------------
@Composable
fun CustomerView(
    viewModel: EcommerceViewModel,
    language: AppLanguage,
    activeCurrency: String
) {
    val query by viewModel.searchQuery.collectAsState()
    val products by viewModel.displayedProducts.collectAsState()
    val customer by viewModel.customerProfile.collectAsState(initial = null)
    val cart by viewModel.cartItems.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()
    val rates by viewModel.currencyRates.collectAsState()
    val showCartState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        // Customer loyalty and budget metrics
        customer?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), Translations.get("loyalty_msg", language), it.loyaltyPoints),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), Translations.get("balance_label", language), String.format("%,.0f", it.balanceSyp)),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Active Alerts & Discounts ticker
        Text(
            text = "📢 " + (if (language == AppLanguage.AR) "عروض اليوم الحصرية والتنبيهات:" else "Exclusive Flash Alerts:"),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(notificationsList.reversed()) { alert ->
                Card(
                    modifier = Modifier.width(280.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Alert", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = alert,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Ads Billboard Hero Card7
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Drawing modern aesthetic grid-lines reflecting Syrian carpet geometry
                        val stepX = size.width / 10
                        val stepY = size.height / 5
                        for (i in 1..10) {
                            drawLine(
                                color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                                start = Offset(i * stepX, 0f),
                                end = Offset(i * stepX, size.height)
                            )
                        }
                        for (i in 1..5) {
                            drawLine(
                                color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                                start = Offset(0f, i * stepY),
                                end = Offset(size.width, i * stepY)
                            )
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.AR) "مهرجان سوقنا لمكافآت الولاء!" else "Souqna Artisan Loyalty Feast!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (language == AppLanguage.AR) "اجمع النقاط مع كل ليرة سورية تنفقها واستبدلها بخصومات حقيقية" else "Collect points on every Syrian Pound spent and redeem instant cash savings",
                            color = Color.White.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEAB308), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = "Points Medal", tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar & Filters Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("product_search_input"),
                placeholder = { Text(Translations.get("search_hint", language), fontSize = 13.sp) },
                prefix = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            // Voice Search Mic Input Trigger
            IconButton(
                onClick = { viewModel.triggerVoiceSearch() },
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("voice_search_mic")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search Mode",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Advanced Filter Controls: Price capping slider and min rating
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                val maxP by viewModel.priceFilterMax.collectAsState()
                val minR by viewModel.ratingFilterMin.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == AppLanguage.AR) "فلترة السعر الأقصى: ${String.format("%,.0f ل.س", maxP)}" else "Max Price: ${String.format("%,.0f SYP", maxP)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.AR) "التقييم الأدنى: ${minR.toInt()} ⭐️" else "Min Rating: ${minR.toInt()} ⭐️",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = maxP.toFloat(),
                    onValueChange = { viewModel.priceFilterMax.value = it.toDouble() },
                    valueRange = 10000f..200000f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0f, 3f, 4f, 5f).forEach { score ->
                        val active = minR == score
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.ratingFilterMin.value = score },
                            label = { Text(if (score == 0f) (if (language == AppLanguage.AR) "الكل" else "All") else "$score ⭐️+") }
                        )
                    }
                }
            }
        }

        // Pricing Global Currency Selectors (with SYP, USD, EUR calculations)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Translations.get("currency_select", language),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = Translations.get("default_currency_info", language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rates.forEach { rate ->
                val active = activeCurrency == rate.code
                Button(
                    onClick = { viewModel.setCurrency(rate.code) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("curr_select_${rate.code.lowercase()}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(text = "${rate.code} (${rate.symbol})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Syrian Crafts Product Carousel
        products.forEach { product ->
            ProductCardItem(product = product, viewModel = viewModel, language = language)
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.AR) "لم يتم العثور على أي منتج يطابق الفلترة والمصطلح الصوتي." else "No Syrian heritage products match your search or filters.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open live interactive AI Chat Support
        CustomerSupportChatPane(viewModel = viewModel, language = language)

        Spacer(modifier = Modifier.height(16.dp))

        // Historic customer orders log / shipping updates
        CustomerOrdersListPane(viewModel = viewModel, language = language)

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Interactive Custom FAB showing floating cart sheet trigger
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val totalItems = cart.sumOf { it.second }
        FloatingActionButton(
            onClick = { showCartState.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .testTag("floating_cart_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            BadgedBox(
                badge = {
                    if (totalItems > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(text = totalItems.toString())
                        }
                    }
                }
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Open Cart Basket")
            }
        }
    }

    // Slide Sheet Shopping Cart Dialogue Overlay
    if (showCartState.value) {
        CartBottomSheetDialog(
            viewModel = viewModel,
            cart = cart,
            language = language,
            activeCurrency = activeCurrency,
            onDismiss = { showCartState.value = false }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Product visual Card
// -----------------------------------------------------------------------------
@Composable
fun ProductCardItem(
    product: Product,
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    var expandedSpecs by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    val commentsState = viewModel.observeReviews(product.id).collectAsState(initial = emptyList<ProductReview>())
    val comments = commentsState.value
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Picture of the product
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.localUri != null) {
                        AsyncImage(
                            model = Uri.parse(product.localUri),
                            contentDescription = product.titleAr,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Drawing elegant geometric Damascus jasmine symbols inside back-panel as vector graphic placeholder
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF0F766E).copy(alpha = 0.25f),
                                radius = size.minDimension / 3.5f
                            )
                            drawRect(
                                color = Color(0xFFD97706).copy(alpha = 0.15f),
                                topLeft = Offset(size.width * 0.35f, size.height * 0.35f),
                                size = Size(size.width * 0.3f, size.height * 0.3f)
                            )
                        }
                        Text(
                            text = if (product.imageResName != null) "📦" else "🌺",
                            fontSize = 32.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.AR) product.titleAr else product.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (language == AppLanguage.AR) product.categoryAr else product.categoryEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.AR) product.descriptionAr else product.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing and checkout indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = viewModel.convertPrice(product.priceInSyp),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    // Display original base SYP inside small labels if active currency is not SYP
                    val activeCurr by viewModel.currencyCode.collectAsState()
                    if (activeCurr != "SYP") {
                        Text(
                            text = "Original: ${String.format("%,.0f", product.priceInSyp)} ل.س",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rating score display
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating Star", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", product.averageRating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " (${product.ratingCount})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Stock indicators
                    if (product.stockCount <= 0) {
                        Text(
                            text = if (language == AppLanguage.AR) "نفذت الكمية!" else "Out of Stock!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = {
                                viewModel.addToCart(product.id)
                                Toast.makeText(context, Translations.get("added_to_cart_toast", language), Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_cart_btn_${product.id}")
                        ) {
                            Text(text = if (language == AppLanguage.AR) "إضافة للسلة" else "+ Cart", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Expandable details (Specifications & Custom Reviews)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { expandedSpecs = !expandedSpecs },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expandedSpecs) (if (language == AppLanguage.AR) "إخفاء التفاصيل ⬆" else "Hide details ⬆")
                        else (if (language == AppLanguage.AR) "تفاصيل وآراء العملاء ⬇" else "Details & Reviews ⬇"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (expandedSpecs) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                // Specs
                Text(
                    text = Translations.get("product_specs_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = if (language == AppLanguage.AR) product.specsAr.ifBlank { "مواصفات حرفية تقليدية معتمدة." } else product.specsEn.ifBlank { "Traditional handcrafted standard specifications." },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Share product action
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Check this Syrian Craft")
                            putExtra(Intent.EXTRA_TEXT, "شاهد هذا المنتج الممتاز على منصة سوقنا: ${product.titleAr} بسعر ${product.priceInSyp} ل.س!\nLet's shop Syrian heritage.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share product"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Text(text = if (language == AppLanguage.AR) "مشاركة هذا المنتج" else "Share Product Link", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer reviews list
                Text(
                    text = Translations.get("recent_reviews", language) + " (${comments.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in comments) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = r.reviewerName, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Row {
                                        for (star in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "*",
                                                tint = if (star <= r.rating) Color(0xFFFBBF24) else Color.LightGray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = r.comment, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (comments.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.AR) "لا يوجد مراجعات بعد لهذا المنتج الشامي. اكتب رأيك لتكون أولهم!" else "No review for this item yet. Be the first to review!",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Add Review block
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Translations.get("write_review", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        placeholder = { Text(if (language == AppLanguage.AR) "أضف بضع كلمات..." else "Write review text...", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    
                    // Star count swapper
                    Row(
                        modifier = Modifier.clickable {
                            reviewRating = if (reviewRating == 5) 1 else reviewRating + 1
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "$reviewRating⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (reviewText.isNotBlank()) {
                                viewModel.submitProductReview(product.id, "عميل سوقنا الشام", reviewRating, reviewText)
                                reviewText = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = Translations.get("send_btn", language), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Shopping Cart overlay dialog
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheetDialog(
    viewModel: EcommerceViewModel,
    cart: List<Pair<Product, Int>>,
    language: AppLanguage,
    activeCurrency: String,
    onDismiss: () -> Unit
) {
    var selectedGateway by remember { mutableStateOf("Syriatel Cash") }
    val customer by viewModel.customerProfile.collectAsState(initial = null)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("cart_sheet_container"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.get("cart_title", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Cart")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Cart list scrollable block
                Box(modifier = Modifier.weight(1f)) {
                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Translations.get("empty_cart", language),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cart) { (product, qty) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (language == AppLanguage.AR) product.titleAr else product.titleEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = viewModel.convertPrice(product.priceInSyp * qty),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Item counter adjusting buttons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateCartQty(product.id, qty - 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(16.dp))
                                        }
                                        Text(text = qty.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = { viewModel.updateCartQty(product.id, qty + 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (cart.isNotEmpty()) {
                    // Calculation breakdown
                    val totalSyp = cart.sumOf { it.first.priceInSyp * it.second }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == AppLanguage.AR) "المبلغ الإجمالي بالعملة المحددة:" else "Total Cost Converted:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.convertPrice(totalSyp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gateway configuration selector
                    Text(
                        text = if (language == AppLanguage.AR) "اختر بوابة الدفع الإلكتروني الآمنة:" else "Secure Payment Gateways:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val gateways = listOf(
                            "Credit Card" to Translations.get("pay_credit", language).take(12),
                            "Syriatel Cash" to Translations.get("pay_mobile_cash", language).take(12),
                            "Cash on Delivery" to Translations.get("pay_cod", language).take(12)
                        )
                        gateways.forEach { (type, label) ->
                            val gateActive = selectedGateway == type
                            OutlinedButton(
                                onClick = { selectedGateway = type },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (gateActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (gateActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkout Button
                    Button(
                        onClick = {
                            viewModel.performCheckout(selectedGateway)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Checkout", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Translations.get("checkout_btn", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Voice Search dialogue
// -----------------------------------------------------------------------------
@Composable
fun VoiceSearchDialog(
    voiceLabel: String,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic Indicator",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = voiceLabel,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.get("voice_search_unsupported", language),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Customer support Chat Pane
// -----------------------------------------------------------------------------
@Composable
fun CustomerSupportChatPane(
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    var rawText by remember { mutableStateOf("") }
    val messages by viewModel.supportMessages.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = Translations.get("chat_support_title", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = Translations.get("chat_support_desc", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat message scroll box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(6.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(messages) { msg ->
                        val isUser = msg.senderRole == "USER"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp,
                                            bottomStart = if (isUser) 8.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 8.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(Translations.get("input_msg_hint", language), fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            viewModel.sendChatMessage(rawText)
                            rawText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Text(text = Translations.get("send_btn", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Customer orders & physical tracking logs
// -----------------------------------------------------------------------------
@Composable
fun CustomerOrdersListPane(
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    val orders by viewModel.customerOrders.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = "Shipping", tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = Translations.get("order_tracking", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (orders.isEmpty()) {
                Text(
                    text = Translations.get("empty_logs", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    orders.forEach { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ID: #SQ-${order.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = order.status,
                                        color = when (order.status) {
                                            "DELIVERED" -> Color(0xFF10B981)
                                            "DISPATCHED" -> Color(0xFFFBBF24)
                                            "REFUNDED" -> Color(0xFFEF4444)
                                            else -> Color(0xFF3B82F6)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "Items: ${order.invoiceDetails}",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "Courier: ${order.deliveryStaffName} | Gateway: ${order.paymentMethod}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Physical live coordinates step tracker
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍 Lat: ${String.format(Locale.getDefault(), "%.4f", order.trackingLatitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", order.trackingLongitude)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = when(order.status) {
                                            "PENDING" -> Translations.get("step_pending", language)
                                            "DISPATCHED" -> Translations.get("step_delivering", language)
                                            "DELIVERED" -> Translations.get("step_delivered", language)
                                            "REFUNDED" -> (if (language == AppLanguage.AR) "تمت عملية استرداد النقود بنجاح" else "Refund returned to default currency.")
                                            else -> order.status
                                        },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Trigger Print Invoice
                                    OutlinedButton(
                                        onClick = { viewModel.printedOrder.value = order },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(Translations.get("print_invoice", language), fontSize = 9.sp)
                                    }

                                    // Trigger Instant Refund if completed
                                    if (order.status != "REFUNDED" && order.status != "PENDING") {
                                        Button(
                                            onClick = { viewModel.requestRefund(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(Translations.get("refund_btn", language), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// USER INTERFACE: Merchant Screen (Abu Ahmad Dashboard)
// -----------------------------------------------------------------------------
@Composable
fun MerchantView(
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    val storeActive = remember { mutableStateOf(true) }
    val productsState = viewModel.observeProductsByMerchant("merchant_1").collectAsState(initial = emptyList<Product>())
    val products = productsState.value
    val orders by viewModel.merchantOrders.collectAsState()
    val totalRevenueSyp = orders.sumOf { it.totalAmountInSyp }

    // New Product form fields
    var titleEn by remember { mutableStateOf("") }
    var titleAr by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var descAr by remember { mutableStateOf("") }
    var specsEn by remember { mutableStateOf("") }
    var specsAr by remember { mutableStateOf("") }
    var priceSypText by remember { mutableStateOf("") }
    var stockCountText by remember { mutableStateOf("") }
    var categoryEn by remember { mutableStateOf("Heritage Crafts") }
    var categoryAr by remember { mutableStateOf("صناعات يدوية تراثية") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Media picking launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            Toast.makeText(context, if (language == AppLanguage.AR) "تم اختيار الصورة من ذاكرة الهاتف!" else "Image selected successfully from storage!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        // Merchant subscription control status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (storeActive.value) Color(0xFF0F766E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (storeActive.value) (if (language == AppLanguage.AR) "حساب المتجر نشط (أبو أحمد)" else "Merchant Status Active (Abu Ahmad)")
                        else Translations.get("unsubscribed_warning", language),
                        fontWeight = FontWeight.Bold,
                        color = if (storeActive.value) Color(0xFF0F766E) else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (language == AppLanguage.AR) "الاشتراك الشهري مدفوع ومرخص بالكامل" else "Platform dues are current & fully valid.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Switch(
                    checked = storeActive.value,
                    onCheckedChange = { storeActive.value = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analytics metrics block
        Text(
            text = Translations.get("sales_analytics", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = Translations.get("total_revenue", language), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f ل.س", totalRevenueSyp),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "USD Equiv", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = "$ ${String.format(Locale.getDefault(), "%,.1f", totalRevenueSyp / 15000.0)}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Visual Monthly Sales Growth Chart Canvas (Renders sales charts directly on canvas)
        Text(
            text = if (language == AppLanguage.AR) "رسم بياني مبيعات المخزن 2026 (بالمليون ل.س):" else "Merchant Sales Progress 2026 (Million SYP):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            val chartColor = MaterialTheme.colorScheme.primary
            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            val barLabels = if (language == AppLanguage.AR) listOf("أيار", "حزيران", "تموز", "آب") else listOf("May", "Jun", "Jul", "Aug")
            val barValues = listOf(0.4f, 0.75f, 0.6f, 0.9f) // simulated heights

            Canvas(modifier = Modifier.fillMaxSize()) {
                val spaceBetween = size.width / (barValues.size)
                val barWidth = 40.dp.toPx()
                val maxH = size.height * 0.8f

                for (i in barValues.indices) {
                    val x = i * spaceBetween + (spaceBetween - barWidth) / 2
                    val h = barValues[i] * maxH
                    val y = size.height - h - 15.dp.toPx()

                    // Draw colorful column bar with corner roundings
                    drawRoundRect(
                        color = chartColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                barLabels.forEach { label ->
                    Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ADD NEW PRODUCT form logic
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = Translations.get("add_item_title", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = titleAr,
                    onValueChange = { titleAr = it },
                    placeholder = { Text(Translations.get("title_ar", language)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    placeholder = { Text(Translations.get("title_en", language)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = descAr,
                    onValueChange = { descAr = it },
                    placeholder = { Text(Translations.get("desc_ar", language)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    placeholder = { Text(Translations.get("desc_en", language)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceSypText,
                        onValueChange = { priceSypText = it },
                        placeholder = { Text(Translations.get("price_syp", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = stockCountText,
                        onValueChange = { stockCountText = it },
                        placeholder = { Text(Translations.get("stock_count", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = specsAr,
                        onValueChange = { specsAr = it },
                        placeholder = { Text("المواصفات بالعربية (وزن، منشأ)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = specsEn,
                        onValueChange = { specsEn = it },
                        placeholder = { Text("Specs (Weight, Ingredients)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Image selector Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Button(
                        onClick = { pickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Translations.get("upload_gallery", language))
                    }
                }

                if (selectedImageUri != null) {
                    Text(
                        text = "File: ${selectedImageUri!!.take(40)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        val price = priceSypText.toDoubleOrNull() ?: 15000.0
                        val stock = stockCountText.toIntOrNull() ?: 10
                        if (titleAr.isNotBlank() && titleEn.isNotBlank()) {
                            viewModel.addNewProduct(
                                titleEn = titleEn, titleAr = titleAr,
                                descEn = descEn, descAr = descAr,
                                categoryEn = categoryEn, categoryAr = categoryAr,
                                priceSyp = price, stock = stock,
                                imageUri = selectedImageUri,
                                specsEn = specsEn, specsAr = specsAr
                            )
                            // Reset fields
                            titleAr = ""
                            titleEn = ""
                            descAr = ""
                            descEn = ""
                            specsAr = ""
                            specsEn = ""
                            priceSypText = ""
                            stockCountText = ""
                            selectedImageUri = null
                            Toast.makeText(context, if (language == AppLanguage.AR) "تم حفظ ونشر المنتج بنجاح!" else "Published successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("publish_product_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Translations.get("save_product", language), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active products with custom updates
        Text(
            text = if (language == AppLanguage.AR) "إدارة المخزون والأسعار الفورية:" else "Inventory & Pricing Editor:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        for (p in products) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = if (language == AppLanguage.AR) p.titleAr else p.titleEn, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Stock: ${p.stockCount} | Price: ${p.priceInSyp} SYP", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        if (p.stockCount <= 5) {
                            Text(
                                text = Translations.get("low_stock_warning", language),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stock increment button
                        IconButton(
                            onClick = { viewModel.updateProductDetails(p.copy(stockCount = p.stockCount + 5)) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+5 Stock", modifier = Modifier.size(16.dp))
                        }

                        // Price modifier button
                        IconButton(
                            onClick = { viewModel.updateProductDetails(p.copy(priceInSyp = p.priceInSyp + 1000.0)) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "+1k price", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delivery Staff Registry UI ("إضافة موظفين توصيل")
        DeliveryStaffUI(viewModel = viewModel, language = language)

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Delivery staff roster
// -----------------------------------------------------------------------------
@Composable
fun DeliveryStaffUI(
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    var brandNewDriverName by remember { mutableStateOf("") }
    val staffRoster by viewModel.deliveryStaff.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = Translations.get("delivery_manager", language),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            LazyRow(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(staffRoster) { driver ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🛵 " + driver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = brandNewDriverName,
                    onValueChange = { brandNewDriverName = it },
                    placeholder = { Text(if (language == AppLanguage.AR) "ادخل اسم السائق..." else "Driver name...", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (brandNewDriverName.isNotBlank()) {
                            viewModel.registerDeliveryStaff(brandNewDriverName)
                            brandNewDriverName = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text(text = Translations.get("add_delivery_btn", language), fontSize = 10.sp)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// USER INTERFACE: System Administrator Screen
// -----------------------------------------------------------------------------
@Composable
fun AdminView(
    viewModel: EcommerceViewModel,
    language: AppLanguage
) {
    val admin by viewModel.adminProfile.collectAsState(initial = null)
    val rates by viewModel.currencyRates.collectAsState()
    val orders by viewModel.allOrders.collectAsState()

    var editUsdRate by remember { mutableStateOf("") }
    var editEurRate by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Text(
            text = Translations.get("commission_title", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        admin?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = Translations.get("platform_earnings", language), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f ل.س", it.balanceSyp),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = Translations.get("admin_fee_info", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Exchange Rate editor form ("اضافة اكثر من عملة مع حساب المعادل لها")
        Text(
            text = Translations.get("currency_editor", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                rates.forEach { rate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${rate.code} (${rate.symbol})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "1 ${rate.code} = ${String.format("%,.0f ل.س", rate.rateToSyp)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editUsdRate,
                    onValueChange = { editUsdRate = it },
                    placeholder = { Text("New USD Rate (e.g. 15000 SYP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = editEurRate,
                    onValueChange = { editEurRate = it },
                    placeholder = { Text("New EUR Rate (e.g. 16200 SYP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        val usd = editUsdRate.toDoubleOrNull()
                        val eur = editEurRate.toDoubleOrNull()
                        if (usd != null) viewModel.updateCurrencyRate("USD", usd)
                        if (eur != null) viewModel.updateCurrencyRate("EUR", eur)
                        editUsdRate = ""
                        editEurRate = ""
                        Toast.makeText(context, if (language == AppLanguage.AR) "تم تحديث أسعار الصرف دولياً بنجاح!" else "Currency conversion rates updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = Translations.get("update_rates_btn", language), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System logs
        Text(
            text = if (language == AppLanguage.AR) "جريدة العمليات السحابية العامة للمنصة:" else "Live Global Platform Transaction Logs:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        orders.forEach { o ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = "Ref: #SQ-${o.id} | Amt: ${o.totalAmountInSyp} SYP", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(text = "Customer: metadata.cust | Gateway: ${o.paymentMethod}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Text(
                        text = "Fee Collected: +${o.totalAmountInSyp * 0.05} SYP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            Text(
                text = if (language == AppLanguage.AR) "لا توجد أي معاملات مسجلة سحابياً بعد." else "No worldwide transactions logged yet.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: Printable invoice receipt overlay dialog
// -----------------------------------------------------------------------------
@Composable
fun InvoiceReceiptDialog(
    order: Order,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("invoice_dialog_box"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White) // Monochrome print receipt background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Header
                Text(
                    text = Translations.get("invoice_header", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "******************************************",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = String.format(Locale.getDefault(), Translations.get("invoice_id", language), order.id),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Date: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(order.date)),
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Invoice Body
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Items purchased:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Text(text = order.invoiceDetails, fontSize = 11.sp, color = Color.DarkGray)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Base SYP Total:", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "${String.format("%,.0f", order.totalAmountInSyp)} SYP", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Payment Method Used:", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = order.paymentMethod, fontSize = 11.sp, color = Color.Black)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Exchange EquivalentPaid:", fontSize = 11.sp, color = Color.DarkGray)
                        Text(
                            text = "${String.format("%.2f", order.convertedAmount)} ${order.targetCurrencyCode}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "******************************************",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Text(
                    text = Translations.get("invoice_thanks", language),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.Black
                )
                Text(text = "Platform Operator commission: 5% model.", fontSize = 9.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back/Close action
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(text = if (language == AppLanguage.AR) "رجوع" else "Close", color = Color.Black)
                    }

                    // Simulated System Print manager trigger action
                    Button(
                        onClick = {
                            try {
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                                Toast.makeText(context, if (language == AppLanguage.AR) "تم استدعاء بروتوكول الطباعة للموبايل بنجاح!" else "Printer interface invoked successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error sending to print adapter", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (language == AppLanguage.AR) "طباعة" else "Print PDF", color = Color.White)
                    }
                }
            }
        }
    }
}
