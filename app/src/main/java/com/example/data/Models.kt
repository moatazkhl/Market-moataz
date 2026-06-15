package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class UserRole {
    CUSTOMER,
    MERCHANT,
    ADMIN
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val role: UserRole,
    val loyaltyPoints: Int = 0,
    val shopName: String? = null,
    val isSubscribed: Boolean = false,
    val balanceSyp: Double = 0.0
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val categoryEn: String,
    val categoryAr: String,
    val priceInSyp: Double, // Default base is Syrian Pound
    val stockCount: Int,
    val ratingCount: Int = 0,
    val ratingSum: Double = 0.0,
    val localUri: String? = null, // URI for merchant uploaded photos
    val imageResName: String? = null, // Mock drawing name or category name for built-in drawables
    val merchantId: String, // Shop owner ID
    val specsEn: String = "",
    val specsAr: String = ""
) {
    val averageRating: Float
        get() = if (ratingCount > 0) (ratingSum / ratingCount).toFloat() else 0f
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int,
    val userId: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: String,
    val merchantId: String,
    val date: Long = System.currentTimeMillis(),
    val status: String, // PENDING, PREPARING, DISPATCHED, DELIVERING, DELIVERED, REFUNDED
    val totalAmountInSyp: Double,
    val targetCurrencyCode: String = "SYP",
    val convertedAmount: Double,
    val deliveryStaffName: String = "",
    val paymentMethod: String, // Credit Card, Syriatel Cash, Cash on Delivery
    val invoiceDetails: String, // Simplified items list summary
    val trackingLatitude: Double = 33.5138, // Defaults around Damascus capital
    val trackingLongitude: Double = 36.2765,
    val isRefundRequested: Boolean = false
)

@Entity(tableName = "product_reviews")
data class ProductReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val reviewerName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatRoomId: String, // "support" or match merchant user ID
    val senderRole: String, // "USER" or "ASSISTANT" or "MERCHANT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "currency_rates")
data class CurrencyRate(
    @PrimaryKey val code: String, // SYP, USD, EUR
    val symbol: String, // ل.س, $, €
    val rateToSyp: Double // 1.0, 15000.0, 16200.0 respectively
)

class DatabaseTypeConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }
}
