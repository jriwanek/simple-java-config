package com.keildraco.config.types;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ParserInternalTypeBase {
	private final ParserInternalTypeBase parent;
	private String name;
	protected Map<String,ParserInternalTypeBase> items;
	public static final ParserInternalTypeBase EmptyType = new ParserInternalTypeBase("EMPTY") {
		@Override
		public boolean has(String itemName) { return false; }
		@Override
		public ParserInternalTypeBase get(String itemName) { return null; }
		@Override
		public void addItem(ParserInternalTypeBase item) { return; }
		@Override
	    public ItemType getType() { return ItemType.EMPTY; }
	};
	
	public ParserInternalTypeBase() {
		this(null, "");
	}
	
	public ParserInternalTypeBase(String name) {
		this(null, name);
	}
	
	public ParserInternalTypeBase(ParserInternalTypeBase parent, String name) {
		this.name = name;
		this.parent = parent;
		this.items = new ConcurrentHashMap<>();
	}
	
	public ParserInternalTypeBase(ParserInternalTypeBase parent, String name, String value) {
		this(parent, name);
	}
	
    public ParserInternalTypeBase get(String itemName)  {
    	String[] bits;
    	if(itemName.indexOf('.') > 0) {
    		bits = itemName.split("\\.");
    		if(this.has(bits[0])) {
    			String nameRest = itemName.substring(itemName.indexOf('.'));
    			System.err.println(nameRest);
    			return this.get(bits[0]).get(nameRest);
    		}
    	} else if(this.has(itemName)) {
    		return this.items.get(itemName);
    	}
    	return ParserInternalTypeBase.EmptyType;
    }
    
    public boolean has(String itemName) {
    	if(itemName.contains(".") && itemName.split("\\.").length > 2) return this.items.containsKey(itemName.split("\\.")[0]);
    	if(itemName.contains(".")) {
    		String nn = itemName.substring(0, itemName.indexOf('.'));
    		String rest = itemName.substring(itemName.indexOf('.'));
    		return this.items.containsKey(nn)?this.items.get(nn).has(rest):false;
    	}
    	return this.items.containsKey(itemName);
    } 
    
    public enum ItemType {
        SECTION, IDENTIFIER, NUMBER, BOOLEAN, LIST, OPERATION, INVALID, EMPTY;
    };
    
    public ItemType getType() { return ItemType.INVALID; }
    
    public String asString() {
    	return "BaseType()";
    }
    
    public Number toNumber()  {
    	return Float.NaN;
    }
    
    public boolean toBoolean()  {
    	return Boolean.FALSE;
    }
    
    public List<ParserInternalTypeBase> toList() {
    	return Collections.emptyList();
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public void addItem(ParserInternalTypeBase item) {
    	this.items.put(item.getName(),item);
    }
    
    public Map<String, ParserInternalTypeBase> getChildren() {
    	return Collections.unmodifiableMap(this.items);
    }

    private List<String> subWalk(Set<Entry<String, ParserInternalTypeBase>> items) {
    	List<String> blargh = new LinkedList<>();
    	
    	items.stream().forEach( ent -> {
			String baseName = makeName(ent.getKey(), ent.getValue().getParent());
			List<String> temp;
    		switch(ent.getValue().getType()) {
    		case SECTION:
    			temp = this.subWalk(ent.getValue().getChildren().entrySet());
    			temp.stream().forEach( s -> blargh.add(String.format("%s.%s", baseName, s)));
    			break;
    		case OPERATION:
    			blargh.add(String.format("%s.%s", baseName, this.pibAsString(ent.getValue())));
    			break;
    		case LIST:
    			temp = this.walkList(ent.getValue());
    			temp.stream().forEach( s -> blargh.add(String.format("%s.%s", baseName, s)));
    			break;
    		default:
    			blargh.add(String.format("%s.%s", baseName, ent.getValue().getName()));
    		}
    	});
    	return blargh;    	
    }
    
    private String makeName(String key, ParserInternalTypeBase parent) {
    	Deque<String> bases = new LinkedList<>();
    	bases.push(key);
    	
    	bases.push(parent.getName());
    	ParserInternalTypeBase next = parent.getParent();
    	while( next != EmptyType ) {
    		bases.push(next.getName());
    		next = next.parent;
    	}
    	List<String> rv = bases.stream().collect(Collectors.toList());
    	Collections.reverse(rv);
    	return String.join(".", rv);
	}

	private String pibAsString(ParserInternalTypeBase pib) {
    	switch(pib.getType()) {
    	case OPERATION:
    		return String.format("(%s%c%s)", pib.getName(), ((OperationType)pib).getOperator(), pib.getValue());
    	case IDENTIFIER:
    		return pib.getName();
    	default:
    		return "BROKEN";
    	}
    }
    
    private List<String> walkList(ParserInternalTypeBase value) {
    	return value.toList().stream().map(this::pibAsString).collect(Collectors.toList());
	}

	public List<String> flattenData() {
    	return this.subWalk(this.items.entrySet());
    }
    
    public ParserInternalTypeBase getParent() {
    	return this.parent!=null?this.parent:EmptyType;
    }
    
    public String getValue() {
    	return "";
    }
    
}
