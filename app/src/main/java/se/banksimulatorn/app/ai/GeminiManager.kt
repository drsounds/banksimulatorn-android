package se.banksimulatorn.app.ai

import android.content.Context
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import com.google.ai.edge.aicore.DownloadCallback
import com.google.ai.edge.aicore.DownloadConfig
import com.google.ai.edge.aicore.GenerativeAIException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class GeminiManager(private val context: Context) {
    
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady

    private val _statusMessage = MutableStateFlow("Initializing...")
    val statusMessage: StateFlow<String> = _statusMessage

    private val statusCallback = object : DownloadCallback {
        override fun onDownloadStarted(bytesToDownload: Long) {
            _statusMessage.value = "Downloading model (${bytesToDownload / 1024 / 1024} MB)..."
            _isModelReady.value = false
        }

        override fun onDownloadProgress(totalBytesDownloaded: Long) {
            _statusMessage.value = "Downloading... ${totalBytesDownloaded / 1024 / 1024} MB received."
        }

        override fun onDownloadCompleted() {
            _statusMessage.value = "Model Ready"
            _isModelReady.value = true
        }

        override fun onDownloadFailed(failureStatus: String, e: GenerativeAIException) {
            _statusMessage.value = "Download Failed: $failureStatus"
            _isModelReady.value = false
        }
    }

    private val model: GenerativeModel by lazy {
        val config = generationConfig {
            this.context = this@GeminiManager.context
            this.temperature = 0.1f
            this.topK = 1
        }
        GenerativeModel(
            generationConfig = config,
            downloadConfig = DownloadConfig(statusCallback)
        )
    }

    /**
     * Checks if the model is ready by attempting a simple generation if the callback hasn't confirmed it yet.
     */
    suspend fun checkReadiness(): Boolean {
        if (_isModelReady.value) return true
        
        return try {
            model.generateContent("ping")
            _isModelReady.value = true
            _statusMessage.value = "Model Ready"
            true
        } catch (e: Exception) {
            if (e.message?.contains("NOT_AVAILABLE") == true) {
                _statusMessage.value = "Model Downloadable/Unavailable"
            }
            false
        }
    }

    /**
     * Triggers the download of the on-device model.
     */
    fun triggerDownload() {
        // Accessing model triggers initialization and download if config provided
        model 
    }

    /**
     * Converts a user life description into a complete initial financial state.
     */
    suspend fun generateInitialLifeState(description: String): String? = withContext(Dispatchers.IO) {
        val prompt = """
            Act as a financial life generator for a banking simulator. 
            Based on the user description, generate a valid JSON object matching the 'BankDataBundle' schema.
            
            User Description: "$description"
            
            JSON Schema Requirements (STRICT):
            - "accounts": List of { "name": String, "balance": Double, "type": "CHECKING"|"SAVINGS", "interestRate": Double }
            - "revolvingCredits": List of { "name": String, "creditLimit": Double, "interestRate": Double, "statementDay": 1..28 }
            - "loans": List of { "name": String, "balance": Double, "loanFee": Double, "invoiceCycleDay": 1..28 }
            - "assets": List of { "name": String, "type": "VILLA"|"CONDO"|"CAR", "currentValue": Double, "monthlyGrowthRate": Double }
            - "budgetItems": List of { "name": String, "amount": Double, "type": "INCOME"|"EXPENSE", "frequency": "MONTHLY", "paymentMethod": "DIRECT_DEBIT"|"CREDIT_CARD"|"E_INVOICE" }
            - "transactions": [], "invoices": [], "recurringTasks": [], "creditCards": []
            - "globalSettings": { "currency": "SEK", "country": "Sweden" }
            
            Return ONLY the raw JSON string. Do not include markdown code blocks.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            extractJson(response.text)
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
            - If it's a permanent change, update the corresponding 'budgetItems' or 'interestRate'.
            - Return the complete updated JSON matching the 'BankDataBundle' schema.
            - Return ONLY raw JSON. No markdown.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            extractJson(response.text)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractJson(text: String?): String? {
        if (text == null) return null
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return text
    }
}
