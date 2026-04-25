package matter_manipulator.core.i18n;

import matter_manipulator.core.i18n.Localized.ArgProcessor;

public interface ILocalizer {

    String localize(ArgProcessor argProcessor, Object[] args);

}
