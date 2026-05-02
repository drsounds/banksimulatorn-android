package se.banksimulatorn.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BankDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao

    companion object {
        @Volatile
        private var INSTANCE: BankDatabase? = null

        fun getDatabase(context: Context): BankDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BankDatabase::class.java,
                    "bank_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
