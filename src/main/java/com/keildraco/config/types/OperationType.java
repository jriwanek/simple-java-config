package com.keildraco.config.types;

import javax.annotation.Nullable;

import com.keildraco.config.interfaces.ParserInternalTypeBase;

public final class OperationType extends ParserInternalTypeBase {

	private String ident;

	private String operator;

	public OperationType(final String name) {
		super(name);
		this.operator = "";
	}

	public OperationType(@Nullable final ParserInternalTypeBase parent, final String name) {
		super(parent, name);
		this.operator = "";
	}

	public OperationType(@Nullable final ParserInternalTypeBase parent, final String name,
			final String value) {
		this(parent, name);
		this.ident = value;
	}

	public OperationType(final String name, final String value) {
		this(null, name, value);
	}

	public OperationType setOperation(final String oper) {
		this.operator = oper.trim();
		return this;
	}

	public int getOperator() {
		return this.operator.charAt(0);
	}

	@Override
	public String getValue() {
		return String.format("%s(%s %s)", this.getName(), this.operator, this.ident);
	}

	@Override
	public String getValueRaw() {
		return this.ident;
	}

	@Override
	public ItemType getType() {
		return ItemType.OPERATION;
	}
}
