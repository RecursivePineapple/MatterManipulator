package matter_manipulator.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.common.Optional;

/// An improved version of [Optional.Method] that works with fields, enum variants, and more. In general: if you can
/// annotate it, it will be properly handled. Note that this does not prevent [NoSuchFieldError] or
/// [NoSuchMethodError]s if removed members are accessed.
/// <br />
/// This system also supports checking for several mods, whereas [Optional.Method] can only check one.
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.FIELD, ElementType.METHOD
})
public @interface ModInterop {

    /// The list of mod IDs to check. Mod IDs can be prefixed with a `!` to require that the mod is missing instead of
    /// present. If any mods are missing, the member will be removed.
    String[] value();
}
