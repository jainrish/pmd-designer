/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.controls;

import static net.sourceforge.pmd.internal.util.IteratorUtil.toIterable;
import static net.sourceforge.pmd.util.fxdesigner.util.DesignerIteratorUtil.parentIterator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.reactfx.EventStreams;
import org.reactfx.Subscription;
import org.reactfx.value.Var;

import net.sourceforge.pmd.lang.ast.Node;

import javafx.scene.control.SelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;


/**
 * @author Clément Fournier
 * @since 7.0.0
 */
public class ASTTreeView extends TreeView<Node> {


    private final Var<Consumer<Node>> onNodeClickedHandler = Var.newSimpleVar(n -> {});
    private final TreeViewWrapper<Node> myWrapper = new TreeViewWrapper<>(this);

    private Subscription myNodeSelectedSub;
    private ASTTreeItem selectedTreeItem;


    public ASTTreeView() {

        onNodeClickedHandler.values()
                            .subscribe(handler -> {
                                setCellFactory(tv -> new ASTTreeCell(handler));

                                Optional.ofNullable(myNodeSelectedSub).ifPresent(Subscription::unsubscribe);

                                myNodeSelectedSub = EventStreams.valuesOf(getSelectionModel().selectedItemProperty())
                                                                .filterMap(Objects::nonNull, TreeItem::getValue)
                                                                .subscribe(handler);

                            });


    }


    /**
     * Focus the given node, handling scrolling if needed.
     */
    public void focusNode(Node node) {
        SelectionModel<TreeItem<Node>> selectionModel = getSelectionModel();

        if (selectedTreeItem == null && node != null
            || selectedTreeItem != null && !Objects.equals(node, selectedTreeItem.getValue())) {
            // node is different from the old one
            // && node is not null

            ASTTreeItem found = ((ASTTreeItem) getRoot()).findItem(node);
            if (found != null) {
                selectionModel.select(found);
            }

            highlightFocusNodeParents(selectedTreeItem, found);

            selectedTreeItem = found;

            getFocusModel().focus(selectionModel.getSelectedIndex());
            if (!isIndexVisible(selectionModel.getSelectedIndex())) {
                scrollTo(selectionModel.getSelectedIndex());
            }
        }
    }


    private void highlightFocusNodeParents(ASTTreeItem oldSelection, ASTTreeItem newSelection) {
        if (oldSelection != null) {
            // remove highlighting on the cells of the item
            sideEffectParents(oldSelection, (item, depth) -> item.setStyleClasses());
        }

        if (newSelection != null) {
            // 0 is the deepest node, "depth" goes up as we get up the parents
            sideEffectParents(newSelection, (item, depth) -> item.setStyleClasses("ast-parent", "depth-" + depth));
        }
    }


    private void sideEffectParents(ASTTreeItem deepest, BiConsumer<ASTTreeItem, Integer> itemAndDepthConsumer) {

        int depth = 0;
        for (TreeItem<Node> item : toIterable(parentIterator(deepest, true))) {
            // the depth is "reversed" here, i.e. the deepest node has depth 0
            itemAndDepthConsumer.accept((ASTTreeItem) item, depth++);
        }

    }


    /**
     * Returns true if the item at the given index
     * is visible in the TreeView.
     */
    private boolean isIndexVisible(int index) {
        return myWrapper.isIndexVisible(index);
    }


    public Var<Consumer<Node>> onNodeClickedHandlerProperty() {
        return onNodeClickedHandler;
    }
}