package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.ui.locale.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EcommerceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ECommerceRepository.getInstance(application)

    // User Session Configurations
    val currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val activeLanguage = MutableStateFlow(AppLanguage.AR)
    val currencyCode = MutableStateFlow("SYP")
    val isDarkTheme = MutableStateFlow(true) // Comfortable night mode enabled by default

    // User Profiles observed from DB
    val customerProfile = repository.observeUser("customer_1")
    val merchantProfile = repository.observeUser("merchant_1")
    val adminProfile = repository.observeUser("admin_1")

    // Products Flow
    val searchQuery = MutableStateFlow("")
    val priceFilterMax = MutableStateFlow(200000.0)
    val ratingFilterMin = MutableStateFlow(0f)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val displayedProducts: StateFlow<List<Product>> = searchQuery
        .combine(priceFilterMax) { query, maxPrice -> query to maxPrice }
        .combine(ratingFilterMin) { (query, maxPrice), minRating -> Triple(query, maxPrice, minRating) }
        .flatMapLatest { (query, maxPrice, minRating) ->
            if (query.isBlank()) {
                repository.allProducts.map { list ->
                    list.filter { it.priceInSyp <= maxPrice && it.averageRating >= minRating }
                }
            } else {
                repository.productDao.searchProducts(query).map { list ->
                    list.filter { it.priceInSyp <= maxPrice && it.averageRating >= minRating }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Items
    val cartItems: StateFlow<List<Pair<Product, Int>>> = repository.observeCart("customer_1")
        .map { list ->
            list.mapNotNull { item ->
                val p = repository.getProduct(item.productId)
                if (p != null) p to item.quantity else null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Currency Conversion Rates
    val currencyRates: StateFlow<List<CurrencyRate>> = repository.currencyRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Delivery Staff Managed by Merchant
    val deliveryStaff = MutableStateFlow(listOf("سامر التوصيل السريع", "أبو غازي دمشق", "موفق الشام"))

    // Customer Alert Push Notifications List
    val notifications = MutableStateFlow(
        listOf(
            "عرض حصري: خصم 15% على صابون الغار الحلبي الممتاز اليوم بمناسبة الصيف!",
            "وصل حديثاً: زيت الورد الشامي المعصور على البارد متوفر الآن بكميات محدودة.",
            "مشترياتك آمنة: محفظة سيريتل كاش معتمدة رسمياً في منصة سوقنا للتجارة."
        )
    )

    // Invoice Print Preview Order
    val printedOrder = MutableStateFlow<Order?>(null)

    // Voice Dialogue state
    val voiceSearchActive = MutableStateFlow(false)
    val voiceDialogLabel = MutableStateFlow("انقر للتحدث بالمنتج الشامي المفضل...")

    // Active Customer Orders
    val customerOrders = repository.observeOrdersForCustomer("customer_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Merchant Orders
    val merchantOrders = repository.observeOrdersForMerchant("merchant_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Unified Platform Orders
    val allOrders = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Support Chat State
    val supportMessages = repository.observeChatMessages("support")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
    }

    // Swapping Roles
    fun setRole(role: UserRole) {
        currentRole.value = role
    }

    fun toggleLanguage() {
        activeLanguage.value = if (activeLanguage.value == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
    }

    fun setCurrency(code: String) {
        currencyCode.value = code
    }

    fun observeReviews(productId: Int): Flow<List<ProductReview>> {
        return repository.observeReviews(productId)
    }

    fun observeProductsByMerchant(merchantId: String): Flow<List<Product>> {
        return repository.observeProductsByMerchant(merchantId)
    }

    // Price Conversion calculation
    fun convertPrice(priceInSyp: Double): String {
        val activeCode = currencyCode.value
        val rates = currencyRates.value
        if (activeCode == "SYP") {
            return String.format(Locale.getDefault(), "%,.0f ل.س", priceInSyp)
        }
        val targetRate = rates.find { it.code == activeCode } ?: return String.format(Locale.getDefault(), "%,.0f ل.س", priceInSyp)
        val value = priceInSyp / targetRate.rateToSyp
        return String.format(Locale.getDefault(), "%,.2f %s", value, targetRate.symbol)
    }

    // Add To Cart
    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addProductToCart(productId, quantity, "customer_1")
        }
    }

    // Modify quantity or delete
    fun updateCartQty(productId: Int, newQty: Int) {
        viewModelScope.launch {
            val list = repository.cartDao.getCartItems("customer_1")
            val target = list.find { it.productId == productId }
            if (target != null) {
                if (newQty <= 0) {
                    repository.cartDao.deleteCartItem(target.id)
                } else {
                    repository.cartDao.updateCartQuantity(target.id, newQty)
                }
            }
        }
    }

    // Checkout Order
    fun performCheckout(paymentMethod: String) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isNotEmpty()) {
                val ord = repository.checkout("customer_1", currencyCode.value, paymentMethod, items)
                if (ord != null) {
                    printedOrder.value = ord
                    // Simulate real time delivery path tracking with a delay
                    simulateOrderShipmentTracking(ord.id)
                }
            }
        }
    }

    // Real-Time Shipment Delivery Progress Simulation
    private fun simulateOrderShipmentTracking(orderId: Int) {
        viewModelScope.launch {
            delay(1000)
            val o1 = repository.orderDao.getOrderById(orderId)
            if (o1 != null) {
                // Update to prepping
                repository.orderDao.updateOrder(o1.copy(status = "PREPARING", trackingLatitude = 33.5138, trackingLongitude = 36.2765))
            }
            delay(6000)
            val o2 = repository.orderDao.getOrderById(orderId)
            if (o2 != null) {
                // Dispatch courier
                repository.orderDao.updateOrder(o2.copy(status = "DISPATCHED", trackingLatitude = 33.5204, trackingLongitude = 36.2905))
            }
            delay(8000)
            val o3 = repository.orderDao.getOrderById(orderId)
            if (o3 != null) {
                // Delivered
                repository.orderDao.updateOrder(o3.copy(status = "DELIVERED", trackingLatitude = 33.5255, trackingLongitude = 36.3150))
                // Notify customer of arrival
                notifications.update { current ->
                    current + "تم تسليم الطلب رقم SQ-$orderId# بنجاح بواسطة ${o3.deliveryStaffName}."
                }
            }
        }
    }

    // Refund Request
    fun requestRefund(orderId: Int) {
        viewModelScope.launch {
            val order = repository.orderDao.getOrderById(orderId)
            if (order != null && order.status != "REFUNDED") {
                // Update order to Refunded
                repository.orderDao.updateOrder(order.copy(status = "REFUNDED", isRefundRequested = true))
                
                // Revert user loyalty points and update balance
                val user = repository.userDao.getUserById("customer_1")
                if (user != null) {
                    val pointsToRevert = (order.totalAmountInSyp / 5000.0).toInt()
                    val newPoints = (user.loyaltyPoints - pointsToRevert).coerceAtLeast(0)
                    val newBalance = user.balanceSyp + order.totalAmountInSyp
                    repository.userDao.updateUser(user.copy(loyaltyPoints = newPoints, balanceSyp = newBalance))
                }
            }
        }
    }

    // Voice search simulation
    fun triggerVoiceSearch() {
        voiceSearchActive.value = true
        voiceDialogLabel.value = if (activeLanguage.value == AppLanguage.AR) "جاري الاستماع لصوتك الشامي..." else "Listening for Syrian regional products..."
        viewModelScope.launch {
            delay(3000)
            // Match demo voice queries
            val searchOptions = listOf("صابون غار", "زيت زيتون", "ورد دمشقي", "معمول", "بهارات")
            val chosen = searchOptions.random()
            searchQuery.value = chosen
            voiceDialogLabel.value = if (activeLanguage.value == AppLanguage.AR) "تم العثور على: $chosen !" else "Matched search: $chosen !"
            delay(1500)
            voiceSearchActive.value = false
        }
    }

    // Submit and broadcast customer reviews
    fun submitProductReview(productId: Int, name: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val reviewerName = name.ifBlank { if (activeLanguage.value == AppLanguage.AR) "مشتري مجهول" else "Anonymous" }
            repository.submitReview(productId, reviewerName, rating, comment)
        }
    }

    // Add New Delivery Staff
    fun registerDeliveryStaff(name: String) {
        if (name.isNotBlank()) {
            deliveryStaff.update { current -> current + name }
        }
    }

    // Support Chat Messages sent by shopper
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Write User query
            val msgUser = ChatMessage(chatRoomId = "support", senderRole = "USER", text = text)
            repository.chatDao.insertMessage(msgUser)

            // Let AI respond reactively
            delay(500)
            getAiSupportResponse(text)
        }
    }

    // Interact with Google Gemini or simulate smart customer representative
    private fun getAiSupportResponse(userQuery: String) {
        viewModelScope.launch {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") {
                try {
                    // Call Generative LLM via REST
                    val botMessage = callGeminiRestApi(userQuery, key)
                    repository.chatDao.insertMessage(
                        ChatMessage(chatRoomId = "support", senderRole = "ASSISTANT", text = botMessage)
                    )
                } catch (e: Exception) {
                    val fallback = getFallbackSupportResponse(userQuery)
                    repository.chatDao.insertMessage(
                        ChatMessage(chatRoomId = "support", senderRole = "ASSISTANT", text = fallback)
                    )
                }
            } else {
                // Key not configured, use local simulated intelligent support
                delay(1200)
                val response = getFallbackSupportResponse(userQuery)
                repository.chatDao.insertMessage(
                    ChatMessage(chatRoomId = "support", senderRole = "ASSISTANT", text = response)
                )
            }
        }
    }

    private suspend fun callGeminiRestApi(userQuery: String, apiKey: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.1-flash:generateContent?key=$apiKey"

        val systemPrompt = """
            You are "Souqna", a helpful, intelligent e-commerce customer support assistant for the Souqna Syrian craft platform. 
            Answer in the language that the user queries with (Arabic or English). Keep answers concise and related to shopping, products (Aleppo soap, Rose oil, olive oil, spices, or mamoul shortbread), shipping rates, and delivery within Damascus or Syrian governorates.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nUser Question: $userQuery")
                        })
                    })
                })
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext "عذراً، أواجه مشكلة في الاتصال بخوادم الذكاء الاصطناعي الآن. كيف يمكنني مساعدتك برمجياً؟"
            val responseString = response.body?.string() ?: return@withContext "عذراً، لم أستلم رد بريدي."
            
            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return@withContext parts.getJSONObject(0).getString("text")
        }
    }

    private fun getFallbackSupportResponse(query: String): String {
        val lang = activeLanguage.value
        val lower = query.lowercase()
        return if (lang == AppLanguage.AR) {
            when {
                lower.contains("توصيل") || lower.contains("شحن") || lower.contains("أين") -> 
                    "أهلاً بك! يتم التوصيل في كافة أحياء دمشق ومحافظات حلب، حمص، واللاذقية عبر فريق التوصيل السريع (سامر وموفق) خلال 24-48 ساعة. تكلفة التوصيل رمزية جداً ومضمنة بالتسعيرة."
                lower.contains("سعر") || lower.contains("ليرة") || lower.contains("دولار") || lower.contains("صرف") -> 
                    "أهلاً بك! نقبل الليرة السورية كعملتنا الافتراضية، ويمكنك التحويل إلى الدولار واليورو من أعلى الشاشة لحساب السعر المعادل تلقائياً استناداً لمعدلات الصرف التي يحددها المدير."
                lower.contains("غار") || lower.contains("صابون") -> 
                    "صابون الغار الحلبي المتوفر لدينا بتركيز 20% زيت غار طبيعي أصيل ومصنوع بطرق تقليدية متوارثة. يغذي البشرة ويفيد فروة الرأس."
                lower.contains("مستودع") || lower.contains("مخزن") or lower.contains("منتج") -> 
                    "يمكن لصاحب المتجر (التاجر أبو أحمد) تعديل كميات المخزون وتحديث الأسعار وإضافة منتجات من ذاكرة جهازه اللوحية أو الموبايل عبر تبديل الدور من أعلى اللوحة."
                lower.contains("استرجاع") || lower.contains("refund") || lower.contains("ترجيع") -> 
                    "يمكنك طلب استرداد الأموال للطلبات من قسم طلباتي فورياً، وسيقوم النظام بإرجاع رصيدك وتحديث نقاط الولاء فور قبول الطلب."
                else -> "مرحباً بك في خدمة عملاء سوقنا الشامي! أنا هنا لمساعدتك في تتبع الشحنات، تصفح صابون الغار، زيت الورد الدمشقي، أو إتمام الكاش محلياً. كيف يمكنني إرشادك اليوم؟"
            }
        } else {
            when {
                lower.contains("deliver") || lower.contains("shipping") || lower.contains("where") -> 
                    "Welcome! We deliver to all Damascus districts, Aleppo, Homs, and Latakia within 24-48 hours via fast couriers (Samer & Mowaffaq). Shipping is fully integrated."
                lower.contains("price") || lower.contains("currency") || lower.contains("syp") || lower.contains("usd") -> 
                    "Welcome! Syrian Pound (SYP) is our primary currency. You can switch to USD or EUR in the top menu to view dynamic localized rates computed in real time."
                lower.contains("soap") || lower.contains("laurel") -> 
                    "Our Aleppo premium Laurel soap is 100% natural, containing 20% pure laurel berry and 80% prime olive oils. It is handcrafted using ancient Syrian boiling methods."
                lower.contains("refund") || lower.contains("return") -> 
                    "You can easily tap 'Request Instant Refund' on any completed order in your shopping logs to reverse transaction balances and recover spent currency."
                else -> "Welcome to Souqna Help Desk! I can assist with order tracking, Syrian craft specifications, or checkouts."
            }
        }
    }

    // Add New Product by Merchant (Abu Ahmad)
    fun addNewProduct(
        titleEn: String, titleAr: String,
        descEn: String, descAr: String,
        categoryEn: String, categoryAr: String,
        priceSyp: Double, stock: Int,
        imageUri: String?, specsEn: String, specsAr: String
    ) {
        viewModelScope.launch {
            val newP = Product(
                titleEn = titleEn,
                titleAr = titleAr,
                descriptionEn = descEn,
                descriptionAr = descAr,
                categoryEn = categoryEn,
                categoryAr = categoryAr,
                priceInSyp = priceSyp,
                stockCount = stock,
                localUri = imageUri,
                merchantId = "merchant_1",
                specsEn = specsEn,
                specsAr = specsAr
            )
            repository.productDao.insertProduct(newP)

            // Alert customer of new arrivals
            notifications.update { current ->
                current + "منتج جديد متوفر الآن في المتجر: $titleAr المميز!"
            }
        }
    }

    // Merchant Updates Stocks
    fun updateProductDetails(product: Product) {
        viewModelScope.launch {
            repository.productDao.updateProduct(product)
            if (product.stockCount <= 5) {
                // Add notifications for low inventory
                notifications.update { current ->
                    current + "تنبيه مخزن أبو أحمد: المنتج '${product.titleAr}' منخفض جداً (${product.stockCount} قطع معلقة)!"
                }
            }
        }
    }

    // Admin updates currency rates
    fun updateCurrencyRate(code: String, rateToSyp: Double) {
        viewModelScope.launch {
            val currentRate = repository.currencyDao.getRateByCode(code)
            if (currentRate != null) {
                repository.currencyDao.insertRate(currentRate.copy(rateToSyp = rateToSyp))
            }
        }
    }
}
