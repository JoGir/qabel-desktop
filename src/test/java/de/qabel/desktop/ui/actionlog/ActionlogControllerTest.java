package de.qabel.desktop.ui.actionlog;

import de.qabel.core.config.Account;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropMessageMetadata;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.desktop.AsyncUtils;
import de.qabel.desktop.daemon.drop.TextMessage;
import de.qabel.desktop.repository.DropMessageRepository;
import de.qabel.desktop.repository.inmemory.InMemoryDropMessageRepository;
import de.qabel.desktop.ui.AbstractControllerTest;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemController;
import de.qabel.desktop.ui.actionlog.item.MyActionlogItemView;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


public class ActionlogControllerTest extends AbstractControllerTest {
    ActionlogController controller;
    Identity i;
    ActionlogView view;
    Contact c;
    String text = "MessageString";
    DropMessage dm;
    InMemoryDropMessageRepository repo;

    @Test
    public void addMessageToActionlogTest() throws Exception {
        contactRepository.save((Contact) dm.getSender(), i);
        controller.addMessageToActionlog(dm);
        assertEquals(1, controller.messages.getChildren().size());
    }

    @Test
    public void addOwnMessageToActionlogTest() throws Exception {
        controller.addOwnMessageToActionlog(dm);
        assertEquals(1, controller.messages.getChildren().size());
    }

    @Test
    public void marksSeenMessages() throws Exception {
        controller.setContact(c);
        waitUntil(() -> controller.contact == c);
        PersistenceDropMessage message = new PersistenceDropMessage(dm, c, i, false, false);
        System.out.println("saving message " + message);
        dropMessageRepository.save(message);
        waitUntil(message::isSeen, 10000L); // is done in cascaded async calls => higher timeout
    }

    @Test
    public void switchBetweenIdentitesTest() throws Exception {
        clientConfiguration.selectIdentity(i);
        controller.sendDropMessage(c, "msg1");
        i = identityBuilderFactory.factory().withAlias("NewIdentity").build();
        c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());

        clientConfiguration.selectIdentity(i);
        controller.sendDropMessage(c, "msg2");

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, i);

        assertEquals(1, lst.size());
        DropMessage dropMessage = lst.get(0).dropMessage;
        assertEquals("msg2", TextMessage.fromJson(dropMessage.getDropPayload()).getText());
    }

    @Test
    public void addDropMessageMetadata() throws Exception {
        controller.sendDropMessage(c, "msg2");

        List<PersistenceDropMessage> lst = dropMessageRepository.loadConversation(c, i);
        DropMessageMetadata metadata = lst.get(0).dropMessage.getDropMessageMetadata();
        assertEquals(metadata, new DropMessageMetadata(i));
    }

    @Test
    public void refreshTime() throws Exception {
        controller.sleepTime = 1;
        controller.dateRefresher.interrupt();
        DropMessage d = new DropMessage(i, new TextMessage("payload").toJson(), "test");
        Contact sender = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());

        Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("dropMessage", d);
        injectionContext.put("contact", sender);

        MyActionlogItemView my = new MyActionlogItemView(injectionContext::get);
        MyActionlogItemController messagesController = (MyActionlogItemController) my.getPresenter();
        controller.messageControllers.add(messagesController);

        messagesController.setDropMessage(d);
        String old = messagesController.getDateLabel().getText();
        messagesController.getDateLabel().setText("");


        waitUntil(() -> {
            String newString = messagesController.getDateLabel().getText();
            return old.equals(newString);
        },
            () -> "dateString was not refreshed as expected. expected: '" + old + "'" +
                ", actual: '" + messagesController.getDateLabel().getText()
        );
    }

    @Test
    public void notificationShownForUnknownContacts() throws Exception {
        AsyncUtils.waitUntil(() -> controller.notification.isManaged());
        toggleContactStatus();
        AsyncUtils.assertAsync(() -> controller.notification.isManaged(), is(false));
        toggleContactStatus();
        AsyncUtils.assertAsync(() -> controller.notification.isManaged(), is(true));
    }

    private void toggleContactStatus() throws PersistenceException {
        if (controller.contact.getStatus() == Contact.ContactStatus.UNKNOWN) {
            controller.contact.setStatus(Contact.ContactStatus.NORMAL);
        } else {
            controller.contact.setStatus(Contact.ContactStatus.UNKNOWN);
        }
        contactRepository.save(controller.contact, identity);
    }

    @Test
    public void acceptContact() throws Exception {
        toggleContactStatus();
        controller.accept.fire();
        AsyncUtils.assertAsync(() -> {
            assertThat(controller.notification.isManaged(), is(false));
            Contact contact = contactRepository.find(controller.contact.getId());
            assertThat(contact.getStatus(), is(Contact.ContactStatus.NORMAL));
            assertThat(contact.isIgnored(), is(false));
        });
    }

    @Test
    public void ignoreContact() throws Exception {
        toggleContactStatus();
        controller.ignore.fire();
        AsyncUtils.assertAsync(() -> {
            assertThat(controller.notification.isManaged(), is(false));
            Contact contact = contactRepository.find(controller.contact.getId());
            assertThat(contact.getStatus(), is(Contact.ContactStatus.NORMAL));
            assertThat(contact.isIgnored(), is(true));
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        repo = new InMemoryDropMessageRepository();
        dropMessageRepository = repo;
        super.setUp();
        i = identityBuilderFactory.factory().withAlias("TestAlias").build();
        c = new Contact(i.getAlias(), i.getDropUrls(), i.getEcPublicKey());
        c.setStatus(Contact.ContactStatus.UNKNOWN);
        createController(i);
        controller.setContact(c);
        controller = (ActionlogController) view.getPresenter();

        dm = new DropMessage(c, new TextMessage(text).toJson(), DropMessageRepository.PAYLOAD_TYPE_MESSAGE);
    }

    private void createController(Identity i) {
        view = new ActionlogView();
        clientConfiguration.selectIdentity(i);
        clientConfiguration.setAccount(new Account("Provider", "user", "auth"));
        controller = (ActionlogController) view.getPresenter();
    }
}
