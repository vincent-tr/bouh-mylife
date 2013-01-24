package mylife.home.hw.api;

import java.util.EnumSet;

/**
 * Options d'ouverture
 * @author pumbawoman
 *
 */
public enum Options {
	
	DIRECTION_INPUT(1, "direction:input"),
	DIRECTION_OUTPUT(1, "direction:output"),
	
	TYPE_DIGITAL(11, "type:digital"),
	TYPE_ANALOG(12, "yupr:analog"),
	
    OPTION_PULL_DOWN(21, "pull:down"),
    OPTION_PULL_UP(22, "pull:up"); 

    private final int value;
    private final String name;

    private Options(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name.toUpperCase();        
    }    
    
    public static EnumSet<Options> all() {
        return EnumSet.of(
        		DIRECTION_INPUT,
        		DIRECTION_OUTPUT,
        		TYPE_DIGITAL,
        		TYPE_ANALOG,
        	    OPTION_PULL_DOWN,
        	    OPTION_PULL_UP); 
    }     
}
