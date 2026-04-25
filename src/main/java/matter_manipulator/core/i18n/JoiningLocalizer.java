package matter_manipulator.core.i18n;

import java.util.Arrays;
import java.util.Collection;

import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.i18n.Localized.ArgProcessor;

public class JoiningLocalizer implements ILocalizer {

    public static final JoiningLocalizer NOTHING = new JoiningLocalizer("");
    public static final JoiningLocalizer COLONS = new JoiningLocalizer(": ");

    public final String separator;

    public JoiningLocalizer(String separator) {
        this.separator = separator;
    }

    @Override
    public String localize(ArgProcessor argProcessor, Object[] args) {
        args = argProcessor.process(args);

        //noinspection unchecked
        return DataUtils.join(separator, (Collection<String>) (Object) Arrays.asList(args));
    }
}
