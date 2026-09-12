package com.example.tijori.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.tijori.data.dao.TransactionDao
import com.example.tijori.data.dao.UserDao
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.TransactionSource
import com.example.tijori.data.entities.TransactionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var transactionDao: TransactionDao
    @Inject lateinit var userDao: UserDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = userDao.getCurrentUserOnce() ?: return@launch // nobody logged in

                for (message in messages) {
                    val sender = message.originatingAddress ?: continue
                    val body = message.messageBody ?: continue

                    val parsed = BankSmsParser.parse(sender, body) ?: continue

                    // Skip if this exact UPI transaction was already recorded —
                    // banks occasionally resend the same SMS.
                    if (parsed.upiTransactionId != null) {
                        val existing = transactionDao.findByUpiTransactionId(parsed.upiTransactionId)
                        if (existing != null) continue
                    }

                    transactionDao.insert(
                        Transaction(
                            userId = currentUser.id,
                            amount = parsed.amount,
                            note = parsed.merchantOrPayee,
                            source = TransactionSource.SMS_AUTO,
                            needsReview = true,
                            type = if (parsed.isDebit) TransactionType.DEBIT else TransactionType.CREDIT,
                            upiTransactionId = parsed.upiTransactionId
                        )
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}