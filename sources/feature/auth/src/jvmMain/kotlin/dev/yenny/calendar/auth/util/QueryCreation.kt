package dev.yenny.calendar.auth.util

internal fun buildQueryParameters(vararg queryParameters: Pair<String, String>): String {
    return StringBuilder().apply {
        queryParameters.forEachIndexed { index, keyValue ->
            addQueryParameter(key = keyValue.first, value = keyValue.second, isFirst = index == 0)
        }
    }.toString()
}

private fun StringBuilder.addQueryParameter(key: String, value: String, isFirst: Boolean) {
    if (!isFirst) append('&')

    append(key)
    append('=')
    append(value)
}
