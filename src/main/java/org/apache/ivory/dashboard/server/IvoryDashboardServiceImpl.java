package org.apache.ivory.dashboard.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.ivory.dashboard.client.IvoryDashboardService;
import org.apache.ivory.entity.v0.EntityType;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service for initial connection with
 * the server.
 */
@SuppressWarnings("serial")
public class IvoryDashboardServiceImpl extends RemoteServiceServlet implements IvoryDashboardService {

    public String[] getEntities() throws IllegalArgumentException {

        String[] entities = new String[EntityType.values().length];
        int i = 0;
        for (EntityType e : EntityType.values()) {
            entities[i++] = e.name();
        }
        return entities;
    }

    /*
     * This method fetches the xsd from the server and returns the XSD to client
     * as a string
     */
    public String getSchema(String entityName) throws IllegalArgumentException, IOException {
        EntityType type = EntityType.valueOf(entityName);
        String schemaFile = type.getSchemaFile();
        InputStream stream = this.getClass().getResourceAsStream(schemaFile);

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append('\n');
        return builder.toString();
    }
}
