package com.humara.nagar.utils

object StringUtils {
    /**
     * Function which returns a modified string with consecutive whitespace characters like space, tab and enter replaced by a single space for [input] string.
     */
    fun replaceWhitespaces(input: String): String {
        return input.replace("\\s+".toRegex(), " ")
    }

    fun toStringWithoutSpaces(inputString: String): String {
        return inputString.filter { it.isLetterOrDigit() }
    }

    fun showDotStringAfterLimitReached(length: Int, string: String): String {
        if (string.length <= length) {
            return string
        }
        return "${string.substring(0, length)}..."
    }
}