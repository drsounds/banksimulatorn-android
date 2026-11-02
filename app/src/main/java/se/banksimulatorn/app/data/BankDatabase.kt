package se.banksimulatorn.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Account::class, 
        Transaction::class, 
        Loan::class, 
        CreditCard::class, 
        TimeSettings::class,
        RevolvingCreditAccount::class,
        GlobalSettings::class,
        Invoice::class,
        RecurringTask::class,
        Asset::class,
        BudgetItem::class,
        Persona::class
    ],
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BankDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao

    companion object {
        /**
         * Adds overdraft fee settings and reminder links without wiping the user's
         * simulation. Also repairs credit lines that the old payment code pushed
         * below zero (paying fees and interest reduced the used credit too).
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN overdraftFee REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN overdraftFeeThreshold REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN overdraftFeeCoolingDays INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE accounts ADD COLUMN lastOverdraftFeeAt INTEGER")
                db.execSQL("ALTER TABLE invoices ADD COLUMN relatedInvoiceId INTEGER")
                db.execSQL("UPDATE revolving_credit_accounts SET usedCredit = 0 WHERE usedCredit < 0")
            }
        }

        @Volatile
        private var INSTANCE: BankDatabase? = null

        fun getDatabase(context: Context): BankDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BankDatabase::class.java,
                    "bank_database"
                )
                .addMigrations(MIGRATION_15_16)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
