package com.keildraco.config.types;

import javax.annotation.Nullable;

import com.keildraco.config.interfaces.ParserInternalTypeBase;

public final class IdentifierType extends ParserInternalTypeBase {

	private final String ident;

	public IdentifierType(final String n) {
		this(n, n);
	}

	public IdentifierType(final String n, final String v) {
		this(null, n, v);
	}

	public IdentifierType(@Nullable final ParserInternalTypeBase parent, final String name,
			final String value) {
		super(parent, name);
		this.ident = value;
	}

	@Override
	public void addItem(final ParserInternalTypeBase item) {
		// identifiers - that is, key-value pairs - can't store more than an identifier they equal,
		// so this method gets stubbed
	}

	@Override
	public boolean has(final String s) {
		return this.getName().equalsIgnoreCase(s) || this.ident.equalsIgnoreCase(s);
	}

	@Override
	public ParserInternalTypeBase get(final String s) {
		if (this.has(s)) {
			return this;
		}
		return EMPTY_TYPE;
	}

	@Override
	public String getValue() {
		if (this.getName().equals(this.ident)) {
			return this.ident;
		}
		return String.format("%s = %s", this.getName(), this.ident);
	}

	@Override
	public String getValueRaw() {
		return this.ident;
	}

	@Override
	public ItemType getType() {
		return ItemType.IDENTIFIER;
	}
}
