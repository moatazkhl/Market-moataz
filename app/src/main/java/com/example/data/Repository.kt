package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ECommerceRepository(private val db: AppDatabase) {

    // DAOs access
    val userDao = db.userDao
    val productDao = db.productDao
    val cartDao = db.cartDao
    val orderDao = db.orderDao
    val reviewDao = db.reviewDao
    val chatDao = db.chatDao
    val currencyDao = db.currencyDao

    // Exposed Flows
    val allProducts: Flow<List<Product>> = productDao.observeAllProducts()
    val currencyRates: Flow<List<CurrencyRate>> = currencyDao.observeCurrencyRates()
    val allOrders: Flow<List<Order>> = orderDao.observeAllOrders()

    fun observeCart(userId: String) = cartDao.observeCartItems(userId)
    fun observeOrdersForCustomer(customerId: String) = orderDao.observeOrdersByCustomer(customerId)
    fun observeOrdersForMerchant(merchantId: String) = orderDao.observeOrdersByMerchant(merchantId)
    fun observeProductsByMerchant(merchantId: String) = productDao.observeProductsByMerchant(merchantId)
    fun observeReviews(productId: Int) = reviewDao.observeReviewsForProduct(productId)
    fun observeChatMessages(roomId: String) = chatDao.observeChatMessages(roomId)
    fun observeUser(userId: String) = userDao.observeUserById(userId)

    suspend fun getProduct(id: Int) = productDao.getProductById(id)
    suspend fun getUser(id: String) = userDao.getUserById(id)

    // Prepopulate Data if Empty
    suspend fun populateInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Prepopulate Currencies
        val existingRates = currencyDao.observeCurrencyRates().first()
        if (existingRates.isEmpty()) {
            currencyDao.insertRate(CurrencyRate("SYP", "ل.س", 1.0))
            currencyDao.insertRate(CurrencyRate("USD", "$", 15000.0))
            currencyDao.insertRate(CurrencyRate("EUR", "€", 16200.0))
        }

        // Prepopulate Users
        val demoCustomer = userDao.getUserById("customer_1")
        if (demoCustomer == null) {
            userDao.insertUser(User("customer_1", "خالد الأحمد (عميل)", UserRole.CUSTOMER, loyaltyPoints = 250, balanceSyp = 500000.0))
            userDao.insertUser(User("merchant_1", "أبو أحمد (صاحب متجر الشامية)", UserRole.MERCHANT, shopName = "الشامية المتميزة", isSubscribed = true, balanceSyp = 1200000.0))
            userDao.insertUser(User("admin_1", "أدمن منصة سوقنا", UserRole.ADMIN, balanceSyp = 85000.0))
        }

        // Prepopulate Products
        val existingProducts = productDao.observeAllProducts().first()
        if (existingProducts.isEmpty()) {
            val items = listOf(
                Product(
                    titleEn = "Aleppo Laurel Soap Premium",
                    titleAr = "صابون غار حلبي فاخر وممتاز",
                    descriptionEn = "Authentic Syrian soap made with pure olive oil and laurel oil berry extract. Great for skin and hair health.",
                    descriptionAr = "صابون غار حلبي أصيل، مصنوع بالكامل من زيت الزيتون البكر الطبيعي وخلاصة زيت الغار السحري للعناية بالشعر والبشرة.",
                    categoryEn = "Health & Beauty",
                    categoryAr = "الصحة والجمال",
                    priceInSyp = 18000.0,
                    stockCount = 120,
                    merchantId = "merchant_1",
                    imageResName = "soap",
                    specsEn = "Ingredients: 20% Laurel Oil, 80% Olive Oil; Weight: 200g; Origin: Aleppo, Syria",
                    specsAr = "المكونات: 20% زيت غار، 80% زيت زيتون؛ الوزن: 200 غرام؛ المنشأ: حلب العريقة، سوريا"
                ),
                Product(
                    titleEn = "Pure Damascus Rose Oil Escential",
                    titleAr = "زيت الورد الدمشقي العطري النقي",
                    descriptionEn = "100% natural distilled aromatic Damascene Rose Oil. Widely acclaimed for perfumes and relaxing aromatherapy.",
                    descriptionAr = "زيت الورد الجوري الدمشقي الأصلي المستخلص بالتقطير البخاري النقي، رائع للتعطير الفاخر والاسترخاء ومكافحة التجاعيد.",
                    categoryEn = "Perfumes & Oils",
                    categoryAr = "العطور والزيوت",
                    priceInSyp = 95000.0,
                    stockCount = 24,
                    merchantId = "merchant_1",
                    imageResName = "rose_oil",
                    specsEn = "Pure single-distilled organic rose water; Volume: 15ml; Packing: Dropper Glass",
                    specsAr = "مستخلص قطيفة طبيعي 100%؛ الحجم: 15 مل؛ مغلف في زجاجة قطارة فاخرة"
                ),
                Product(
                    titleEn = "Organic Syrian Olive Oil (Extra Virgin)",
                    titleAr = "زيت زيتون سوري بكر ممتاز عصرة أولى",
                    descriptionEn = "Cold-pressed extra-virgin olive oil harvested from the old hills of Afrin and Idlib orchards.",
                    descriptionAr = "زيت زيتون سوري بكر ممتاز، معصور على البارد من حقول غابات جبال عفرين وإدلب الخضراء، طعم غني وحموضة أقل من 0.8%.",
                    categoryEn = "Food & Spices",
                    categoryAr = "المأكولات والبهارات",
                    priceInSyp = 110000.0,
                    stockCount = 45,
                    merchantId = "merchant_1",
                    imageResName = "olive_oil",
                    specsEn = "Acidity: < 0.8%; Size: 1 Liter; Vintage: Autumn 2026",
                    specsAr = "الحموضة: أقل من 0.8%؛ الحجم: 1 ليتر؛ تاريخ الإنتاج: خريف 2026"
                ),
                Product(
                    titleEn = "Traditional Syrian Mixed Spices (Damascene)",
                    titleAr = "خلطة البهارات الدمشقية المشكلة الفاخرة",
                    descriptionEn = "The famous seven aromatic spice blend of old Damascus souqs. Perfect for meat, rice, and traditional stews.",
                    descriptionAr = "خلطة السبع بهارات الدمشقية الشهيرة من قلب أسواق الشام القديمة، تضفي نكهة ساحرة لأطباق اللحوم والأرز والكبسة السورية.",
                    categoryEn = "Food & Spices",
                    categoryAr = "المأكولات والبهارات",
                    priceInSyp = 14000.0,
                    stockCount = 200,
                    merchantId = "merchant_1",
                    imageResName = "spices",
                    specsEn = "Blend: cardamom, cinnamon, cloves, nutmeg, coriander, allspice, black pepper; Weight: 250g",
                    specsAr = "توليفة: هيل، قرفة، قرنفل، جوزة الطيب، كزبرة، فلفل أسود، بهار حلو؛ الوزن: 250 غرام"
                ),
                Product(
                    titleEn = "Premium Aleppo Pistachio Maamoul Box",
                    titleAr = "علبة معمول بالفستق الحلبي الفاخر على الأصول",
                    descriptionEn = "Freshly baked traditional shortbread buttery cookies filled generously with premium ground Aleppo pistachios.",
                    descriptionAr = "معمول الفستق الحلبي الفاخر المصنع بالسمن الحيواني البلدي الفاخر ومحشو بالكامل بالفستق الحلبي الطازج الغني بالقطر الشامي.",
                    categoryEn = "Sweets & Pastries",
                    categoryAr = "الحلويات والمعجنات",
                    priceInSyp = 65000.0,
                    stockCount = 60,
                    merchantId = "merchant_1",
                    imageResName = "maamoul",
                    specsEn = "Quantity: 24 pieces; Ingredients: Premium Semolina, Ghee, Aleppo Pistachio, Blossom Water",
                    specsAr = "الكمية: 24 قطعة فاخرة؛ المكونات: سميد ممتاز، سمن عربي بلدي، فستق حلبي أخضر، ماء زهر"
                )
            )

            // Insert predefined products
            for (item in items) {
                productDao.insertProduct(item)
            }

            // Insert initial default reviews
            reviewDao.insertReview(ProductReview(productId = 1, reviewerName = "سلمى المصري", rating = 5, comment = "صابون الغار هذا أكثر من رائع! يعطي نعومة رائعة للبشرة ورائحته منعشة جداً."))
            reviewDao.insertReview(ProductReview(productId = 1, reviewerName = "عمر الشامي", rating = 4, comment = "منتج أصيل ونظيف، تغليف جيد جداً وتوصيل سريع للغاية."))
            reviewDao.insertReview(ProductReview(productId = 2, reviewerName = "لين الحلبي", rating = 5, comment = "زيت الورد مركز ورائع جداً للوجه والترطيب. أنصح الجميع بشرائه من متجر أبو أحمد."))
        }
    }

    // Dynamic Currency Calculator Helper
    suspend fun getConvertedPrice(priceInSyp: Double, targetCurrency: String): Double = withContext(Dispatchers.IO) {
        if (targetCurrency == "SYP") return@withContext priceInSyp
        val rate = currencyDao.getRateByCode(targetCurrency)?.rateToSyp ?: return@withContext priceInSyp
        return@withContext priceInSyp / rate
    }

    // Add To Cart logic
    suspend fun addProductToCart(productId: Int, quantity: Int, userId: String) = withContext(Dispatchers.IO) {
        val existing = cartDao.getCartItems(userId).firstOrNull { it.productId == productId }
        if (existing != null) {
            cartDao.updateCartQuantity(existing.id, existing.quantity + quantity)
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, quantity = quantity, userId = userId))
        }
    }

    // Create Order with Commission & Loyalty points accumulation
    // Syrian Pound is processed, commissions is calculated (e.g. 5% platform service earnings)
    suspend fun checkout(
        userId: String,
        selectedCurrency: String,
        paymentMethod: String,
        cartItems: List<Pair<Product, Int>>
    ): Order? = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext null
        val rate = currencyDao.getRateByCode(selectedCurrency)?.rateToSyp ?: 1.0

        // Calculate totals
        var totalSyp = 0.0
        val itemsSummary = StringBuilder()

        for ((product, qty) in cartItems) {
            totalSyp += product.priceInSyp * qty
            itemsSummary.append("${if (itemsSummary.isNotEmpty()) ", " else ""}${qty}x ${product.titleAr} (${product.titleEn})")
            
            // Deduct stock
            val newStock = (product.stockCount - qty).coerceAtLeast(0)
            productDao.updateProduct(product.copy(stockCount = newStock))
        }

        if (totalSyp <= 0) return@withContext null

        val finalConverted = totalSyp / rate

        // Dynamic Delivery allocation
        val deliveryStaff = listOf("موفق الشام", "سامر التوصيل السريع", "عادل الحلبي").random()

        // Insert into Orders
        val newOrder = Order(
            customerId = userId,
            merchantId = "merchant_1", // In this demo, products are owned by Abu Ahmad
            status = "PENDING",
            totalAmountInSyp = totalSyp,
            targetCurrencyCode = selectedCurrency,
            convertedAmount = finalConverted,
            deliveryStaffName = deliveryStaff,
            paymentMethod = paymentMethod,
            invoiceDetails = itemsSummary.toString(),
            // Set starting lat/lng matching Damascus
            trackingLatitude = 33.5138,
            trackingLongitude = 36.2765
        )

        val orderId = orderDao.insertOrder(newOrder).toInt()

        // Admin collects platform commission (5% commission model - "التطبيق ربحي")
        val adminCommission = totalSyp * 0.05
        val admin = userDao.getUserById("admin_1")
        if (admin != null) {
            userDao.updateUser(admin.copy(balanceSyp = admin.balanceSyp + adminCommission))
        }

        // Loyal customer receives points (e.g., 1 reward point for each 5,000 SYP spent)
        val gainedPoints = (totalSyp / 5000.0).toInt()
        val updatedPoints = user.loyaltyPoints + gainedPoints
        val finalBalance = if (paymentMethod == "Wallet Pay") {
            (user.balanceSyp - totalSyp).coerceAtLeast(0.0)
        } else {
            user.balanceSyp
        }
        
        userDao.updateUser(user.copy(
            loyaltyPoints = updatedPoints,
            balanceSyp = finalBalance
        ))

        // Clear cart
        cartDao.clearCart(userId)

        return@withContext newOrder.copy(id = orderId)
    }

    // Submit product review & refresh score
    suspend fun submitReview(productId: Int, reviewerName: String, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        val review = ProductReview(productId = productId, reviewerName = reviewerName, rating = rating, comment = comment)
        reviewDao.insertReview(review)

        // Retrieve current product to update average rating fields
        val product = productDao.getProductById(productId)
        if (product != null) {
            val updatedCount = product.ratingCount + 1
            val updatedSum = product.ratingSum + rating
            productDao.updateProduct(product.copy(ratingCount = updatedCount, ratingSum = updatedSum))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ECommerceRepository? = null

        fun getInstance(context: Context): ECommerceRepository {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "souqna_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                val repo = ECommerceRepository(db)
                INSTANCE = repo
                repo
            }
        }
    }
}
