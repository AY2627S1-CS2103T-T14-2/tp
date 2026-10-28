package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests that supporter cards in the list are as tall as their content.
 */
public class PersonListCardHeightTest {
    private static final double TOLERANCE = 1;

    private static final Person LONG_SUPPORTER = new PersonBuilder()
            .withName("Muhammad Abdul Rahman bin Mohamed Iskandar Shafiq")
            .withOrganisation("Singapore Society for the Prevention of Cruelty to Animals").build();
    private static final Person SHORT_SUPPORTER = new PersonBuilder().withName("Amy Tan").build();

    @Test
    public void layout_narrowList_cardsFitContent() throws Exception {
        assertCardsFitContent(340);
    }

    @Test
    public void layout_wideList_cardsFitContent() throws Exception {
        assertCardsFitContent(700);
    }

    private static void assertCardsFitContent(double width) throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> supporters =
                    FXCollections.observableArrayList(LONG_SUPPORTER, SHORT_SUPPORTER);
            Region root = new PersonListPanel(supporters).getRoot();
            Scene scene = new Scene(root, width, 1000);
            scene.getStylesheets().add(
                    PersonListCardHeightTest.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();
            root.layout();

            List<ListCell<?>> cards = root.lookupAll(".list-cell").stream()
                    .<ListCell<?>>map(node -> (ListCell<?>) node)
                    .filter(cell -> cell.getGraphic() != null)
                    .toList();
            assertEquals(2, cards.size());
            for (ListCell<?> cell : cards) {
                assertEquals(getContentHeight(cell), cell.getHeight(), TOLERANCE);
            }
            for (Node node : root.lookupAll(".scroll-bar")) {
                ScrollBar scrollBar = (ScrollBar) node;
                assertFalse(scrollBar.getOrientation() == Orientation.HORIZONTAL && scrollBar.isVisible());
            }
        });
    }

    /**
     * Returns the height {@code cell} needs for its card: the bottom of the card's lowest visible detail
     * plus the bottom padding of the card and the cell, or the card's minimum height plus the cell's
     * padding if that is larger.
     */
    private static double getContentHeight(ListCell<?> cell) {
        VBox details = (VBox) cell.lookup("#name").getParent().getParent();
        double cellTop = cell.localToScene(0, 0).getY();
        double lowestBottom = 0;
        for (Node child : details.getChildren()) {
            if (child.isManaged() && child.isVisible()) {
                double bottom = child.localToScene(child.getBoundsInLocal()).getMaxY() - cellTop;
                lowestBottom = Math.max(lowestBottom, bottom);
            }
        }
        assertTrue(lowestBottom > 0);
        Insets cellPadding = cell.getInsets();
        return Math.max(details.getMinHeight() + cellPadding.getTop() + cellPadding.getBottom(),
                lowestBottom + details.getPadding().getBottom() + cellPadding.getBottom());
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        try {
            Platform.startup(task);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(task);
        }
        task.get(15, TimeUnit.SECONDS);
    }
}
