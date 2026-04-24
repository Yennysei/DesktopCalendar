package dev.yenny.calendar.di

operator fun <T> Lazy<T>.invoke(): T {
    return value
}

fun <T> lazyUnsafe(initializer: () -> T): Lazy<T> {
    return lazy(mode = LazyThreadSafetyMode.NONE, initializer = initializer)
}

fun <T> lazySafePublication(initializer: () -> T): Lazy<T> {
    return lazy(mode = LazyThreadSafetyMode.PUBLICATION, initializer = initializer)
}
