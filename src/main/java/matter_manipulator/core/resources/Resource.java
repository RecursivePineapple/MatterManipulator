package matter_manipulator.core.resources;

/// This is an opaque handle that represents some type of resource that a manipulator can provide or accept. Any details
/// about the implementation are undefined, as there is no easy or elegant way to abstract these into a common
/// interface.
/// <p />
/// [Resource] objects are singletons exposed by an API. The specifics of this API are undefined, but it will typically
/// be a static field or static getter method.
/// <p />
/// [ResourceProvider]s are objects created by a [ResourceProviderFactory]. These objects are created on-demand, when
/// something extracts or inserts a resource from or into a manipulator. They are persisted within the manipulator's
/// state object until the state object gets garbage collected.
/// <p />
/// There is no explicit 'flush' or 'save' step - all operations done on these interfaces must be atomic and must
/// immediately update the world.
public interface Resource<Provider extends ResourceProvider> {

}
