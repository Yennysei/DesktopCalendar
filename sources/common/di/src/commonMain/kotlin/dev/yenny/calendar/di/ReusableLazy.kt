package dev.yenny.calendar.di

import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

fun <T> unsafeReusable(initializer: () -> T): Lazy<T> {
    return ReusableUnsafeLazyImpl(initializer = initializer)
}

fun <T> safePublicationReusable(
    retriesCount: Int = 3,
    initializer: () -> T,
): Lazy<T> {
    return ReusableSafePublicationLazyImpl(
        retriesCount = retriesCount,
        initializer = initializer
    )
}

fun <T> synchronizedReusable(
    lock: Any? = null,
    initializer: () -> T,
): Lazy<T> {
    return ReusableSynchronizedLazyImpl(lock = lock, initializer = initializer)
}

private object Uninitialized

private class ReusableUnsafeLazyImpl<out T>(
    private val initializer: () -> T,
) : Lazy<T> {

    private var _reference: Any? = Uninitialized

    override val value: T
        get() {
            @Suppress("UNCHECKED_CAST")
            when (val reference = _reference) {
                null -> return null as T
                is WeakReference<*> -> reference.get()?.let { return it as T }
            }

            return initializer().also { newValue ->
                _reference = newValue?.let { WeakReference(it) }
            }
        }

    override fun isInitialized(): Boolean = _reference !== Uninitialized

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}


private class ReusableSafePublicationLazyImpl<T>(
    private val retriesCount: Int = 3,
    private val initializer: () -> T,
) : Lazy<T> {

    @Volatile
    private var _reference: Any? = Uninitialized

    // Artificial final field to ensure safe publication of 'SafePublicationLazyImpl' itself through
    // var lazy = lazy() {}
    @Suppress("unused")
    private val final: Any = Uninitialized

    override val value: T
        get() {
            @Suppress("UNCHECKED_CAST")
            return when (val reference = _reference) {
                null -> null
                is WeakReference<*> -> reference.get() ?: initializeValue(reference)
                else -> initializeValue(reference)
            } as T
        }

    override fun isInitialized(): Boolean = _reference !== Uninitialized

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    private fun initializeValue(
        reference: Any?,
        currentRetry: Int = 0,
    ): T {
        val newValue = initializer()
        val wrappedValue = newValue?.let { WeakReference(it) }

        if (valueUpdater.compareAndSet(this, reference, wrappedValue)) {
            return newValue
        }

        @Suppress("UNCHECKED_CAST")
        val updatedReference = _reference as? WeakReference<T> ?: return null as T
        val returnedValue = updatedReference.get()

        if (returnedValue == null) {
            if (currentRetry >= retriesCount) {
                error("Could not initialize property after $retriesCount retries.")
            }

            return initializeValue(reference = updatedReference, currentRetry = currentRetry + 1)
        }

        return returnedValue
    }

    private companion object {

        private val valueUpdater = AtomicReferenceFieldUpdater.newUpdater(
            ReusableSafePublicationLazyImpl::class.java,
            Any::class.java,
            "_reference"
        )
    }
}

private class ReusableSynchronizedLazyImpl<out T>(
    lock: Any? = null,
    private val initializer: () -> T,
) : Lazy<T> {

    @Volatile
    private var _reference: Any? = Uninitialized

    // final field to ensure safe publication of 'SynchronizedLazyImpl' itself through
    // var lazy = lazy() {}
    private val lock = lock ?: this

    override val value: T
        get() {
            @Suppress("UNCHECKED_CAST")
            when (val reference = _reference) {
                null -> return null as T
                is WeakReference<*> -> reference.get()?.let { return it as T }
            }

            return synchronized(lock) {
                @Suppress("UNCHECKED_CAST")
                when (val reference = _reference) {
                    null -> return null as T
                    is WeakReference<*> -> reference.get()?.let { return it as T }
                }

                initializer().also { newValue ->
                    _reference = newValue?.let { WeakReference(it) }
                }
            }
        }

    override fun isInitialized(): Boolean = _reference !== Uninitialized

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}
