package matter_manipulator.common.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.util.math.BlockPos;

import matter_manipulator.GlobalMMConfig;
import matter_manipulator.common.networking.MMActionWithPayload;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.misc.FeedbackSeverity;

public class BuildStatusTracker {

    public static final HashMap<BlockPos, BuildFeedback> FEEDBACK = new HashMap<>(0);
    public static final List<BuildFeedback> SORTED_FEEDBACK = new ArrayList<>(0);
    public static long statusExpiration = 0;

    public static final MMActionWithPayload<List<BuildFeedback>> SYNC_FEEDBACK = MMActionWithPayload.client(
        "feedback",
        (player, buildFeedbacks) -> {
            FEEDBACK.clear();
            buildFeedbacks.forEach(feedback -> FEEDBACK.put(feedback.pos(), feedback));

            SORTED_FEEDBACK.clear();
            SORTED_FEEDBACK.addAll(buildFeedbacks);

            if (GlobalMMConfig.RenderingConfig.statusExpiration <= 0) {
                statusExpiration = 0;
            } else {
                statusExpiration = System.currentTimeMillis() + GlobalMMConfig.RenderingConfig.statusExpiration * 1000;
            }
        },
        (buffer, value) -> {
            buffer.writeList(value, (buffer2, feedback) -> {
                buffer2.writeBlockPos(feedback.pos());
                feedback.message().encode(buffer2);
                buffer2.writeVarInt(feedback.severity().ordinal());
            });
        },
        buffer -> {
            return buffer.readList(buffer2 -> {
                return new BuildFeedback(
                    buffer2.readBlockPos(),
                    new Localized().decode(buffer2),
                    FeedbackSeverity.values()[buffer2.readVarInt()]
                );
            });
        });

    public static boolean hasExpired() {
        return statusExpiration > 0 && System.currentTimeMillis() > statusExpiration;
    }
}
