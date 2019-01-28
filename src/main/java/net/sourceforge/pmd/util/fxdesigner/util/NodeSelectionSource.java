/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util;

import java.util.Objects;

import org.reactfx.EventStream;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.MainDesignerController;
import net.sourceforge.pmd.util.fxdesigner.util.controls.AstTreeView;
import net.sourceforge.pmd.util.fxdesigner.util.controls.NodeParentageCrumbBar;


/**
 * A control or controller that has the ability to push node selection events.
 * When a node is selected in the control (e.g. {@link AstTreeView}, {@link NodeParentageCrumbBar}, etc),
 * the whole UI is synchronized to the node. Selection events are merged iteratively into
 * a global stream for the whole app. Events from that stream are handled by {@link MainDesignerController}.
 *
 * @author Clément Fournier
 */
public interface NodeSelectionSource {

    /**
     * Returns a stream of nodes that pushes an event every time
     * this control records a *user* change in selection.
     */

    EventStream<NodeSelectionEvent> getSelectionEvents();


    default void select(NodeSelectionEvent selectionEvent) {
        System.out.println("\t\t" + this.getClass().getSimpleName() + " handling " + selectionEvent);
        if (selectionEvent.getOrigin() != this) {
            setFocusNode(selectionEvent.getSelection());
        } else {
            System.out.println("\tUnhandled");
        }
    }


    void setFocusNode(Node node);


    final class NodeSelectionEvent {

        private final Node selection;
        private final NodeSelectionSource origin;


        public NodeSelectionEvent(Node selection, NodeSelectionSource origin) {
            this.selection = selection;
            this.origin = origin;
        }


        public Node getSelection() {
            return selection;
        }


        public NodeSelectionSource getOrigin() {
            return origin;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NodeSelectionEvent that = (NodeSelectionEvent) o;
            return Objects.equals(selection, that.selection) &&
                Objects.equals(origin, that.origin);
        }


        @Override
        public int hashCode() {
            return Objects.hash(selection, origin);
        }


        @Override
        public String toString() {
            return getSelection().getXPathNodeName() + "(" + hashCode() + ")\t\tfrom " + getOrigin().getClass().getSimpleName();
        }
    }

}