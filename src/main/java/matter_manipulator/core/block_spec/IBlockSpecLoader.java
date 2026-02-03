package matter_manipulator.core.block_spec;

import com.google.gson.JsonElement;

/// Something that can load a block spec from json and vice versa. The external storage format for specs can be thought
/// of as a tagged union. The format looks something like the following.
/// ```json
/// {
///   // not controlled within this system - this may be anything as loaders are invoked via a JsonSerializer/JsonDeserializer
///   "cables": {
///     // from getKey()
///     "block": {
///       // the generic data this loader interacts with
///       "state": "minecraft:stairs{facing=down,half=bottom,shape=straight}"
///     }
///   }
/// }
/// ```
public interface IBlockSpecLoader {

    /// A short string that uniquely identifies this loader. Note that this is attached to all block specs that this
    /// loader is associated with, so please keep it terse.
    String getKey();

    IBlockSpec load(JsonElement element);

    JsonElement save(IBlockSpec spec);
}
