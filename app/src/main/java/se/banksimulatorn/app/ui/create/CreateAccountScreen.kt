package se.banksimulatorn.app.ui.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.*
import se.banksimulatorn.app.navigation.CreateType
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    initialType: CreateType = CreateType.ACCOUNT,
    onBack: () -> Unit
) {
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    val isSaving by viewModel.isSaving.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is CreateUiEvent.Success) {
                onBack()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.open_new)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreateType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(stringResource(type.labelRes())) }
                    )
                }
            }

            when (selectedType) {
                CreateType.ACCOUNT -> BankAccountForm(isSaving = isSaving, onSave = viewModel::createBankAccount)
                CreateType.CREDIT -> RevolvingCreditForm(isSaving = isSaving, onSave = viewModel::createRevolvingCredit)
                CreateType.LOAN -> LoanForm(accounts = accounts, isSaving = isSaving, onSave = viewModel::createLoan)
                CreateType.ASSET -> AssetForm(onSave = viewModel::createAsset)
                CreateType.BUDGET -> {
                    val credits by viewModel.allRevolvingCredits.collectAsState()
                    BudgetForm(accounts = accounts, credits = credits, onSave = viewModel::createBudgetItem)
                }
            }
        }
    }
}

private fun CreateType.labelRes(): Int = when (this) {
    CreateType.ACCOUNT -> R.string.accounts
    CreateType.CREDIT -> R.string.credits
    CreateType.LOAN -> R.string.loans
    CreateType.ASSET -> R.string.assets
    CreateType.BUDGET -> R.string.budget
}

@Composable
private fun FieldError.message(): String = stringResource(
    when (this) {
        FieldError.REQUIRED -> R.string.error_required
        FieldError.INVALID_NUMBER -> R.string.error_invalid_number
        FieldError.MUST_BE_POSITIVE -> R.string.error_must_be_positive
        FieldError.MUST_NOT_BE_NEGATIVE -> R.string.error_must_not_be_negative
        FieldError.RATE_OUT_OF_RANGE -> R.string.error_rate_range
        FieldError.DAY_OUT_OF_RANGE -> R.string.error_day_range
        FieldError.MONTHS_OUT_OF_RANGE -> R.string.error_months_range
    }
)

/**
 * A text field that only shows its validation error once the user has tried to
 * submit, so an empty form doesn't open covered in red.
 */
@Composable
private fun ValidatedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: FieldError?,
    showErrors: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null
) {
    val visibleError = error?.takeIf { showErrors }
    val supportingMessage: String? = if (visibleError != null) visibleError.message() else supportingText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = visibleError != null,
        supportingText = if (supportingMessage != null) {
            { Text(supportingMessage) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SubmitButton(text: String, isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}

@Composable
private fun ChoiceRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun BankAccountForm(
    isSaving: Boolean,
    onSave: (name: String, number: String, openingBalance: Double, type: AccountType, interestRate: Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(AccountType.CHECKING) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = CreateFormValidation.name(name)
    val balanceError = CreateFormValidation.optionalNonNegativeAmount(balance)
    val rateError = CreateFormValidation.optionalRate(rate)

    Text(stringResource(R.string.account_type), style = MaterialTheme.typography.labelLarge)
    Column(Modifier.selectableGroup()) {
        ChoiceRow(type == AccountType.CHECKING, stringResource(R.string.private_account)) { type = AccountType.CHECKING }
        ChoiceRow(type == AccountType.SAVINGS, stringResource(R.string.savings_account)) { type = AccountType.SAVINGS }
    }

    ValidatedField(name, { name = it }, stringResource(R.string.field_name), nameError, showErrors)
    ValidatedField(
        number, { number = it }, stringResource(R.string.field_account_number_optional), null, showErrors,
        keyboardType = KeyboardType.Number,
        supportingText = stringResource(R.string.account_number_hint)
    )
    ValidatedField(balance, { balance = it }, stringResource(R.string.field_opening_balance), balanceError, showErrors, KeyboardType.Decimal)
    ValidatedField(rate, { rate = it }, stringResource(R.string.field_interest_rate), rateError, showErrors, KeyboardType.Decimal)

    SubmitButton(stringResource(R.string.open_account_button), isSaving) {
        showErrors = true
        if (nameError == null && balanceError == null && rateError == null) {
            onSave(
                name,
                number,
                CreateFormValidation.parseAmount(balance) ?: 0.0,
                type,
                CreateFormValidation.parseAmount(rate) ?: 0.0
            )
        }
    }
}

@Composable
fun RevolvingCreditForm(
    isSaving: Boolean,
    onSave: (name: String, limit: Double, rate: Double, statementDay: Int, issueCard: Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var limit by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var statementDay by rememberSaveable { mutableStateOf("25") }
    var issueCard by rememberSaveable { mutableStateOf(true) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = CreateFormValidation.name(name)
    val limitError = CreateFormValidation.positiveAmount(limit)
    val rateError = CreateFormValidation.optionalRate(rate)
    val dayError = CreateFormValidation.dayOfMonth(statementDay)

    ValidatedField(name, { name = it }, stringResource(R.string.field_name), nameError, showErrors)
    ValidatedField(limit, { limit = it }, stringResource(R.string.field_credit_limit), limitError, showErrors, KeyboardType.Decimal)
    ValidatedField(rate, { rate = it }, stringResource(R.string.field_interest_rate), rateError, showErrors, KeyboardType.Decimal)
    ValidatedField(statementDay, { statementDay = it }, stringResource(R.string.field_statement_day), dayError, showErrors, KeyboardType.Number)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = issueCard, onValueChange = { issueCard = it }, role = Role.Switch)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.issue_credit_card), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.issue_credit_card_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = issueCard, onCheckedChange = null)
    }

    SubmitButton(stringResource(R.string.open_credit_button), isSaving) {
        showErrors = true
        if (nameError == null && limitError == null && rateError == null && dayError == null) {
            onSave(
                name,
                CreateFormValidation.parseAmount(limit) ?: 0.0,
                CreateFormValidation.parseAmount(rate) ?: 0.0,
                statementDay.trim().toInt(),
                issueCard
            )
        }
    }
}

/** Loan types are stored as these English keys; the dashboard localizes the known ones. */
private val loanTypes = listOf(
    "Loan" to R.string.loan_type_personal,
    "Mortgage loan" to R.string.mortgage_loan,
    "Car loan" to R.string.loan_type_car
)

@Composable
fun LoanForm(
    accounts: List<Account>,
    isSaving: Boolean,
    onSave: (
        name: String,
        loanType: String,
        amount: Double,
        months: Int,
        monthlyFee: Double,
        cycleDay: Int,
        payoutAccountId: Int?
    ) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var loanType by rememberSaveable { mutableStateOf(loanTypes.first().first) }
    var amount by rememberSaveable { mutableStateOf("") }
    var months by rememberSaveable { mutableStateOf("60") }
    var fee by rememberSaveable { mutableStateOf("") }
    var cycleDay by rememberSaveable { mutableStateOf("27") }
    // -1 = not chosen yet (defaults to the first account), 0 = don't pay out.
    var payoutChoice by rememberSaveable { mutableStateOf(-1) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val payoutAccountId: Int? = when (payoutChoice) {
        -1 -> accounts.firstOrNull()?.id
        0 -> null
        else -> payoutChoice.takeIf { id -> accounts.any { it.id == id } }
    }

    val nameError = CreateFormValidation.name(name)
    val amountError = CreateFormValidation.positiveAmount(amount)
    val monthsError = CreateFormValidation.loanMonths(months)
    val feeError = CreateFormValidation.optionalNonNegativeAmount(fee)
    val dayError = CreateFormValidation.dayOfMonth(cycleDay)

    Text(stringResource(R.string.loan_type), style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        loanTypes.forEach { (key, label) ->
            FilterChip(
                selected = loanType == key,
                onClick = { loanType = key },
                label = { Text(stringResource(label)) }
            )
        }
    }

    ValidatedField(name, { name = it }, stringResource(R.string.field_name), nameError, showErrors)
    ValidatedField(amount, { amount = it }, stringResource(R.string.field_loan_amount), amountError, showErrors, KeyboardType.Decimal)

    val parsedAmount = CreateFormValidation.parseAmount(amount)
    val parsedMonths = months.trim().toIntOrNull()
    val amortization = if (amountError == null && monthsError == null && parsedAmount != null && parsedMonths != null) {
        stringResource(
            R.string.loan_monthly_amortization,
            NumberFormat.getNumberInstance().apply { maximumFractionDigits = 0 }.format(parsedAmount / parsedMonths)
        )
    } else null
    ValidatedField(
        months, { months = it }, stringResource(R.string.field_repayment_months), monthsError, showErrors,
        keyboardType = KeyboardType.Number,
        supportingText = amortization
    )
    ValidatedField(fee, { fee = it }, stringResource(R.string.field_monthly_fee), feeError, showErrors, KeyboardType.Decimal)
    ValidatedField(cycleDay, { cycleDay = it }, stringResource(R.string.field_invoice_day), dayError, showErrors, KeyboardType.Number)

    Text(stringResource(R.string.loan_payout_to), style = MaterialTheme.typography.labelLarge)
    Column(Modifier.selectableGroup()) {
        accounts.forEach { account ->
            ChoiceRow(payoutAccountId == account.id, account.name) { payoutChoice = account.id }
        }
        ChoiceRow(payoutAccountId == null, stringResource(R.string.loan_no_payout)) { payoutChoice = 0 }
    }

    SubmitButton(stringResource(R.string.open_loan_button), isSaving) {
        showErrors = true
        if (nameError == null && amountError == null && monthsError == null && feeError == null && dayError == null) {
            onSave(
                name,
                loanType,
                parsedAmount ?: 0.0,
                parsedMonths ?: 1,
                CreateFormValidation.parseAmount(fee) ?: 0.0,
                cycleDay.trim().toInt(),
                payoutAccountId
            )
        }
    }
}

@Composable
fun AssetForm(onSave: (String, AssetType, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.VILLA) }
    var value by remember { mutableStateOf("") }
    var growth by remember { mutableStateOf("0") }

    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Asset Name") }, modifier = Modifier.fillMaxWidth())
    
    Text("Type", style = MaterialTheme.typography.labelLarge)
    Row {
        AssetType.values().filter { it != AssetType.OTHER }.forEach { t ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = t }.padding(8.dp)) {
                RadioButton(selected = type == t, onClick = { type = t })
                Text(t.name)
            }
        }
    }

    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Initial Value") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    OutlinedTextField(value = growth, onValueChange = { growth = it }, label = { Text("Monthly Growth (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

    Button(onClick = { onSave(name, type, value.toDoubleOrNull() ?: 0.0, growth.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
        Text("Add Asset")
    }
}

@Composable
fun BudgetForm(
    accounts: List<Account>,
    credits: List<RevolvingCreditAccount>,
    onSave: (String, Double, BudgetType, BudgetFrequency, PaymentMethod, Int?, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(BudgetType.EXPENSE) }
    var freq by remember { mutableStateOf(BudgetFrequency.MONTHLY) }
    var method by remember { mutableStateOf(PaymentMethod.DIRECT_DEBIT) }
    var targetId by remember { mutableStateOf<Int?>(null) }

    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    
    Row {
        RadioButton(selected = type == BudgetType.INCOME, onClick = { type = BudgetType.INCOME })
        Text("Income")
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = type == BudgetType.EXPENSE, onClick = { type = BudgetType.EXPENSE })
        Text("Expense")
    }

    if (type == BudgetType.INCOME) {
        Text("Target Account")
        accounts.forEach { acc ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { targetId = acc.id }) {
                RadioButton(selected = targetId == acc.id, onClick = { targetId = acc.id })
                Text(acc.name)
            }
        }
    } else {
        Text("Payment Method")
        Row {
            PaymentMethod.values().forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { method = m }.padding(4.dp)) {
                    RadioButton(selected = method == m, onClick = { method = m })
                    Text(m.name.replace("_", " "))
                }
            }
        }
    }

    Button(onClick = { 
        onSave(name, amount.toDoubleOrNull() ?: 0.0, type, freq, method, if (type == BudgetType.INCOME) targetId else null, if (method == PaymentMethod.CREDIT_CARD) targetId else null)
    }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
        Text("Add Budget Item")
    }
}
