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

package org.jdesktop.jdic.spi;

/**
 * The <code>ProviderException</code> class indicates an exception instantiating a provider.
 */
public class ProviderException extends RuntimeException {
    /**
    * Construct a new runtime exception with the specified detail message. 
    * The cause is not initialized, and may be initialized by a call to {@link Throwable#initCause initCause}
    */
    public ProviderException(String message) {
        super(message);
    }


    /**
    * Construct a new runtime exception with the specified cause. This constructor wraps another throwable.
    */
    public ProviderException(Throwable cause) {
        super(cause);
    }
}

