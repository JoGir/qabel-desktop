package de.qabel.desktop.repository.inmemory;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblNetworkInvalidResponseException;
import de.qabel.desktop.ui.connector.DropConnector;
import de.qabel.desktop.ui.connector.DropPollResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class InMemoryHttpDropConnector implements DropConnector {
    private Date date = new Date();
    private RuntimeException exception;
    private int polls;

    private HashMap<String, List<DropMessage>> contactLists = new HashMap<>();

    @Override
    public void send(Contact c, DropMessage d) throws QblNetworkInvalidResponseException {

        List<DropMessage> lst = contactLists.get(c.getKeyIdentifier());
        if(lst == null){
            lst = new LinkedList<>();
        }
        lst.add(d);
        contactLists.put(c.getKeyIdentifier(), lst);
    }

    @Override
    public DropPollResponse receive(Identity i, Date sinceDate) {
        polls++;
        if (exception != null) {
            throw exception;
        }
        List<DropMessage> lst = contactLists.get(i.getKeyIdentifier());
        if(lst == null){
            return new DropPollResponse(new LinkedList<>(), date);
        }
        return new DropPollResponse(contactLists.get(i.getKeyIdentifier()), date);
    }

    public void throwException(RuntimeException e) {
        exception = e;
    }

    public int getPolls() {
        return polls;
    }
}
