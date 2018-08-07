package com.keildraco.config.tests.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.keildraco.config.Config;
import com.keildraco.config.factory.TypeFactory;
import com.keildraco.config.interfaces.IStateParser;
import com.keildraco.config.interfaces.ParserInternalTypeBase;

public class MiscelanneousTests {

	@BeforeEach
	final void setupEach() {
		try {
			Config.reset();
			Config.registerKnownParts();
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			fail("Exception (" + e.getMessage() + " :: " + e + ") caught when not expected");
		}
	}

	@Test
	final void testSetFactory() {
		IStateParser parser = Config.getFactory().getParser("ROOT", null);
		TypeFactory f = new TypeFactory();
		parser.setFactory(f);
		assertEquals(f, parser.getFactory());
	}

	@Test
	final void testGetParent() {
		IStateParser parser = Config.getFactory().getParser("ROOT", null);
		assertEquals(ParserInternalTypeBase.EMPTY_TYPE, parser.getParent());
	}

	@Test
	final void testSetName() {
		final String name = "BLARGH";
		IStateParser parser = Config.getFactory().getParser("ROOT", null);
		parser.setName(name);
		assertEquals(name, parser.getName());
	}

}