package com.xmlapplication.features.dashboard

data class DashboardItem(
    val id: String,
    val title: String,
    val description: String,
    val endpointTag: String,
    val iconRes: Int
)

object DashboardData {
    fun items(): List<DashboardItem> = listOf(
        DashboardItem(
            id = "products",
            title = "Products",
            description = "Browse products and product information",
            endpointTag = "/products",
            iconRes = com.xmlapplication.R.drawable.ic_inventory
        ),
        DashboardItem(
            id = "users",
            title = "Users",
            description = "Explore user profiles and account data",
            endpointTag = "/users",
            iconRes = com.xmlapplication.R.drawable.ic_people
        ),
        DashboardItem(
            id = "carts",
            title = "Carts",
            description = "View shopping carts and cart items",
            endpointTag = "/carts",
            iconRes = com.xmlapplication.R.drawable.ic_shopping_cart
        ),
        DashboardItem(
            id = "posts",
            title = "Posts",
            description = "Explore posts and content",
            endpointTag = "/posts",
            iconRes = com.xmlapplication.R.drawable.ic_article
        )
    )
}