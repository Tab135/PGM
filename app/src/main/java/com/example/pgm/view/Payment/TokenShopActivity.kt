package com.example.pgm.view.Payment
import android.view.LayoutInflater
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.TokenPackage
import com.example.pgm.model.TransactionType
import com.example.pgm.model.Database.UserDatabaseHelper
import com.example.pgm.model.Database.TokenTransactionDatabaseHelper
import com.example.pgm.utils.SessionManager

class TokenShopActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TokenPackageAdapter
    private lateinit var backButton: ImageView
    private lateinit var currentBalanceTextView: TextView
    private lateinit var userDbHelper: UserDatabaseHelper
    private lateinit var transactionDbHelper: TokenTransactionDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_shop)

        initViews()
        setupControllers()
        loadUserSession()
        updateTokenBalance()
        setupTokenPackages()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.tokenPackagesRecyclerView)
        backButton = findViewById(R.id.backButton)
        currentBalanceTextView = findViewById(R.id.currentBalanceTextView)
    }

    private fun setupControllers() {
        userDbHelper = UserDatabaseHelper(this)
        transactionDbHelper = TokenTransactionDatabaseHelper(this) // Initialize this
        sessionManager = SessionManager(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
        if (currentUserId == -1) {
            Toast.makeText(this, "Please log in to purchase tokens", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateTokenBalance() {
        val balance = userDbHelper.getUserTokens(currentUserId)
        currentBalanceTextView.text = "Current Balance: 🪙 $balance tokens"
    }

    private fun setupTokenPackages() {
        val packages = listOf(
            TokenPackage(
                id = 1,
                name = "Starter Pack",
                tokens = 50,
                price = 0.99,
                bonus = 0,
                isPopular = false,
                description = "Perfect for trying out"
            ),
            TokenPackage(
                id = 2,
                name = "Basic Pack",
                tokens = 120,
                price = 1.99,
                bonus = 20,
                isPopular = false,
                description = "+20 bonus tokens"
            ),
            TokenPackage(
                id = 3,
                name = "Popular Pack",
                tokens = 300,
                price = 4.99,
                bonus = 50,
                isPopular = true,
                description = "+50 bonus tokens • Best Value"
            ),
            TokenPackage(
                id = 4,
                name = "Premium Pack",
                tokens = 650,
                price = 9.99,
                bonus = 150,
                isPopular = false,
                description = "+150 bonus tokens"
            ),
            TokenPackage(
                id = 5,
                name = "Ultimate Pack",
                tokens = 1500,
                price = 19.99,
                bonus = 500,
                isPopular = false,
                description = "+500 bonus tokens • Best Deal"
            ),
            TokenPackage(
                id = 6,
                name = "Mega Pack",
                tokens = 3500,
                price = 49.99,
                bonus = 1500,
                isPopular = false,
                description = "+1500 bonus tokens • Maximum Value"
            )
        )

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = TokenPackageAdapter(packages) { tokenPackage ->
            showPaymentDialog(tokenPackage)
        }
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showPaymentDialog(tokenPackage: TokenPackage) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.setCancelable(true)

        // Set package info
        val packageInfoText = dialogView.findViewById<TextView>(R.id.packageInfoText)
        val packageInfo = buildString {
            append("Package: ${tokenPackage.name}\n")
            append("Tokens: ${tokenPackage.tokens}")
            if (tokenPackage.bonus > 0) {
                append(" + ${tokenPackage.bonus} bonus")
            }
            append("\nTotal: ${tokenPackage.tokens + tokenPackage.bonus} tokens")
            append("\nPrice: $${String.format("%.2f", tokenPackage.price)}")
        }
        packageInfoText.text = packageInfo

        val paymentMethodGroup = dialogView.findViewById<RadioGroup>(R.id.paymentMethodGroup)
        val buyButton = dialogView.findViewById<Button>(R.id.buyButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Enable buy button when a payment method is selected
        paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            buyButton.isEnabled = checkedId != -1
        }

        buyButton.setOnClickListener {
            val selectedId = paymentMethodGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton = dialogView.findViewById<RadioButton>(selectedId)
                val paymentMethod = selectedRadioButton.text.toString()
                dialog.dismiss()
                processPayment(tokenPackage, paymentMethod)
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun processPayment(tokenPackage: TokenPackage, paymentMethod: String) {
        // Show payment processing dialog
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Processing Payment")
            .setMessage("Please wait while we process your payment via $paymentMethod...")
            .setCancelable(false)
            .create()

        progressDialog.show()

        // Simulate payment processing (in real app, integrate with payment gateway)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()

            // In a real app, this would be after successful payment from gateway
            // For now, we simulate a successful payment
            completePayment(tokenPackage, paymentMethod)
        }, 2000)
    }

    private fun completePayment(tokenPackage: TokenPackage, paymentMethod: String) {
        val totalTokens = tokenPackage.tokens + tokenPackage.bonus

        // Add tokens to user account
        val success = userDbHelper.addTokens(currentUserId, totalTokens)

        if (success) {
            // Record the transaction
            transactionDbHelper.recordPurchase(
                userId = currentUserId,
                amount = totalTokens,
                price = tokenPackage.price,
                paymentMethod = paymentMethod,
                packageName = tokenPackage.name
            )

            // Show success dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("✅ Payment Successful!")

            val message = buildString {
                append("Thank you for your purchase!\n\n")
                append("Package: ${tokenPackage.name}\n")
                append("Tokens received: $totalTokens\n")
                if (tokenPackage.bonus > 0) {
                    append("(${tokenPackage.tokens} + ${tokenPackage.bonus} bonus)\n")
                }
                append("\nPayment method: $paymentMethod\n")
                append("Amount charged: $${String.format("%.2f", tokenPackage.price)}\n")

                val newBalance = userDbHelper.getUserTokens(currentUserId)
                append("\nNew balance: 🪙 $newBalance tokens")
            }

            builder.setMessage(message)
            builder.setPositiveButton("Great!") { _, _ ->
                updateTokenBalance()
            }
            builder.setCancelable(false)
            builder.show()

            // Update the balance display
            updateTokenBalance()
        } else {
            Toast.makeText(
                this,
                "Failed to add tokens. Please contact support.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateTokenBalance()
    }
}