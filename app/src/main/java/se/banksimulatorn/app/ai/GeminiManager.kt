package se.banksimulatorn.app.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.banksimulatorn.app.ui.settings.BankDataBundle

class GeminiManager(apiKey: String) {
    
    // For Gemini Nano on-device, one would use the Google AI Edge SDK.
    // We use the Generative AI SDK here as it is currently the most stable way to integrate Gemini into Android.
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val gson = Gson()

    /**
     * Converts a user life description into a complete initial financial state.
     */
    suspend fun generateInitialLifeState(description: String): String? = withContext(Dispatchers.IO) {
        val prompt = """
            Act as a financial life generator for a banking simulator. 
            Based on the user description, generate a valid JSON object matching the 'BankDataBundle' schema.
            
            User Description: "$description"
            
            JSON Schema Requirements:
            - "accounts": List of { "name", "balance", "type": "CHECKING"|"SAVINGS", "interestRate" }
            - "revolvingCredits": List of { "name": "Credit Card", "creditLimit", "interestRate", "statementDay": 1..28 }
            - "loans": List of { "name", "balance", "loanFee", "invoiceCycleDay" }
            - "assets": List of { "name", "type": "VILLA"|"CONDO"|"CAR", "currentValue", "monthlyGrowthRate" }
            - "budgetItems": List of { "name", "amount", "type": "INCOME"|"EXPENSE", "frequency": "MONTHLY", "paymentMethod": "DIRECT_DEBIT"|"CREDIT_CARD"|"E_INVOICE" }
            - "transactions": [], "invoices": [], "recurringTasks": []
            - "globalSettings": { "currency": "SEK", "country": "Sweden" }
            
            Return ONLY the raw JSON string. No markdown code blocks.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            // Cleanup any accidental markdown formatting
            response.text?.replace("```json", "")?.replace("```", "")?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Modifies the current financial state based on an economic event.
     */
    suspend fun simulateEconomicEvent(event: String, currentStateJson: String): String? = withContext(Dispatchers.IO) {
        val prompt = """
            Apply the following economic event to the current bank state of the simulator.
            
            Event: "$event"
            Current State: $currentStateJson
            
            Instructions:
            - If it's a one-time income/expense, add a transaction to the appropriate account.
            - If it's a permanent change (e.g. "I got a raise"), update the corresponding 'budgetItems' or 'interestRate'.
            - Return the complete updated JSON matching the 'BankDataBundle' schema.
            - Return ONLY raw JSON. No markdown.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            response.text?.replace("```json", "")?.replace("```", "")?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
