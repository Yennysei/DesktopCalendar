package dev.yenny.auth.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object AuthRoute : NavKey

val authSerializersModule: SerializersModule
    get() = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AuthRoute::class, AuthRoute.serializer())
        }
    }

fun EntryProviderScope<NavKey>.productsEntryBuilder() {
    entry<AuthRoute> {
        AuthScreen()
    }
}
