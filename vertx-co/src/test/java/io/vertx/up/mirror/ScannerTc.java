package io.vertx.up.mirror;

import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import top.UnitBase;

import java.util.Set;

public class ScannerTc extends UnitBase {

    @Test
    public void testScan(final TestContext context) {
        final Set<Class<?>> clazzes = Pack.getClasses(null);
        context.assertNotNull(clazzes);
    }
}