package se.banksimulatorn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import se.banksimulatorn.app.data.BankDatabase
import se.banksimulatorn.app.navigation.Destination
import se.banksimulatorn.app.ui.dashboard.DashboardScreen
import se.banksimulatorn.app.ui.dashboard.DashboardViewModel
import se.banksimulatorn.app.ui.transactions.TransactionScreen
import se.banksimulatorn.app.ui.transactions.TransactionViewModel
import se.banksimulatorn.app.ui.history.HistoryScreen
import se.banksimulatorn.app.ui.history.HistoryViewModel
import se.banksimulatorn.app.ui.credits.CreditDetailScreen
import se.banksimulatorn.app.ui.credits.CreditDetailViewModel
import se.banksimulatorn.app.ui.loans.LoanDetailScreen
import se.banksimulatorn.app.ui.loans.LoanDetailViewModel
import se.banksimulatorn.app.ui.purchase.PurchaseScreen
import se.banksimulatorn.app.ui.purchase.PurchaseViewModel
import se.banksimulatorn.app.ui.theme.BankingSimulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = BankDatabase.getDatabase(this)
        val bankDao = database.bankDao()

        setContent {
            BankingSimulatorTheme {
                val backStack = rememberNavBackStack(Destination.Dashboard as NavKey)
                
                val entryProvider = remember {
                    entryProvider<NavKey> {
                        entry<Destination.Dashboard> {
                            val dashboardViewModel: DashboardViewModel = viewModel { 
                                DashboardViewModel(bankDao) 
                            }
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onAccountClick = { id ->
                                    backStack.add(Destination.History(id))
                                },
                                onHistoryClick = {
                                    backStack.add(Destination.History())
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
                                }
                            )
                        }
                        entry<Destination.TransactionSimulator> { key ->
                            val transactionViewModel: TransactionViewModel = viewModel {
                                TransactionViewModel(key.accountId, bankDao)
                            }
                            TransactionScreen(
                                viewModel = transactionViewModel,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Destination.History> { key ->
                            val historyViewModel: HistoryViewModel = viewModel {
                                HistoryViewModel(bankDao)
                            }
                            // Set initial selected account if provided
                            LaunchedEffect(key.accountId) {
                                key.accountId?.let { historyViewModel.selectAccount(it) }
                            }
                            HistoryScreen(
                                viewModel = historyViewModel,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Destination.LoanDetail> { key ->
                            val loanViewModel: LoanDetailViewModel = viewModel {
                                LoanDetailViewModel(key.loanId, bankDao)
                            }
                            LoanDetailScreen(
                                viewModel = loanViewModel,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Destination.CreditDetail> { key ->
                            val creditViewModel: CreditDetailViewModel = viewModel {
                                CreditDetailViewModel(key.cardId, bankDao)
                            }
                            CreditDetailScreen(
                                viewModel = creditViewModel,
                                onSimulatePurchase = { id ->
                                    backStack.add(Destination.PurchaseSimulator(id))
                                },
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Destination.PurchaseSimulator> { key ->
                            val purchaseViewModel: PurchaseViewModel = viewModel {
                                PurchaseViewModel(key.cardId, bankDao)
                            }
                            PurchaseScreen(
                                viewModel = purchaseViewModel,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
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
