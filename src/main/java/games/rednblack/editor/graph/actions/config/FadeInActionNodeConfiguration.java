package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Float;
import static games.rednblack.editor.graph.actions.ActionFieldType.*;

public class FadeInActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public FadeInActionNodeConfiguration() {
        super("FadeInAction", "Fade In", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("duration", "Duration", true, Float, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("interpolation", "Interpolation", false, Interpolation, Param));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}