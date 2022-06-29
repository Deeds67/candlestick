package repositories

import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet

object DBUtils {
    /**
     * Helper function to more easily use plain sql using jetbrains exposed.
     * WARNING: user input should be sanitized to avoid sql injection.
     * Source: https://github.com/JetBrains/Exposed/issues/118#issuecomment-308453023
     */
    fun <T> String.execAndMap(transform: (ResultSet) -> T): List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }
}