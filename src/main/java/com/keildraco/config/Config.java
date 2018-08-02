package com.keildraco.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import java.io.Reader;
import java.io.StreamTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.keildraco.config.data.DataQuery;
import com.keildraco.config.exceptions.GenericParseException;
import com.keildraco.config.exceptions.IllegalParserStateException;
import com.keildraco.config.exceptions.UnknownStateException;
import com.keildraco.config.factory.Tokenizer;
import com.keildraco.config.factory.TypeFactory;
import com.keildraco.config.interfaces.AbstractParserBase;
import com.keildraco.config.interfaces.IStateParser;
import com.keildraco.config.interfaces.ParserInternalTypeBase;
import com.keildraco.config.interfaces.ParserInternalTypeBase.ItemType;

import org.reflections.Reflections;

public final class Config {
	public static final Logger LOGGER = LogManager.getFormatterLogger("config");
	private static TypeFactory coreTypeFactory = new TypeFactory();
	
	private Config() {
		// do nothing, not even throw
	}

	public static TypeFactory getFactory() {
		return coreTypeFactory;
	}

	private static IStateParser registerParserGenerator(final String name,
			final Class<? extends IStateParser> clazz) {
		Constructor<? extends IStateParser> c;
		try {
			c = clazz.getConstructor(TypeFactory.class, ParserInternalTypeBase.class);
			IStateParser cc = c.newInstance(coreTypeFactory, ParserInternalTypeBase.EmptyType);
			cc.registerTransitions(coreTypeFactory);
			return cc;
		} catch (final NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error("Exception getting type instance for %s (%s): %s", name, e.toString(), e.getMessage());
			java.util.Arrays.asList(e.getStackTrace()).stream().forEach(LOGGER::error);
			return null;
		}
	}

	private static void registerParserInternal(final String name,
			final Class<? extends IStateParser> clazz) {
		coreTypeFactory.registerParser(() -> registerParserGenerator(name, clazz), name);
	}

	private static ParserInternalTypeBase registerTypeGenerator(final ParserInternalTypeBase parent,
			final String name, final String value,
			final Class<? extends ParserInternalTypeBase> clazz) {
		Constructor<? extends ParserInternalTypeBase> c;
		try {
			c = clazz.getConstructor(ParserInternalTypeBase.class, String.class, String.class);
			return c.newInstance(parent, name, value);
		} catch (final NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error("Exception getting type instance for %s (%s): %s", name, e.toString(), e.getMessage());
			java.util.Arrays.asList(e.getStackTrace()).stream().forEach(LOGGER::error);
			return null;
		}
	}

	private static void registerTypeInternal(final ItemType type,
			final Class<? extends ParserInternalTypeBase> clazz) {
		coreTypeFactory.registerType(
				(parent, name, value) -> registerTypeGenerator(parent, name, value, clazz), type);
	}

	public static void registerType(final ItemType type,
			final Class<? extends ParserInternalTypeBase> clazz) {
		registerTypeInternal(type, clazz);
	}

	public static void registerParser(final String name,
			final Class<? extends IStateParser> clazz) {
		registerParserInternal(name, clazz);
	}

	private static void registerType(Class<? extends ParserInternalTypeBase> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException{
		Constructor<? extends ParserInternalTypeBase> cc = clazz.getConstructor(String.class);
		ParserInternalTypeBase zz = cc.newInstance("blargh");
		registerType(zz.getType(), clazz);
	}
	
	private static void registerParser(Class<? extends IStateParser> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<? extends IStateParser> cc = clazz.getConstructor(TypeFactory.class, ParserInternalTypeBase.class);
		IStateParser zz = cc.newInstance(coreTypeFactory, null);
		registerParser(zz.getName().toUpperCase(), clazz);
	}

	/**
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 *
	 */
	public static void registerKnownParts() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Reflections typeRefs = new Reflections("com.keildraco.config.types");
		Reflections parserRefs = new Reflections("com.keildraco.config.states");
		List<Class<? extends ParserInternalTypeBase>> types = typeRefs.getSubTypesOf(ParserInternalTypeBase.class).stream()
		.collect(Collectors.toList());
		
		for(Class<? extends ParserInternalTypeBase> type : types) {
			registerType(type);
		}
		
		List<Class<? extends IStateParser>> parsers = parserRefs.getSubTypesOf(AbstractParserBase.class).stream().collect(Collectors.toList());
		
		for( Class<? extends IStateParser> parser : parsers ) {
			registerParser(parser);
		}
	}

	public static void reset() {
		coreTypeFactory = new TypeFactory();
	}
	
	private static ParserInternalTypeBase runParser(final Reader reader) throws IOException, IllegalParserStateException, UnknownStateException, GenericParseException {
		StreamTokenizer tok = new StreamTokenizer(reader);
		Tokenizer t = new Tokenizer(tok);
		return coreTypeFactory.getParser("ROOT", null).getState(t);
	}

	public static DataQuery parseStream(InputStream is) throws IllegalParserStateException, IOException, UnknownStateException, GenericParseException {
		InputStreamReader br = new InputStreamReader(is);
		final ParserInternalTypeBase res = runParser(br);
		return DataQuery.of(res);
	}
	/**
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws GenericParseException 
	 * @throws UnknownStateException 
	 * @throws IllegalParserStateException 
	 */
	public static DataQuery loadFile(final URI filePath) throws IOException, IllegalParserStateException, UnknownStateException, GenericParseException {
		return parseStream(filePath.toURL().openStream());
	}

	public static DataQuery loadFile(final Path filePath) throws IOException, URISyntaxException, IllegalParserStateException, UnknownStateException, GenericParseException {
		String ts = String.join("/", filePath.toString().split("\\\\"));
		URL tu = Config.class.getClassLoader().getResource(ts);
		URI temp = tu.toURI();
		return loadFile(temp);
	}

	public static DataQuery loadFile(final String filePath) throws IOException, URISyntaxException, IllegalParserStateException, UnknownStateException, GenericParseException {
		URL tu = Config.class.getClassLoader().getResource(filePath);
		URI temp = tu.toURI();
		return loadFile(temp);
	}

	/**
	 *
	 * @param data
	 * @return
	 * @throws GenericParseException 
	 * @throws UnknownStateException 
	 * @throws IOException 
	 * @throws IllegalParserStateException 
	 */
	public static DataQuery parseString(final String data) throws IllegalParserStateException, IOException, UnknownStateException, GenericParseException {
		return parseStream(IOUtils.toInputStream(data, StandardCharsets.UTF_8));
	}

}
