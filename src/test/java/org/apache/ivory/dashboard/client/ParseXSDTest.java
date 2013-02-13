package org.apache.ivory.dashboard.client;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.ivory.dashboard.server.IvoryDashboardServiceImpl;
import org.apache.ivory.entity.v0.EntityType;
import org.junit.Test;

public class ParseXSDTest {
	
	@Test(expected=NoClassDefFoundError.class)
	public void testRecursiveParse() throws IllegalArgumentException, IOException
	{
		ParseXSD parser = new ParseXSD();
		IvoryDashboardServiceImpl service = new IvoryDashboardServiceImpl();
        for(EntityType type:EntityType.values()) {
            String schema = service.getSchema(type.name());
            parser.parse(schema);
        }
		
	}

}
