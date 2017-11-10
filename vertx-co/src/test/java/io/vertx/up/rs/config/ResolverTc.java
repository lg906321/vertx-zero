package io.vertx.up.rs.config;

import io.vertx.ext.unit.TestContext;
import io.vertx.up.rs.VertxHelper;
import org.junit.Test;
import org.vie.exception.up.PathAnnoEmptyException;
import top.UnitBase;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

public class ResolverTc extends UnitBase {

    private String parseRoot(final Class<?> root) {
        final Path path = VertxHelper.getPath(root);
        return PathResolver.resolve(path);
    }

    private Set<String> parseMethod(final Class<?> root) {
        final Set<String> pathes = new TreeSet<>();
        final Method[] methods = root.getDeclaredMethods();
        for (final Method method : methods) {
            final Path path = VertxHelper.getPath(method);
            getLogger().info("[TEST] Parse info: {0}, method = {1}, Path = {2}.",
                    root.getName(),
                    method.getName(),
                    (null == path) ? null : path.value());
            if (null != path) {
                final String uri = PathResolver.resolve(
                        VertxHelper.getPath(method), parseRoot(root));
                pathes.add(uri);
            }
        }
        getLogger().info("[TEST] Parsed :{0}", pathes);
        return pathes;
    }

    @Test(expected = PathAnnoEmptyException.class)
    public void testRte() {
        PathResolver.resolve(null);
    }

    @Test
    public void testRt1(final TestContext context) {
        context.assertEquals("/api", parseRoot(Root1.class));
    }

    @Test
    public void testRt2(final TestContext context) {
        context.assertEquals("/api", parseRoot(Root2.class));
    }

    @Test
    public void testRt3(final TestContext context) {
        context.assertEquals("/api", parseRoot(Root3.class));
    }

    @Test
    public void testMd1(final TestContext context) {
        final Set<String> pathes = parseMethod(Method1.class);
        final Set<String> expected = new TreeSet<String>() {
            {
                add("/api/test");
            }
        };
        context.assertEquals(expected, pathes);
    }

    @Test
    public void testMd2(final TestContext context) {
        final Set<String> pathes = parseMethod(Method2.class);
        final Set<String> expected = new TreeSet<String>() {
            {
                add("/api/test/:name");
            }
        };
        context.assertEquals(expected, pathes);
    }

    @Test
    public void testMd3(final TestContext context) {
        final Set<String> pathes = parseMethod(Method3.class);
        final Set<String> expected = new TreeSet<String>() {
            {
                add("/api/test/:name");
                add("/api/test/:id");
            }
        };
        context.assertEquals(expected, pathes);
    }

    @Test
    public void testMd4(final TestContext context) {
        final Set<String> pathes = parseMethod(Method4.class);
        final Set<String> expected = new TreeSet<String>() {
            {
                add("/test/:name");
                add("/test/:id");
            }
        };
        context.assertEquals(expected, pathes);
    }
}

@Path("api")
class Method2 {

    @Path("test////:name")
    public void test() {
    }
}

@Path("")
class Method4 {

    @Path("test////:name")
    public void test() {
    }

    @Path("///test/:id")
    public void test1() {
    }
}

@Path("api")
class Method3 {

    @Path("test////:name")
    public void test() {
    }

    @Path("///test/:id")
    public void test1() {
    }
}

@Path("api")
class Method1 {

    @Path("test")
    public void test() {
    }
}

/**
 * 1. Root: api
 */
@Path("api")
class Root1 {

    @Path("/test")
    public void test() {
    }
}

/**
 * 2. Root: api/
 */
@Path("api/")
class Root2 {

    @Path("/test")
    public void test() {
    }
}

/**
 * 3. Root: api//
 */
@Path("///api///")
class Root3 {

    @Path("/test")
    public void test() {
    }
}