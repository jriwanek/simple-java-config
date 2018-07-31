package com.keildraco.config.tests.types;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.keildraco.config.types.OperationType;
import com.keildraco.config.types.ParserInternalTypeBase;

import static com.keildraco.config.types.ParserInternalTypeBase.EmptyType;
import static com.keildraco.config.types.ParserInternalTypeBase.ItemType;

public class OperationTypeTest {
	private OperationType testItem;
	
	@Before
	public void setUp() throws Exception {
		this.testItem = new OperationType(EmptyType, "blargh", "foobar");
		this.testItem.setOperation("!");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testOperationTypeString() {
		try {
			@SuppressWarnings("unused")
			OperationType op = new OperationType("OPERATION");
			assertTrue("Expected no exception", true);
		} catch(Exception e) {
			fail("Caught exception instantiating new OperationType");
		}
	}
	
	@Test
	public final void testOperationTypeParserInternalTypeBaseString() {
		try {
			@SuppressWarnings("unused")
			OperationType op = new OperationType(ParserInternalTypeBase.EmptyType, "OPERATION");
			assertTrue("Expected no exception", true);
		} catch(Exception e) {
			fail("Caught exception instantiating new OperationType");
		}
	}
	
	@Test
	public final void testGetType() {
		assertEquals(ItemType.OPERATION, this.testItem.getType());
	}

	@Test
	public final void testAsString() {
		assertEquals("blargh(! foobar)", this.testItem.asString());
	}

	@Test
	public final void testSetOperation() {
		try {
			this.testItem.setOperation("!");
			assertTrue("Expected no exception", true);
		} catch(Exception e) {
			fail("Exception ("+e.getMessage()+" :: "+e+") caught when not expected");
		}
	}

}
