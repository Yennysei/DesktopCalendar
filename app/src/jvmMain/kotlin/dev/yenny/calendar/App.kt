package dev.yenny.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.yenny.auth.ui.AuthRoute
import dev.yenny.auth.ui.authSerializersModule
import dev.yenny.auth.ui.productsEntryBuilder
import dev.yenny.core.ui.CalendarTheme


private val config = SavedStateConfiguration {
    serializersModule = authSerializersModule
}

private val entryBuilders: Set<EntryProviderScope<NavKey>.() -> Unit> = setOf { productsEntryBuilder() }

@Composable
@Preview
fun App() {
    CalendarTheme {
        val backStack = rememberNavBackStack(config, AuthRoute)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entryBuilders.forEach { builder -> this.builder() }
            },
        )
    }
}
