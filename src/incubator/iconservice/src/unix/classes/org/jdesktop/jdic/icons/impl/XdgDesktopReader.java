/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.jdesktop.jdic.icons.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Contains desktop file functionality. See 
* <a href="http://freedesktop.org/Standards/desktop-entry-spec">desktop-entry-spec</a> for specification.
*/
public class XdgDesktopReader {

    final private Map.Entry[] groups;

    /**
    * Read the contents of a desktop entry file.
    * @param file The file to read.
    */
    public XdgDesktopReader(File file) throws IOException {
        this(new FileInputStream(file));
    }

    /**
    * Read the contents of a desktop entry file.
    * @param input The stream to read.  The stream will be closed before this constructor returns.
    */
    public XdgDesktopReader(InputStream input) throws IOException {
        try {
            groups= readLines(new BufferedReader(new InputStreamReader(input, "UTF-8")));
        }
        finally {
            input.close();
        }
    }

    static private Pattern comment= Pattern.compile("\\s*(#.*)?");
    static private Pattern groupHeader= Pattern.compile("\\s*\\[([^\\]\\x00-\\x1F\\x7F]+)\\]\\s*");
    static private Pattern keyValue= Pattern.compile("\\s*([A-Za-z0-9-]+)(\\[([A-Za-z0-9_@-]+)\\])?\\s*=\\s*([^\\x00-\\x1F\\x7F]*)");

    private Map.Entry[] readLines(BufferedReader reader) throws IOException {

        String groupName= null;
        TreeMap groupMap= new TreeMap();
            // while we build up groups, keep map of group name to Map.Entry[]

        TreeMap entryMap= new TreeMap();
            // while we build up group, keep map of key name to Object[]
            // Object[0]==locale
            // Object[1]==value

        for(int lineNo= 1; ;++lineNo) {
            String line= reader.readLine();
            if(line==null)
                break;

            if(comment.matcher(line).matches())
                continue;
            
            final Matcher groupMatcher= groupHeader.matcher(line);
            if(groupMatcher.matches()) {
                // translate prior Map into Group
                if(groupName!=null) {
                    groupMap.put(groupName, createEntries(entryMap, true));
                    entryMap.clear();
                }

                groupName= groupMatcher.group(1);
                continue;
            }

            final Matcher keyMatcher= keyValue.matcher(line);
            if(keyMatcher.matches()) {
                if(groupName==null)
                    throw new IOException("Improper format, key value before group header at line "+line);

                Locale locale= getLocale(keyMatcher.group(3));
                if(locale!=null && !localeIsEffective(locale))
                    continue;

                String key= keyMatcher.group(1);

                Object[] value= (Object[])entryMap.get(key);
                if(value==null || moreEffective((Locale)value[0], locale)) {
                    entryMap.put(key, new Object[] {locale, replaceEscape(keyMatcher.group(4))});
                }
                continue;
            }

            throw new IOException("Improper format, line "+line+" is not group header, key value, or comment");
        }
        if(groupName!=null) {
            groupMap.put(groupName, createEntries(entryMap, true));
        }

        return createEntries(groupMap, false);
    }

    private Map.Entry[] createEntries(TreeMap entries, boolean isEntry) {
        Map.Entry[] rc= new Map.Entry[entries.size()];
        int i= 0;
        for(Iterator it= entries.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry entry= (Map.Entry)it.next();

            final Object key= entry.getKey();
            final Object value= isEntry 
                                    ?((Object[])entry.getValue())[1] 
                                    :new Group((Map.Entry[])entry.getValue());

            rc[i++]= new Map.Entry() {
                public Object getKey() {
                    return key;
                }
                public Object getValue() {
                    return value;
                }
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException();
                }
                public boolean equals(Object o) {
                    if (o instanceof String)
                        return key.equals((String)o);

                    if (! (o instanceof Map.Entry) )
                        return false;
                    return key.equals(((Map.Entry)o).getKey());
                }
                public int hashCode() {
                    return key.hashCode();
                }
            };
        }
        return rc;
    }

    private static String replaceEscape(String input) {
        StringBuffer output= null;
        for(int lastPosition= 0;; ) {
            int escape= input.indexOf('\\', lastPosition);
            if(escape<0) {
                if(lastPosition==0)
                    return input;

                return output.append(input.substring(lastPosition)).toString();
            }
            if(lastPosition==0)
                output= new StringBuffer();

            output.append(input.substring(lastPosition, escape));

            lastPosition= ++escape;
            if(lastPosition==input.length()) {
                // trailing \
                return output.append('\\').toString();
            }

            switch(input.charAt(lastPosition)){
            case '\\':
                output.append('\\');
                break;
            case 'n':
                output.append('\n');
                break;
            case 'r':
                output.append('\r');
                break;
            case 's':
                output.append(' ');
                break;
            case 't':
                output.append('\t');
                break;
            default:
                output.append('\\');
                continue;
            }
            ++lastPosition;
        }
    }

    private static Locale getLocale(String locale) {
        if(locale==null)
            return null;

        int under= locale.indexOf('_');
        int at= locale.indexOf('@', under+1);

        if(under>-1) {
            if(at>-1) {
                return new Locale(locale.substring(0, under), locale.substring(under+1, at), locale.substring(at+1));
            }
            return new Locale(locale.substring(0, under), locale.substring(under+1));
        }
        if(at>-1) {
            return new Locale(locale.substring(0, at), "", locale.substring(at+1));
        }
        return new Locale(locale);
    }

    private static boolean localeIsEffective(Locale locale) {
        Locale dl= Locale.getDefault();

        if(!locale.getLanguage().equals(dl.getLanguage())) {
            return false;
        }

        String country= locale.getCountry();
        if(country.length()>0 && !country.equals(dl.getCountry())) {
            return false;
        }

        String variant= locale.getVariant();
        if(variant!=null && !variant.equals(dl.getVariant())) {
            return false;
        }

        return true;
    }

    private static boolean moreEffective(Locale oldValue, Locale newValue) {
        if(oldValue==null)
            return true;

        if(newValue==null)
            return false;

        if(oldValue.getCountry().length()>newValue.getCountry().length())
            return false;

        String oldVariant= oldValue.getVariant();
        return oldVariant==null || oldVariant.length()==0;
    }

    private static Comparator entryComparator= new Comparator() {
        public int compare(Object o1, Object o2) {
            if(o1 instanceof Map.Entry)
                o1= ((Map.Entry)o1).getKey();

            if(o2 instanceof Map.Entry)
                o2= ((Map.Entry)o2).getKey();

            return ((Comparable)o1).compareTo(o2);
        }
    };

    
    private static Iterator mapEntryIterator(final Map.Entry[] entries) {
        return new Iterator() {
            private int i= 0;

            // Returns true if the iteration has more elements.
            public boolean hasNext() {
                return i<entries.length;
            }

            // Returns the next element in the iteration.
            public Object next() {
                if(i==entries.length)
                    throw new java.util.NoSuchElementException();
                return entries[i++];
            }

            // Removes from the underlying collection the last element returned by the iterator (optional operation).
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
    * Obtain an iterator over the groups.
    * @return an iterator, each element of which is a Map.Entry.
    *  The key of the Map.Entry element is the name of a Group, the value of the Map.Entry element is the Group.
    */
    public Iterator getGroups() {
        return mapEntryIterator(groups);
    }

    /**
    * Obtain the group of keys for a given group name.
    * @param groupName The name of the group of values.
    * @return null, if no group found with the given name; otherwise, the group of entries.
    */
    public Group getGroup(String groupName) {
        int index= Arrays.binarySearch(groups, groupName, entryComparator);
        return index<0 ?null :(Group)groups[index].getValue();
    }

    static private Pattern semiColon= Pattern.compile(";");

    public static class Group {

        private final Map.Entry[] entries;

        Group(Map.Entry[] entries) {
            this.entries= entries;
        }

        /**
        * Obtain an iterator over the entries.
        * @return an iterator, each element of which is a Map.Entry.
        *  The key of the Map.Entry element is the name of the entry, the value of the Map.Entry element is the value of the entry.
        */
        public Iterator getEntries() {
            return mapEntryIterator(entries);
        }
    
        /**
        * Obtain the value of an entry of a given name.
        * @param entryName The name of the entry desired
        * @return null, if no entry found with the given name; otherwise, the value of the entry.
        */
        public String getString(String entryName) {
            int index= Arrays.binarySearch(entries, entryName, entryComparator);
            return index<0 ?null :entries[index].getValue().toString();
        }

        /**
        * Return the entry as an Iterator of Strings.  This method is equivalent to 
        * @param entryName The key of the entry to return.
        * @param seperators A regular expression representing seperator which splits the entry.
        * @return null, if the entry does not exist; otherwise an Iterator over the Strings of the entry.
        */
        public Iterator getStrings(String entryName, Pattern seperators) {
            final String value= getString(entryName);
            if(value==null)
                return null;

            final String[] values= seperators.split(value);

            return new Iterator() {
    
                private int i= 0;
    
                // Returns true if the iteration has more elements.
                public boolean hasNext() {
                    for(;;++i) {
                        if(i==values.length)
                            return false;
    
                        if(values[i].length()>0)
                            return true;
                    }
                }
    
                // Returns the next element in the iteration.
                public Object next() {
                    for(;;) {
                        if(i==values.length)
                            throw new java.util.NoSuchElementException();
    
                        String v= values[i++];
                        if(v.length()>0)
                            return v;
                    }
                }
    
                // Removes from the underlying collection the last element returned by the iterator (optional operation).
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        /**
        * Return the entry as an Iterator of Strings.  This method is equivalent to 
        * {@link #getStrings(String, Pattern) getStrings(entryName, Pattern.compile(seperators))}.
        * @param entryName The key of the entry to return.
        * @param seperators A {@link Pattern regular expression} representing seperator which splits the entry.
        * @return null, if the entry does not exist; otherwise an Iterator over the Strings of the entry.
        */
        public Iterator getStrings(String entryName, String seperators) {
            return getStrings(entryName, Pattern.compile(seperators));
        }

        /**
        * Return the entry as an Iterator of Strings.  This method is equivalent to 
        * {@link #getStrings(String, String) getStrings(entryName, ";")}.
        * @param entryName The key of the entry to return.
        * @return null, if the entry does not exist; otherwise an Iterator over the Strings of the entry.
        */
        public Iterator getStrings(String entryName) {
            return getStrings(entryName, semiColon);
        }

        /**
        * Return the boolean value of an entry.
        * @param entryName The key of the entry to return.
        * @return null, if the entry does not exist; otherwise the Boolean value of the entry.
        * @throws IllegalArgumentException if the entry is not one of 'false', 'true', '0', or '1'.
        */
        public Boolean getBoolean(String entryName) {
            final String value= getString(entryName);
            if(value==null)
                return null;

            try {
                int v= Integer.parseInt(value);
                if(v==0)
                    return Boolean.FALSE;
                if(v==1)
                    return Boolean.TRUE;
                throw new IllegalArgumentException();
            }
            catch(NumberFormatException ex) {
                if(value.equals("false"))
                    return Boolean.FALSE;
                if(value.equals("true"))
                    return Boolean.TRUE;
                throw new IllegalArgumentException();
            }
        }

        /**
        * Return the integer value of an entry.
        * @param entryName The key of the entry to return.
        * @return null, if the entry does not exist; otherwise the Integer value of the entry.
        * @throws NumberFormatException if the entry is not a parsable number.
        */
        public Integer getInteger(String entryName) throws NumberFormatException {
            final String value= getString(entryName);
            if(value==null)
                return null;

            return new Integer(value);
        }

        /**
        * Return the float value of an entry.
        * @param entryName The key of the entry to return.
        * @return null, if the entry does not exist; otherwise the Float value of the entry.
        * @throws NumberFormatException if the entry is not a parsable number.
        */
        public Float getFloat(String entryName) throws NumberFormatException {
            final String value= getString(entryName);
            if(value==null)
                return null;

            return new Float(value);
        }
    }

}
