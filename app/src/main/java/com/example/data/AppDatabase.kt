package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun observeAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE merchantId = :merchantId ORDER BY id DESC")
    fun observeProductsByMerchant(merchantId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    @Query("SELECT * FROM products WHERE titleEn LIKE '%' || :query || '%' OR titleAr LIKE '%' || :query || '%' OR categoryEn LIKE '%' || :query || '%' OR categoryAr LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun observeCartItems(userId: String): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItems(userId: String): List<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun observeAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY id DESC")
    fun observeOrdersByCustomer(customerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE merchantId = :merchantId ORDER BY id DESC")
    fun observeOrdersByMerchant(merchantId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrderById(id: Int): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY date DESC")
    fun observeReviewsForProduct(productId: Int): Flow<List<ProductReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ProductReview)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE chatRoomId = :chatRoomId ORDER BY timestamp ASC")
    fun observeChatMessages(chatRoomId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency_rates")
    fun observeCurrencyRates(): Flow<List<CurrencyRate>>

    @Query("SELECT * FROM currency_rates WHERE code = :code")
    suspend fun getRateByCode(code: String): CurrencyRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: CurrencyRate)
}

@Database(
    entities = [
        User::class,
        Product::class,
        CartItem::class,
        Order::class,
        ProductReview::class,
        ChatMessage::class,
        CurrencyRate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val productDao: ProductDao
    abstract val cartDao: CartDao
    abstract val orderDao: OrderDao
    abstract val reviewDao: ReviewDao
    abstract val chatDao: ChatDao
    abstract val currencyDao: CurrencyDao
}
