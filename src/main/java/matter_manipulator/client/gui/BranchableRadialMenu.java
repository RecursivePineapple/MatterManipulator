package matter_manipulator.client.gui;

import matter_manipulator.client.gui.RadialMenuBuilder.RadialMenuOptionBuilderBranch;
import matter_manipulator.client.gui.RadialMenuBuilder.RadialMenuOptionBuilderLeaf;

public interface BranchableRadialMenu {

    RadialMenuOptionBuilderLeaf<?> option();

    RadialMenuOptionBuilderBranch<?> branch();
}
