object Generators {
    private fun getRandomUppercaseString(length: Int): String {
        val allowedChars = ('A'..'Z')
        return generateRandomString(allowedChars, length)
    }

    private fun getRandomNumberString(length: Int): String {
        val allowedChars = ('0'..'9')
        return generateRandomString(allowedChars, length)
    }

    private fun generateRandomString(allowedChars: CharRange, length: Int): String =
        (1..length)
            .map { allowedChars.random() }
            .joinToString("")

    fun generateISIN(): ISIN = ISIN.create(getRandomUppercaseString(2) + getRandomNumberString(10))
}