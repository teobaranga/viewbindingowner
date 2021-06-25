package com.teobaranga.viewbindingowner

import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty

/**
 * Interface that indicates an owner of a [ViewBinding] layout.
 *
 * Since [ViewBinding] can be created in multiple ways, this interface holds a [ViewBindingProvider]
 * that knows how to retrieve and create an instance.
 */
interface ViewBindingOwner {
    /**
     * The [ViewBindingProvider] responsible for retrieving and creating instances of [ViewBinding].
     */
    val viewBindingProvider: ViewBindingProvider
}

/**
 * Returns a property delegate to access a [ViewBinding] available from the current [ViewBindingOwner]:
 *
 * ```
 * class MainActivity : FragmentActivity(), ViewBindingOwner {
 *     val binding by viewBinding<MainActivityBinding>()
 * }
 * ```
 *
 * Attempting to access the [ViewBinding] before the view has been created or after it was destroyed
 * will result in an [IllegalStateException].
 */
inline fun <reified T : ViewBinding> ViewBindingOwner.viewBinding(): ReadOnlyProperty<ViewBindingOwner, T> {
    return viewBindingProvider.get(T::class)
}
