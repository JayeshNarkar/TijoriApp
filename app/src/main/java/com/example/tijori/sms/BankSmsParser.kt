package com.example.tijori.sms

import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Double,
    val isDebit: Boolean,
    val merchantOrPayee: String?,
    val upiTransactionId: String?
)

object BankSmsParser {

    // Debit: "debited for Rs 99255.00"
    private val debitAmountPattern = Pattern.compile(
        "debited\\s+for\\s+(?:Rs\\.?|INR)\\s*([\\d,]+\\.?\\d*)",
        Pattern.CASE_INSENSITIVE
    )

    // Credit: "credited with Rs 200.00"
    private val creditAmountPattern = Pattern.compile(
        "credited\\s+with\\s+(?:Rs\\.?|INR)\\s*([\\d,]+\\.?\\d*)",
        Pattern.CASE_INSENSITIVE
    )

    // Debit payee: "; XYZ credited"
    private val debitPayeePattern = Pattern.compile(
        ";\\s*(.+?)\\s+credited",
        Pattern.CASE_INSENSITIVE
    )

    // Credit payer: "from JAYESH NILESH N. UPI:..." — capture up to the period before UPI
    private val creditPayerPattern = Pattern.compile(
        "from\\s+(.+?)\\.\\s*UPI",
        Pattern.CASE_INSENSITIVE
    )

    // Matches the numeric UPI reference right after "UPI:", stopping before any
    // trailing "-BankName" suffix that credit messages sometimes append.
    private val upiIdPattern = Pattern.compile(
        "UPI:(\\d+)",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(sender: String, body: String): ParsedTransaction? {
        val looksLikeBankSender = sender.contains("ICICI", ignoreCase = true) ||
                sender.contains("HDFC", ignoreCase = true) ||
                sender.contains("SBI", ignoreCase = true) ||
                sender.matches(Regex("^[A-Z]{2}-[A-Z0-9]+(-[A-Z])?$"))

        if (!looksLikeBankSender) return null

        val upiId = upiIdPattern.matcher(body).let { if (it.find()) it.group(1) else null }

        val debitMatcher = debitAmountPattern.matcher(body)
        if (debitMatcher.find()) {
            val amount = debitMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
            val payeeMatcher = debitPayeePattern.matcher(body)
            val payee = if (payeeMatcher.find()) payeeMatcher.group(1)?.trim() else null

            return ParsedTransaction(
                amount = amount,
                isDebit = true,
                merchantOrPayee = payee,
                upiTransactionId = upiId
            )
        }

        val creditMatcher = creditAmountPattern.matcher(body)
        if (creditMatcher.find()) {
            val amount = creditMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
            val payerMatcher = creditPayerPattern.matcher(body)
            val payer = if (payerMatcher.find()) payerMatcher.group(1)?.trim() else null

            return ParsedTransaction(
                amount = amount,
                isDebit = false,
                merchantOrPayee = payer,
                upiTransactionId = upiId
            )
        }

        return null // doesn't match either known pattern
    }
}