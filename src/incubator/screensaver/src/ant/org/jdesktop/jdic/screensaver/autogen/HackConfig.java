// Copyright © 2004 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

package org.jdesktop.jdic.screensaver.autogen;

import java.util.ArrayList;

/**
 * JavaBean to store configuration information for a single xscreensaver hack.
 *
 * @author  Mark Roth
 */
public class HackConfig {
    private String name;
    private String label;
    private String description;
    private ArrayList options = new ArrayList();
    
    public java.lang.String getLabel() {
        return label;
    }
    
    public void setLabel(java.lang.String label) {
        this.label = label;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public ArrayList getOptions() {
        return this.options;
    }
    
    public void addOption( Option option ) {
        this.options.add( option );
    }
    
    /**
     * Encapsulates information about a commandline parameter.
     */
    public static class Parameter {
        private String name;
        private int type;
        
        /** The parameter is its own value */
        public static final int TYPE_IS_ARG = 0;
        
        /** Value is the next parameter */
        public static final int TYPE_SEP_ARG = 1;
        
        /** Standard screenhack params for Unix screensavers */
        private static final ArrayList standardParams = new ArrayList();
        static {
            standardParams.add( "root" );
            standardParams.add( "window" );
            standardParams.add( "mono" );
            standardParams.add( "install" );
            standardParams.add( "noinstall" );
            standardParams.add( "visual" );
            standardParams.add( "window-id" );
            // These are added since they already appear in the template
            standardParams.add( "jar" );
            standardParams.add( "class" );
        }
        
        public Parameter( String name, int type ) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getType() {
            return type;
        }
        
        public void setType(int type) {
            this.type = type;
        }
        
        public boolean isStandardScreenhackParameter() {
            return standardParams.contains( getName() );
        }
        
        /**
         * Equal if the names are equal.  This is so we can eliminate
         * duplicates.
         */
        public boolean equals( Object o ) {
            return (o != null) && (o instanceof Parameter) && 
                ((Parameter)o).getName().equals( getName() );
        }
        
        public int hashCode() {
            return getName().hashCode();
        }
        
    }
    
    public abstract static class Option {
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        /**
         * Returns information about the parameters that can be passed to
         * the screensaver based on this configuration option.  Duplicates
         * possible.
         */
        public abstract Parameter[] getParameters();
        
        /**
         * Autodetect the parameter name and type from a String.
         * If the String has no space, the parameter is the entire
         * argument without the dash and the value is the parameter
         * name itself.  If the String has a space, the parameter is
         * the first word without the dash and the value is the next
         * argument.
         */
        protected static Parameter autodetectParameterFromArg( String arg ) {
            Parameter result = null;
            int index = arg.indexOf( ' ' );
            String option;
            String type;
            if( index == -1 ) {
                // No space - assume this is the whole argument
                String a = arg.trim();
                if( a.startsWith( "-" ) ) {
                    a = a.substring( 1 );
                }
                result = new Parameter( a, Parameter.TYPE_IS_ARG );
            }
            else {
                // Space - assume the value is the next argument
                String a = arg.substring( 0, index ).trim();
                if( a.startsWith( "-" ) ) {
                    a = a.substring( 1 );
                }
                result = new Parameter( a, Parameter.TYPE_SEP_ARG );
            }
            return result;
        }
    }
    
    public abstract static class LabelOption extends Option {
        private String label;
        
        public java.lang.String getLabel() {
            return label;
        }
        
        public void setLabel(java.lang.String label) {
            this.label = label;
        }
    }
    
    public static class CommandOption extends LabelOption {
        private String arg;
        
        public String getArg() {
            return arg;
        }
        
        public void setArg(String arg) {
            this.arg = arg;
        }
        
        public Parameter[] getParameters() {
            Parameter[] result = new Parameter[1];
            result[0] = autodetectParameterFromArg( arg );
            return result;
        }
    }
    
    public static class BooleanOption extends LabelOption {
        private String argSet;
        private String argUnset;
        
        public String getArgSet() {
            return argSet;
        }
        
        public void setArgSet(String argSet) {
            this.argSet = argSet;
        }
        
        public String getArgUnset() {
            return argUnset;
        }
        
        public void setArgUnset(String argUnset) {
            this.argUnset = argUnset;
        }
        
        public Parameter[] getParameters() {
            Parameter[] result;
            Parameter set = null;
            Parameter unset = null;
            if( argSet != null ) {
                set = autodetectParameterFromArg( argSet );
            }
            if( argUnset != null ) {
                unset = autodetectParameterFromArg( argUnset );
            }
            if( set == null ) {
                if( unset == null ) {
                    result = new Parameter[0];
                }
                else {
                    result = new Parameter[1];
                    result[0] = unset;
                }
            }
            else {
                if( unset == null ) {
                    result = new Parameter[1];
                    result[0] = set;
                }
                else {
                    result = new Parameter[2];
                    result[0] = set;
                    result[1] = unset;
                }
            }
            
            return result;
        }
    }
    
    public static class NumberOption extends LabelOption {
        private String type;
        private String arg;
        private String lowLabel;
        private String highLabel;
        private String low;
        private String high;
        private String defaultValue;
        private String convert;
        
        public java.lang.String getArg() {
            return arg;
        }
        
        public void setArg(java.lang.String arg) {
            this.arg = arg;
        }
        
        public java.lang.String getConvert() {
            return convert;
        }
        
        public void setConvert(java.lang.String convert) {
            this.convert = convert;
        }
        
        public java.lang.String getDefaultValue() {
            return defaultValue;
        }
        
        public void setDefaultValue(java.lang.String defaultValue) {
            this.defaultValue = defaultValue;
        }
        
        public java.lang.String getHigh() {
            return high;
        }
        
        public void setHigh(java.lang.String high) {
            this.high = high;
        }
        
        public java.lang.String getHighLabel() {
            return highLabel;
        }
        
        public void setHighLabel(java.lang.String highLabel) {
            this.highLabel = highLabel;
        }
        
        public java.lang.String getLow() {
            return low;
        }
        
        public void setLow(java.lang.String low) {
            this.low = low;
        }
        
        public java.lang.String getLowLabel() {
            return lowLabel;
        }
        
        public void setLowLabel(java.lang.String lowLabel) {
            this.lowLabel = lowLabel;
        }
        
        public java.lang.String getType() {
            return type;
        }
        
        public void setType(java.lang.String type) {
            this.type = type;
        }
        
        public Parameter[] getParameters() {
            Parameter[] result = new Parameter[1];
            result[0] = autodetectParameterFromArg( arg );
            return result;
        }
    }
    
    public static class SelectOption extends Option {
        private ArrayList options = new ArrayList();
        
        public ArrayList getOptions() {
            return options;
        }
        
        public void addOption( Option option ) {
            this.options.add( option );
        }
    
        public Parameter[] getParameters() {
            ArrayList result = new ArrayList();
            for( int i = 0; i < options.size(); i++ ) {
                OptionOption o = (OptionOption)options.get( i );
                String argSet = o.getArgSet();
                if( argSet != null ) {
                    result.add( o.getParameters()[0] );
                }
            }
            return (Parameter[])result.toArray( new Parameter[result.size()] );
        }
        
        public static class OptionOption extends LabelOption {
            private String argSet;
            
            public java.lang.String getArgSet() {
                return argSet;
            }
            
            public void setArgSet(java.lang.String argSet) {
                this.argSet = argSet;
            }
            
            public Parameter[] getParameters() {
                Parameter[] result = new Parameter[1];
                result[0] = autodetectParameterFromArg( argSet );
                return result;
            }
        }
    }
    
    public static class StringOption extends LabelOption {
        private String arg;
        
        public java.lang.String getArg() {
            return arg;
        }
        
        public void setArg(java.lang.String arg) {
            this.arg = arg;
        }
        
        public Parameter[] getParameters() {
            Parameter[] result = new Parameter[1];
            result[0] = autodetectParameterFromArg( arg );
            return result;
        }
    }
    
    public static class FileOption extends LabelOption {
        private String arg;
        
        public java.lang.String getArg() {
            return arg;
        }
        
        public void setArg(java.lang.String arg) {
            this.arg = arg;
        }
        
        public Parameter[] getParameters() {
            Parameter[] result = new Parameter[1];
            result[0] = autodetectParameterFromArg( arg );
            return result;
        }
    }
    
}
