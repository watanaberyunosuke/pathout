package com.monash.pathout.util

import java.util.regex.Pattern

class Validator {

    fun emailValidator(email:String): Boolean {

        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    fun phoneValidator(phone:String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }
}