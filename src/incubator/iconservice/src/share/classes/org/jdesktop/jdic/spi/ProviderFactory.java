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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The <code>ProviderFactory</code> class provides standardized methods to instantiate a provider.
 * The ProviderFactory includes methods to instantiate and track a provider.
 */
public abstract class ProviderFactory {

    private Provider provider;
    private String propertyName;
    boolean isSingleton = true;

    /**
     * Construct a ProviderFactory that uses a System property to instantiate a Provider.
     * The value of the system property is used as a classname of a class to instantiate.
     * @param propertyName The name of the system property to query when creating a provider.
     * @param isSingleton true, if only one instance of this provider is to be created; false, 
     * if a new instance of the provider should be created for each call to getProvider.
     */
    public ProviderFactory(String propertyName, boolean isSingleton) {
        this.propertyName= propertyName;
        this.isSingleton = isSingleton;
    }

    /**
     * Set the provider for a service.  This method may only be called for singleton 
     * instances. If this ProviderFactory was constructed as a non-singleton instance,
     * invoking this method will cause an UnsupportedOperationException.
     * Once the service has been instantiated, changing the provider has no effect.
     * @param provider The provider which is to be used by the service.
     */
    public void setProvider(Provider provider) {
        if (isSingleton) {
            this.provider= provider;
        }
        else {
            throw new UnsupportedOperationException("cannot set multi-instance provider");
        }
    }

    /**
     * Obtain the provider for a service.  If this ProviderFacotry is configured
     * to support Singleton and no provider was previously set, or this 
     * ProviderFactory is configured to support Multiton, then it will use
     * the system property given in the constructor to create a potential
     * provider.  If that provider cannot be created, or returns false when
     * initialize is invoked, then the classname returned from
     * {@link #getPlatformProviderClassName() getPlatformProviderClassName}
     * will be used to create a potential provider.  If that provider cannot be
     * created, or returns false when initialize is invoked, then a ProviderException
     * will be thrown.
     * @return The provider which was previously set, or a newly created provider.
     */
    public Provider getProvider() {
        if (((this.isSingleton) && (provider == null)) || (!this.isSingleton)) {
            createProvider();
        }
        return provider;
    }

    /**
     * Obtain the class name of potential providers for a service.
     * This method is called by the ProviderFactory when it is attempting to 
     * instantiate a provider.
     * @return The potential provider class names
     */
    protected abstract String getPlatformProviderClassName();

    private synchronized void createProvider() {
            AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                // need permission: RuntimePermission("getClassLoader")
                final ClassLoader cl = Thread.currentThread()
                        .getContextClassLoader();

                // need permission: PropertyPermission("${propertyName}", "read")
                String providerClassName= System.getProperty(propertyName);

                if (providerClassName==null)
                    providerClassName= getPlatformProviderClassName();

                if(loadProvider(cl, providerClassName))
                    return null;

                throw new ProviderException("Provider "+providerClassName+" can not be found");
            }
        });
    }

    private boolean loadProvider(ClassLoader cl, String providerClassName) {
        try {
            final Provider ip= (Provider)cl.loadClass(providerClassName).newInstance();
            if(!ip.initialize()) {
                return false;
            }

            provider= ip;
            return true;
        } catch(RuntimeException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new ProviderException(ex);
        }
    }
}
