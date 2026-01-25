package matter_manipulator.core.misc;

import net.minecraft.util.math.BlockPos;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.core.i18n.Localized;

@Desugar
public record BuildFeedback(BlockPos pos, Localized message, FeedbackSeverity severity) {

}
