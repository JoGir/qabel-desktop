package de.qabel.desktop.daemon.sync.worker.index.sqlite;

import de.qabel.core.repository.sqlite.AbstractClientDatabase;
import de.qabel.core.repository.sqlite.migration.AbstractMigration;
import de.qabel.desktop.daemon.sync.worker.index.sqlite.migration.Migration1462872507CreateSyncedState;

import java.sql.Connection;

public class DesktopSyncDatabase extends AbstractClientDatabase {
    public DesktopSyncDatabase(Connection connection) {
        super(connection);
    }

    @Override
    public AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration1462872507CreateSyncedState(connection)
        };
    }
}