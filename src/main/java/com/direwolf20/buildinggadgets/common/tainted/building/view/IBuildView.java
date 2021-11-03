package com.direwolf20.buildinggadgets.common.tainted.building.view;

import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import net.minecraft.core.BlockPos;

/**
 * A "snapshot" view of a specific buildable TemplateItem providing the ability to iterate over the represented {@link PlacementTarget}'s.
 * It also allows for translating to a specific position via {@link #translateTo(BlockPos)}.<br>
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value.
 * <p>
 * The {@code IBuildView} is constructed given an instance of {@link BuildContext}. This
 * context allows the {@link IBuildView} to adapt itself to the environment in which it is viewed. Therefore no assumptions may be made, that
 * 2 distinct instances of {@code IBuildView} will produce the same results even if they were constructed by the same {@link BuildContext}.
 * <p>
 * All Methods in this class may throw an {@link IllegalStateException} if called after the {@code IBuildView} has been closed.
 *
 * @implSpec Notice that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code IBuildView}.
 * Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface IBuildView extends Iterable<PlacementTarget> {

    /**
     * Translates this {@code IBuildView} to the specified position.
     *
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@code IBuildView}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     * to the specified value.
     */
    IBuildView translateTo(BlockPos pos);

    /**
     * Performs a deep copy of this {@code TemplateView} iterating over all positions if necessary. The resulting {@code TemplateView} should not care about
     * the behaviour of the backing {@link Template} and instead be independent of any resource lock's this {@code TemplateView} imposes as well as not imposing any
     * resource locks itself.
     * <p>
     * Calling
     * {@code
     * IBuildView view = template.createViewInContext(ctx);
     * IBuildView copy = view.copy();
     * view.close();
     * }
     * should ensure that the {@link Template} is no longer restricted because of an open {@code TemplateView}, whilst at the same time providing access to all the positions via
     * {@code copy} in the original {@code TemplateView} in a non-lazy manner.
     * <p>
     * <b>However: be warned that this might require O(n) execution time for this Method on some implementations (with n being the total number of
     * {@link PlacementTarget PlacementTargets} produced) and will almost certainly nullify any benefits that the original {@code TemplateView} may have had by
     * using a lazy implementation.</b>
     *
     * @return A full copy of this {@code TemplateView}. Iterating over the whole {@code TemplateView} if necessary.
     */
    IBuildView copy();

    Region getBoundingBox();

    BuildContext getContext();
}
