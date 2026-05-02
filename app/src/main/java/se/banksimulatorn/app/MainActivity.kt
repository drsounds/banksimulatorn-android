package se.banksimulatorn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import se.banksimulatorn.app.ai.GeminiManager
import se.banksimulatorn.app.data.BankDatabase
import se.banksimulatorn.app.navigation.AccountSettingsType
import se.banksimulatorn.app.navigation.Destination
import se.banksimulatorn.app.ui.dashboard.DashboardScreen
import se.banksimulatorn.app.ui.dashboard.DashboardViewModel
import se.banksimulatorn.app.ui.transactions.TransactionScreen
import se.banksimulatorn.app.ui.transactions.TransactionViewModel
import se.banksimulatorn.app.ui.account.AccountDetailScreen
import se.banksimulatorn.app.ui.account.AccountDetailViewModel
import se.banksimulatorn.app.ui.credits.CreditDetailScreen
import se.banksimulatorn.app.ui.credits.CreditDetailViewModel
import se.banksimulatorn.app.ui.loans.LoanDetailScreen
import se.banksimulatorn.app.ui.loans.LoanDetailViewModel
import se.banksimulatorn.app.ui.purchase.PurchaseScreen
import se.banksimulatorn.app.ui.purchase.PurchaseViewModel
import se.banksimulatorn.app.ui.transaction_detail.BlockedTransactionDetailScreen
import se.banksimulatorn.app.ui.transaction_detail.BlockedTransactionDetailViewModel
import se.banksimulatorn.app.ui.settings.*
import se.banksimulatorn.app.ui.create.*
import se.banksimulatorn.app.ui.invoice.*
import se.banksimulatorn.app.ui.assets.*
import se.banksimulatorn.app.ui.budget.*
import se.banksimulatorn.app.ui.onboarding.*
import se.banksimulatorn.app.ui.aichat.*
import se.banksimulatorn.app.ui.timemachine.TimeMachineBar
import se.banksimulatorn.app.ui.timemachine.TimeMachineViewModel
import se.banksimulatorn.app.ui.theme.BankingSimulatorTheme
import se.banksimulatorn.app.ui.history.HistoryScreen
import se.banksimulatorn.app.ui.history.HistoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = BankDatabase.getDatabase(this)
        val bankDao = database.bankDao()
        val geminiManager = GeminiManager(context = this)

        setContent {
            BankingSimulatorTheme {
                val backStack = rememberNavBackStack(Destination.Dashboard as NavKey)
                val timeMachineViewModel: TimeMachineViewModel = viewModel { TimeMachineViewModel(bankDao) }

                LaunchedEffect(Unit) {
                    // Trigger download and readiness check for Gemini Nano
                    geminiManager.triggerDownload()

                    if (bankDao.hasGlobalSettings() == 0) {
                        backStack.add(Destination.Onboarding)
                    }
                }
                
                val popSafe = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            TimeMachineBar(
                                viewModel = timeMachineViewModel,
                                modifier = Modifier.fillMaxWidth()
                            )
                            NavigationBar {
                                val currentDestination = backStack.lastOrNull()
                                NavigationBarItem(
                                    selected = currentDestination == Destination.Dashboard,
                                    onClick = { 
                                        if (currentDestination != Destination.Dashboard) {
                                            backStack.clear()
                                            backStack.add(Destination.Dashboard) 
                                        }
                                    },
                                    icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.home)) },
                                    label = { Text(stringResource(R.string.home)) }
                                )
                                NavigationBarItem(
                                    selected = currentDestination == Destination.Budget,
                                    onClick = { if (currentDestination != Destination.Budget) backStack.add(Destination.Budget) },
                                    icon = { Icon(Icons.Rounded.ReceiptLong, contentDescription = "Budget") },
                                    label = { Text("Budget") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination == Destination.Assets,
                                    onClick = { if (currentDestination != Destination.Assets) backStack.add(Destination.Assets) },
                                    icon = { Icon(Icons.Rounded.Wallet, contentDescription = "Assets") },
                                    label = { Text("Assets") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination == Destination.AIChat,
                                    onClick = { if (currentDestination != Destination.AIChat) backStack.add(Destination.AIChat) },
                                    icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Sim") },
                                    label = { Text("AI Sim") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination == Destination.Settings,
                                    onClick = { 
                                        if (currentDestination != Destination.Settings) {
                                            backStack.add(Destination.Settings)
                                        }
                                    },
                                    icon = { Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.settings)) },
                                    label = { Text(stringResource(R.string.settings)) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val entryProvider = remember {
                        entryProvider<NavKey> {
                            entry<Destination.Dashboard> {
                                val dashboardViewModel: DashboardViewModel = viewModel { 
                                    DashboardViewModel(bankDao) 
                                }
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onAccountClick = { id ->
                                        backStack.add(Destination.AccountDetail(id))
                                    },
                                    onHistoryClick = {
                                        backStack.add(Destination.History)
                                    },
                                    onNewTransactionClick = { id ->
                                        backStack.add(Destination.TransactionSimulator(id))
                                    },
                                    onNewPurchaseClick = { id ->
                                        backStack.add(Destination.PurchaseSimulator(id))
                                    },
                                    onLoanClick = { id ->
                                        backStack.add(Destination.LoanDetail(id))
                                    },
                                    onCreditClick = { id ->
                                        backStack.add(Destination.CreditDetail(id))
                                    },
                                    onTransactionClick = { id ->
                                        backStack.add(Destination.BlockedTransactionDetail(id))
                                    },
                                    onInvoiceClick = { id ->
                                        backStack.add(Destination.InvoicePayment(id))
                                    },
                                    onOnboardRequest = {
                                        backStack.add(Destination.Onboarding)
                                    }
                                )
                            }
                            entry<Destination.Budget> {
                                val budgetViewModel: BudgetViewModel = viewModel { BudgetViewModel(bankDao) }
                                BudgetScreen(
                                    viewModel = budgetViewModel,
                                    onCreateBudget = { backStack.add(Destination.CreateAccount) }
                                )
                            }
                            entry<Destination.Assets> {
                                val assetViewModel: AssetViewModel = viewModel { AssetViewModel(bankDao) }
                                AssetScreen(
                                    viewModel = assetViewModel,
                                    onCreateAsset = { backStack.add(Destination.CreateAccount) }
                                )
                            }
                            entry<Destination.AIChat> {
                                val aiChatViewModel: AIChatViewModel = viewModel { AIChatViewModel(bankDao, geminiManager) }
                                AIChatScreen(viewModel = aiChatViewModel)
                            }
                            entry<Destination.History> {
                                val historyViewModel: HistoryViewModel = viewModel {
                                    HistoryViewModel(bankDao)
                                }
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.Onboarding> {
                                val onboardingViewModel: OnboardingViewModel = viewModel {
                                    OnboardingViewModel(bankDao, geminiManager)
                                }
                                OnboardingScreen(
                                    viewModel = onboardingViewModel,
                                    onSuccess = {
                                        backStack.clear()
                                        backStack.add(Destination.Dashboard)
                                    }
                                )
                            }
                            entry<Destination.TransactionSimulator> { key ->
                                val transactionViewModel: TransactionViewModel = viewModel {
                                    TransactionViewModel(key.accountId, bankDao)
                                }
                                TransactionScreen(
                                    viewModel = transactionViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.AccountDetail> { key ->
                                val accountDetailViewModel: AccountDetailViewModel = viewModel {
                                    AccountDetailViewModel(key.accountId, bankDao)
                                }
                                AccountDetailScreen(
                                    viewModel = accountDetailViewModel,
                                    onNewTransactionClick = { id ->
                                        backStack.add(Destination.TransactionSimulator(id))
                                    },
                                    onSettingsClick = { id ->
                                        backStack.add(Destination.AccountSettings(id, AccountSettingsType.ACCOUNT))
                                    },
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.LoanDetail> { key ->
                                val loanViewModel: LoanDetailViewModel = viewModel {
                                    LoanDetailViewModel(key.loanId, bankDao)
                                }
                                LoanDetailScreen(
                                    viewModel = loanViewModel,
                                    onSettingsClick = { id ->
                                        backStack.add(Destination.AccountSettings(id, AccountSettingsType.LOAN))
                                    },
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.CreditDetail> { key ->
                                val creditViewModel: CreditDetailViewModel = viewModel {
                                    CreditDetailViewModel(key.revolvingAccountId, bankDao)
                                }
                                CreditDetailScreen(
                                    viewModel = creditViewModel,
                                    onSimulatePurchase = { id ->
                                        backStack.add(Destination.PurchaseSimulator(id))
                                    },
                                    onTransactionClick = { id ->
                                        backStack.add(Destination.BlockedTransactionDetail(id))
                                    },
                                    onSettingsClick = { id ->
                                        backStack.add(Destination.AccountSettings(id, AccountSettingsType.CREDIT_CARD))
                                    },
                                    onInvoiceClick = { id ->
                                        backStack.add(Destination.InvoicePayment(id))
                                    },
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.PurchaseSimulator> { key ->
                                val purchaseViewModel: PurchaseViewModel = viewModel {
                                    PurchaseViewModel(key.cardId, bankDao)
                                }
                                PurchaseScreen(
                                    viewModel = purchaseViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.BlockedTransactionDetail> { key ->
                                val blockedViewModel: BlockedTransactionDetailViewModel = viewModel {
                                    BlockedTransactionDetailViewModel(key.transactionId, bankDao)
                                }
                                BlockedTransactionDetailScreen(
                                    viewModel = blockedViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.AccountSettings> { key ->
                                val settingsViewModel: AccountSettingsViewModel = viewModel {
                                    AccountSettingsViewModel(key.id, key.type, bankDao)
                                }
                                AccountSettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.Settings> {
                                val globalSettingsViewModel: GlobalSettingsViewModel = viewModel {
                                    GlobalSettingsViewModel(bankDao)
                                }
                                SettingsScreen(
                                    viewModel = globalSettingsViewModel
                                )
                            }
                            entry<Destination.CreateAccount> {
                                val createAccountViewModel: CreateAccountViewModel = viewModel {
                                    CreateAccountViewModel(bankDao)
                                }
                                CreateAccountScreen(
                                    viewModel = createAccountViewModel,
                                    onBack = popSafe
                                )
                            }
                            entry<Destination.InvoicePayment> { key ->
                                val invoiceViewModel: InvoicePaymentViewModel = viewModel {
                                    InvoicePaymentViewModel(key.invoiceId, bankDao)
                                }
                                InvoicePaymentScreen(
                                    viewModel = invoiceViewModel,
                                    onBack = popSafe
                                )
                            }
                        }
                    }

                    NavDisplay(
                        backStack = backStack,
                        onBack = popSafe,
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider
                    )
                }
            }
        }
    }
}
