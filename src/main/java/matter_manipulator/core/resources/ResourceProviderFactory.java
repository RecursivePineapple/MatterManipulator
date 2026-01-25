package matter_manipulator.core.resources;

import matter_manipulator.core.context.ManipulatorContext;

/// A ResourceProviderFactory is something that can examine a manipulator's context and produce an object that can
/// extract or insert a specific type of resource. Any implementation details beyond this are undefined, and each
/// [Resource] will have its own mechanism for registering sub-providers, assuming such a concept makes sense for that
/// resource.
public interface ResourceProviderFactory<Provider extends ResourceProvider> {

    /// Returns the resource that is associated with this factory.
    Resource<Provider> getResource();

    /// Returns true when the given stack is something that can be passed to the [ResourceProvider#extract(Object)] or
    /// [ResourceProvider#insert(Object)] methods of the provider returned by [#createProvider(ManipulatorContext)].
    boolean supports(Object stack);

    /// Checks if two resources are identical, including the stack size/amount/etc.
    boolean areEqual(Object a, Object b);

    /// Converts a stack to its localized form, to be shown to the player.
    String getLocalizedName(Object stack, int multiplier);

    /// Performs whatever logic is necessary to create the [ResourceProvider] from a [ManipulatorContext].
    Provider createProvider(ManipulatorContext context);
}
