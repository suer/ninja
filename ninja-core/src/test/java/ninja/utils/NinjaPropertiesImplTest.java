package ninja.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

public class NinjaPropertiesImplTest {

	@Before
	public void setup() {
	}

	@Test
	public void testSkippingThroughModesWorks() {

		// check that mode tests works:
		System.setProperty("mode", "test");
		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();
		assertEquals("test_testproperty",
		        ninjaPropertiesImpl.get("testproperty"));

		// check that mode dev works:
		System.setProperty("mode", "dev");
		ninjaPropertiesImpl = new NinjaPropertiesImpl();
		assertEquals("dev_testproperty",
		        ninjaPropertiesImpl.get("testproperty"));
		assertEquals("secret", ninjaPropertiesImpl.get("applicationSecret"));

		// remove property => we expect that the dev property is used as default
		// value
		System.clearProperty("mode");
		ninjaPropertiesImpl = new NinjaPropertiesImpl();
		assertEquals("dev_testproperty",
		        ninjaPropertiesImpl.get("testproperty"));
		assertEquals("secret", ninjaPropertiesImpl.get("applicationSecret"));

		// and in a completely different mode with no "%"-prefixed key the
		// default value use used
		System.setProperty("mode", "prod");
		ninjaPropertiesImpl = new NinjaPropertiesImpl();
		assertEquals("testproperty_without_prefix",
		        ninjaPropertiesImpl.get("testproperty"));
		assertEquals("secret", ninjaPropertiesImpl.get("applicationSecret"));

		// tear down
		System.clearProperty("mode");

	}
	
	@Test(expected = RuntimeException.class)
	public void testGetOrDie() {

		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();

		assertEquals("dev_testproperty",
		        ninjaPropertiesImpl.getOrDie("testproperty"));

		ninjaPropertiesImpl.getOrDie("a_propert_that_is_not_in_the_file");

	}

	@Test
	public void testGetBooleanParsing() {

		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();
		assertEquals(true, ninjaPropertiesImpl.getBoolean("booleanTestTrue"));

		assertEquals(false, ninjaPropertiesImpl.getBoolean("booleanTestFalse"));

		assertEquals(null, ninjaPropertiesImpl.getBoolean("booleanNotValid"));

	}
	

	@Test(expected = RuntimeException.class)
	public void testGetBooleanOrDie() {

		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();

		assertEquals(true,
		        ninjaPropertiesImpl.getBooleanOrDie("booleanTestTrue"));

		ninjaPropertiesImpl.getBooleanOrDie("booleanNotValid");

	}

	@Test
	public void testGetIntegerParsing() {

		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();

		assertEquals(new Integer(123456789),
		        ninjaPropertiesImpl.getInteger("integerTest"));

		assertEquals(null, ninjaPropertiesImpl.getInteger("integerNotValid"));

	}

	@Test(expected = RuntimeException.class)
	public void testGetIntegerDie() {

		NinjaPropertiesImpl ninjaPropertiesImpl = new NinjaPropertiesImpl();

		assertEquals(new Integer(123456789),
		        ninjaPropertiesImpl.getIntegerOrDie("integerTest"));

		ninjaPropertiesImpl.getIntegerOrDie("integerNotValid");

	}

    @Test
    public void testPropertiesBoundInGuice() {
        final NinjaPropertiesImpl props = new NinjaPropertiesImpl();
        MockService service = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                props.bindProperties(binder());
            }
        }).getInstance(MockService.class);
        assertNotNull("Application secret not set by Guice", service.applicationSecret);
        assertEquals("secret", service.applicationSecret);
    }

    public static class MockService {
        @Inject
        @Named("applicationSecret")
        public String applicationSecret;
    }


}
