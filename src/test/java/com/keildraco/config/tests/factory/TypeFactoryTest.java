package com.keildraco.config.tests.factory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.keildraco.config.Config;
import com.keildraco.config.data.Token;
import com.keildraco.config.factory.TypeFactory;
import com.keildraco.config.interfaces.IStateParser;
import com.keildraco.config.interfaces.ParserInternalTypeBase.ItemType;
import com.keildraco.config.states.SectionParser;
import com.keildraco.config.tokenizer.Tokenizer;
import com.keildraco.config.types.IdentifierType;

/**
 *
 * @author Daniel Hazelton
 *
 */
class TypeFactoryTest {

	/**
	 *
	 */
	@Test
	final void testTypeFactory() {
		try {
			final TypeFactory f = new TypeFactory();
			assertTrue(f != null);
		} catch (final Exception e) {
			Config.LOGGER.error("Exception getting instance for %s: %s", e.toString(),
					e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}

	/**
	 *
	 */
	@Test
	final void testRegisterType() {
		try {
			final TypeFactory f = new TypeFactory();
			f.registerType((parent, name, value) -> new IdentifierType(parent, name, value),
					ItemType.IDENTIFIER);
			assertTrue(true, "Able to register a type without an exception");
		} catch (final Exception e) {
			Config.LOGGER.error("Exception registering type %s: %s", e.toString(), e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}

	/**
	 *
	 */
	@Test
	final void testGetType() {
		try {
			final TypeFactory f = new TypeFactory();
			f.registerType((parent, name, value) -> new IdentifierType(parent, name, value),
					ItemType.IDENTIFIER);
			assertTrue(f.getType(null, "", "", ItemType.IDENTIFIER) != null,
					"TypeFactory.getType() works");
		} catch (final Exception e) {
			Config.LOGGER.error("Exception registering type %s: %s", e.toString(), e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}

	/**
	 *
	 */
	@Test
	final void testRegisterParser() {
		try {
			final TypeFactory f = new TypeFactory();
			f.registerParser(() -> {
				final SectionParser sp = new SectionParser(f, null);
				sp.registerTransitions(f);
				return sp;
			}, "SECTION");
			assertTrue(true, "Able to register a parser without exceptions");
		} catch (final Exception e) {
			Config.LOGGER.error("Exception registering type %s: %s", e.toString(), e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}

	/*
	 * registerStateTransition is actually tested in quite a few places, so we won't be testing it
	 * here
	 */

	/**
	 *
	 */
	@Test
	final void testGetParser() {
		try {
			final TypeFactory f = new TypeFactory();
			f.registerParser(() -> {
				final SectionParser sp = new SectionParser(f, null);
				sp.registerTransitions(f);
				return sp;
			}, "SECTION");
			assertTrue(f.getParser("SECTION", null) != null, "TypeFactory.getParser() works");
		} catch (final Exception e) {
			Config.LOGGER.error("Exception registering type %s: %s", e.toString(), e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}

	/**
	 *
	 */
	@Test
	final void testNextState() {
		try {
			Config.registerKnownParts();
			final String data = "key = value";
			final InputStream is = IOUtils.toInputStream(data, StandardCharsets.UTF_8);
			final InputStreamReader br = new InputStreamReader(is, StandardCharsets.UTF_8);
			final StreamTokenizer tok = new StreamTokenizer(br);
			final Tokenizer t = new Tokenizer(tok);
			final Token cur = t.peek();
			final Token next = t.peekToken();
			final IStateParser nextState = Config.getFactory().nextState("SECTION", cur, next);
			assertAll("TypeFactory.nextState() works", () -> assertTrue(nextState != null),
					() -> assertEquals("KEYVALUE",
							nextState.getName().toUpperCase(Locale.ENGLISH)));
		} catch (final Exception e) {
			Config.LOGGER.error("Exception registering type %s: %s", e.toString(), e.getMessage());
			Arrays.asList(e.getStackTrace()).stream().forEach(Config.LOGGER::error);
			fail("Caught exception running loadFile: " + e);
		}
	}
}
