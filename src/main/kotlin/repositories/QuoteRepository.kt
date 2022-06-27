package repositories

import Quote

interface QuoteRepository {
    fun createQuote(quote: Quote): Int
}