package services

import Candlestick

interface CandlestickManager {
    fun getCandlesticks(isin: String): List<Candlestick>
}