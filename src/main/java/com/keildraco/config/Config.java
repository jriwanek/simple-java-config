package com.keildraco.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import com.keildraco.config.data.DataQuery;
import com.keildraco.config.exceptions.TypeRegistrationException;
import com.keildraco.config.exceptions.ParserRegistrationException;
import com.keildraco.config.factory.TypeFactory;
import com.keildraco.config.interfaces.AbstractParserBase;
import com.keildraco.config.interfaces.EmptyParserType;
import com.keildraco.config.interfaces.IStateParser;
import com.keildraco.config.interfaces.ItemType;
import com.keildraco.config.interfaces.ParserInternalTypeBase;
import com.keildraco.config.tokenizer.Tokenizer;

/**
 *
 * @author Daniel Hazelton
 *
 */
public final class Config {

	/**
	 *
	 */
	public static final Logger LOGGER = LogManager.getFormatterLogger("config");

	/**
	 *
	 */
	private static final TypeFactory CORE_TYPE_FACTORY = new TypeFactory();

	/**
	 *
	 */
	public static final ParserInternalTypeBase EMPTY_TYPE = new EmptyParserType();

	/**
	 *
	 */
	public static final int DEFAULT_HASH_SIZE = 256;

	/**
	 *
	 */
	private Config() {
		// do nothing, not even throw
	}

	/**
	 *
	 * @return
	 */
	public static TypeFactory getFactory() {
		return CORE_TYPE_FACTORY;
	}

	/**
	 *
	 * @param name
	 * @param clazz
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static IStateParser registerParserGenerator(final Class<? extends IStateParser> clazz)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		final Constructor<? extends IStateParser> c = clazz.getConstructor(TypeFactory.class,
				ParserInternalTypeBase.class);
		final IStateParser cc = c.newInstance(CORE_TYPE_FACTORY, Config.EMPTY_TYPE);
		cc.registerTransitions(CORE_TYPE_FACTORY);
		return cc;
	}

	/**
	 *
	 * @param name
	 * @param clazz
	 */
	private static void registerParserInternal(final String name,
			final Class<? extends IStateParser> clazz) {
		CORE_TYPE_FACTORY.registerParser(() -> {
			try {
				return registerParserGenerator(clazz);
			} catch (NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ParserRegistrationException(name, e);
			}
		}, name);
	}

	/**
	 *
	 * @param parent
	 * @param name
	 * @param value
	 * @param clazz
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static ParserInternalTypeBase registerTypeGenerator(final ParserInternalTypeBase parent,
			final String name, final String value,
			final Class<? extends ParserInternalTypeBase> clazz) throws NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		final Constructor<? extends ParserInternalTypeBase> c = clazz
				.getConstructor(ParserInternalTypeBase.class, String.class, String.class);
		return c.newInstance(parent, name, value);
	}

	/**
	 *
	 * @param type
	 * @param clazz
	 */
	private static void registerTypeInternal(final ItemType type,
			final Class<? extends ParserInternalTypeBase> clazz) {
		CORE_TYPE_FACTORY.registerType((parent, name, value) -> {
			try {
				if (parent == null) {
					return registerTypeGenerator(EMPTY_TYPE, name, value, clazz);
				} else {
					return registerTypeGenerator(parent, name, value, clazz);
				}
			} catch (NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new TypeRegistrationException(String.format(
						"Caught exception %s when trying to register type %s", e.getClass(), name));
			}
		}, type);
	}

	/**
	 *
	 * @param type
	 * @param clazz
	 */
	public static void registerType(final ItemType type,
			final Class<? extends ParserInternalTypeBase> clazz) {
		registerTypeInternal(type, clazz);
	}

	/**
	 *
	 * @param clazz
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void registerType(final Class<? extends ParserInternalTypeBase> clazz)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		final Constructor<? extends ParserInternalTypeBase> cc = clazz.getConstructor(String.class);
		ParserInternalTypeBase zz = cc.newInstance("blargh");
		registerTypeInternal(zz.getType(), clazz);
	}

	/**
	 *
	 * @param name
	 * @param clazz
	 */
	public static void registerParser(final String name,
			final Class<? extends IStateParser> clazz) {
		registerParserInternal(name, clazz);
	}

	/**
	 *
	 * @param clazz
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void registerParser(final Class<? extends IStateParser> clazz)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		final Constructor<? extends IStateParser> cc = clazz.getConstructor(TypeFactory.class,
				ParserInternalTypeBase.class);
		final IStateParser zz = cc.newInstance(CORE_TYPE_FACTORY, null);
		registerParserInternal(zz.getName(), clazz);
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
	public static void registerKnownParts() {
		(new Reflections("com.keildraco.config.types")).getSubTypesOf(ParserInternalTypeBase.class)
				.stream().forEach(t -> {
					try {
						Config.registerType(t);
					} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
							| InvocationTargetException e) {
						throw new TypeRegistrationException(
								String.format("Caught exception %s when trying to register type %s",
										e.getClass(), t.getName()));
					}
				});
		(new Reflections("com.keildraco.config.states")).getSubTypesOf(AbstractParserBase.class)
				.stream().forEach(t -> {
					try {
						Config.registerParser(t);
					} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
							| InvocationTargetException e) {
						throw new ParserRegistrationException(t.getName(), e);
					}
				});
	}

	/**
	 *
	 */
	public static void reset() {
		CORE_TYPE_FACTORY.reset();
	}

	/**
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private static ParserInternalTypeBase runParser(final Reader reader) throws IOException {
		final StreamTokenizer tok = new StreamTokenizer(reader);
		final Tokenizer t = new Tokenizer(tok);
		return CORE_TYPE_FACTORY.getParser("ROOT", null).getState(t);
	}

	/**
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static DataQuery parseStream(final InputStream is) throws IOException {
		final InputStreamReader br = new InputStreamReader(is, StandardCharsets.UTF_8);
		final ParserInternalTypeBase res = runParser(br);
		return DataQuery.of(res);
	}

	/**
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static DataQuery loadFile(final URI filePath) throws IOException {
		return parseStream(filePath.toURL().openStream());
	}

	/**
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static DataQuery loadFile(final Path filePath) throws IOException, URISyntaxException {
		final String ts = String.join("/", filePath.toString().split("\\\\"));
		return loadFile(ts);
	}

	/**
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static DataQuery loadFile(final String filePath) throws IOException, URISyntaxException {
		final URL tu = Config.class.getClassLoader().getResource(filePath);
		if (tu != null) {
			final URI temp = tu.toURI();
			return loadFile(temp);
		} else {
			throw new IOException("URL was null!");
		}
	}

	/**
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static DataQuery parseString(final String data) throws IOException {
		return parseStream(IOUtils.toInputStream(data, StandardCharsets.UTF_8));
	}
}
