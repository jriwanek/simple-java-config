package com.keildraco.config.states;

import javax.annotation.Nullable;

import com.keildraco.config.factory.TypeFactory;
import com.keildraco.config.types.ParserInternalTypeBase;

public abstract class AbstractParserBase implements IStateParser {

	protected TypeFactory factory;

	protected boolean errored;

	protected ParserInternalTypeBase parent;

	protected String name;

	/**
	 *
	 * @param factory
	 * @param parent
	 * @param name
	 */
	public AbstractParserBase(@Nullable final TypeFactory factory, @Nullable final ParserInternalTypeBase parent, final String name) {
		this.factory = factory;
		this.errored = false;
		this.parent = parent;
		this.name = name;
	}

	@Override
	public void setFactory(final TypeFactory factory) {
		this.factory = factory;
	}

	@Override
	public TypeFactory getFactory() {
		return this.factory;
	}

	@Override
	public void setErrored() {
		this.errored = true;
	}

	@Override
	public boolean errored() {
		return this.errored;
	}

	@Override
	public void setParent(final ParserInternalTypeBase parent) {
		this.parent = parent;
	}

	@Override
	public ParserInternalTypeBase getParent() {
		return this.parent;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void clearErrors() {
		this.errored = false;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}
}
