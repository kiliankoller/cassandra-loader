/*
 * Copyright 2015 Brian Hess
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.loader.parser;

import java.lang.Character;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.IndexOutOfBoundsException;
import java.io.StringReader;
import java.io.IOException;
import java.text.ParseException;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidTypeException;

public abstract class AbstractParser implements Parser {
    public abstract Object parse(String toparse) throws ParseException;
    public abstract String format(Row row, int index) throws IndexOutOfBoundsException, InvalidTypeException;

    public Object parse(IndexedLine il, String nullString, Character delim, 
			Character escape, Character quote, boolean last)
	throws IOException, ParseException {
	if (last)
	    return parse(prepareToParse(il.remaining(), nullString, quote));
	return parse(getQuotedOrUnquoted(il, nullString, delim, escape, quote));
    }

    public String prepareToParse(String retstring, String nullString, Character quote) {
	if (retstring.startsWith(quote.toString()) 
	    && retstring.endsWith(quote.toString()))
	    retstring = retstring.substring(1, retstring.length() - 1);
	retstring = retstring.trim();
	if (nullString.equalsIgnoreCase(retstring))
	    return null;	
	return retstring;
    }

    public String getQuotedOrUnquoted(IndexedLine il, String nullString,
				      Character delim, Character escape, 
				      Character quote) 
	throws IOException, ParseException {
	String retstring;
	if (null == delim) {
	    return null;
	}
	if (!il.hasNext())
	    return null;
	char c = il.getNext();
	if (c == delim) {
	    retstring = "";
	}
	else {
	    StringBuilder sb = new StringBuilder().append(c);
	    String s = extractUntil(il, delim, escape, quote, (c == quote));
	    if (null == s) {
		return null;
	    }
	    retstring = sb.append(s).toString();
	}
	return prepareToParse(retstring, nullString, quote);
    }

    public String extractUntil(IndexedLine il, Character delim, 
			       Character escape, Character quote,
			       boolean inquote)
	throws IOException, ParseException {
	if (null == delim) {
	    return null;
	}
	StringBuilder sb = new StringBuilder();
	char c;
	while (il.hasNext()) {
	    c = il.getNext();
	    if ((c == delim) && (!inquote)) {
		break;
	    }
	    sb.append(c);
	    if (null != quote) {
		if (c == quote) {
		    inquote = false;
		}
	    }
	    if (null != escape) {
		if (c == escape) {
		    c = il.getNext();
		    sb.append(Character.toChars(c));
		}
	    }
	}
	return sb.toString();
    }
}
