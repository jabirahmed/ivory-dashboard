package org.apache.ivory.dashboard.server;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.ivory.entity.v0.EntityType;
import org.testng.annotations.Test;

public class IvoryDashboardServiceImplTest {
    @Test
    public void testGetEntities() {
        IvoryDashboardServiceImpl service = new IvoryDashboardServiceImpl();
        String[] entities = service.getEntities();
        Assert.assertEquals(3, entities.length);
    }
    
    @Test
    public void testGetSchema() throws IllegalArgumentException, IOException {
        IvoryDashboardServiceImpl service = new IvoryDashboardServiceImpl();
        for(EntityType type:EntityType.values()) {
            String schema = service.getSchema(type.name());
            Assert.assertNotNull(schema);
        }
    }
}
