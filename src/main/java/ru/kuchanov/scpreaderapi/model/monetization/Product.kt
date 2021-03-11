package ru.kuchanov.scpreaderapi.model.monetization

data class Product(
        private val productType: InappType,
        private val store: Store
)

enum class InappType(val index: Int) {
    INAPP(0), CONSUMABLE(1), SUBS(2)
}

enum class Store {
    HUAWEI, GOOGLE, AMAZON, APPLE
}