package com.example.diceroller.constants

object RegexConstants {
    const val phoneNumberRegex: String = "((\\+*)((0[ -]*)*|((91 )*))((\\d{12})+|(\\d{10})+))|\\d{5}([- ]*)\\d{6}"
    const val emailRegex: String = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
}