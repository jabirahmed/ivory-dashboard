package org.apache.ivory.dashboard.client;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service for initial connection with the server.
 */
@RemoteServiceRelativePath("api")
public interface IvoryDashboardService extends RemoteService {
  String[] getEntities();
  String getSchema(String entityName) throws IllegalArgumentException, IOException;
}