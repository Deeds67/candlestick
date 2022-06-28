package services

import Candlestick
import ISIN

interface CandlestickManager {
    fun getCandlesticks(isin: ISIN): List<Candlestick>
}

class CandlestickManagerImpl: CandlestickManager {
    override fun getCandlesticks(isin: ISIN): List<Candlestick> {
        return listOf()
    }
}