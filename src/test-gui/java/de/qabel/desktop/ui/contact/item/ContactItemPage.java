package de.qabel.desktop.ui.contact.item;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import de.qabel.desktop.ui.contact.context.AssignContactPage;
import javafx.scene.Node;
import org.testfx.api.FxRobot;

public class ContactItemPage extends AbstractPage {
    private ContactItemController controller;

    public ContactItemPage(FXRobot baseFXRobot, FxRobot robot, ContactItemController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public Integer getIndicatorCount() {
        String indicatorText = controller.getIndicator().getText();
        return indicatorText.isEmpty() ? null : Integer.parseInt(indicatorText);
    }

    private Node query(String query) {
        waitUntil(() -> robot.lookup(query).tryQuery().isPresent());
        return robot.lookup(query).tryQuery().get();
    }

    public String getAvatarStyle() {
        return query("#avatar").getStyle();
    }

    public Node getDeleteButton() {
        return getFirstNode("#delete");
    }

    public void delete() {
        clickOn("#delete");
    }

    public AssignContactPage assign() {
        rightClickOn(".contact-" + controller.contact.getId());
        return new AssignContactPage(baseFXRobot, robot);
    }
}
