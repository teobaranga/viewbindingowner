package com.teobaranga.viewbindingowner

import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Interface implemented by classes that are able to create and retrieve [ViewBinding] instances.
 */
interface ViewBindingProvider {
    /**
     * Retrieve a property delegate that can provide a [ViewBinding] of the specified type.
     *
     * Attempting to access the [ViewBinding] before the view has been created or after it was destroyed
     * will result in an [IllegalStateException].
     *
     * @param viewBindingClass the requested [ViewBinding] subtype
     */
    fun <T : ViewBinding> get(viewBindingClass: KClass<T>): ReadOnlyProperty<ViewBindingOwner, T>
}
