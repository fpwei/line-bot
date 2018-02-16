package org.fpwei.line.server.handler;

import org.fpwei.line.server.enums.Parameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractCommandHandler.class)
public class AbstractCommandHandlerTest {

    private AbstractCommandHandler commandHandler;

    private Parameter r, n;

    @Before
    public void setUp() {
        commandHandler = mock(AbstractCommandHandler.class, Mockito.CALLS_REAL_METHODS);
        r = mock(Parameter.class);
        n = mock(Parameter.class);

        when(commandHandler.getParameter("r")).thenReturn(r);
        when(commandHandler.getParameter("n")).thenReturn(n);
    }


    @Test
    public void testParse() throws Exception {
        String testParameter = "-n 5";
        Map<Parameter, Object> result = Whitebox.invokeMethod(commandHandler, "parse", testParameter);

        Assert.assertTrue(result.containsKey(n));
        Assert.assertEquals("5", result.get(n).toString());
    }


    @Test
    public void testParse2() throws Exception {
        String testParameter = "-r";
        Map<Parameter, Object> result = Whitebox.invokeMethod(commandHandler, "parse", testParameter);

        Assert.assertTrue(result.containsKey(r));
        Assert.assertNull(result.get(r));
    }

    @Test
    public void testParse3() throws Exception {
        String testParameter = "-n 10 -r";
        Map<Parameter, Object> result = Whitebox.invokeMethod(commandHandler, "parse", testParameter);

        Assert.assertTrue(result.containsKey(n));
        Assert.assertEquals("10", result.get(n).toString());
        Assert.assertTrue(result.containsKey(r));
        Assert.assertNull(result.get(r));
    }
}
