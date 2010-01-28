package com.clarkparsia.empire.spi;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class EmpirePersistenceProvider implements PersistenceProvider {
    // TODO: should we keep factories created so that to factories created w/ the same name are == ?

    private Set<DataSourceFactory> mFactories = new HashSet<DataSourceFactory>();
    private Map<String, String> mContainerConfig;

    private static final String FACTORY = "factory";

    @Inject
    public EmpirePersistenceProvider(Set<DataSourceFactory> theFactories,
                              @Named("ec") Map<String, String> theContainerConfig) {
        mFactories = theFactories;
        mContainerConfig = theContainerConfig;
    }

    public EntityManagerFactory createEntityManagerFactory(final String theUnitName, final Map theMap) {
        Map<String, String> aConfig = new HashMap<String, String>(mContainerConfig);
        aConfig.putAll(mapOfStrings(theMap));

        if (!aConfig.containsKey(FACTORY)) {
            return null;
        }

        final String aName = aConfig.get(FACTORY);

        for (DataSourceFactory aFactory  : mFactories) {
            if (aFactory.getClass().getName().equals(aName)) {
                return new EntityManagerFactoryImpl(aFactory);
            }
        }

        return null;
    }

    public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo thePersistenceUnitInfo,
                                                                    final Map theMap) {
        // TODO: there's a lot more options on PersistenceUnitInfo that we can use here.
        return createEntityManagerFactory(thePersistenceUnitInfo.getPersistenceUnitName(), theMap);
    }

    private Map<String, String> mapOfStrings(Map theMap) {
        Map<String, String> aMap = new HashMap<String, String>();

        for (Object aKey : theMap.keySet()) {
            aMap.put(aKey.toString(), theMap.get(aKey).toString());
        }

        return aMap;
    }

    private Map<String, String> subMap(String thePrefix, Map<String, String> theMap) {
        Map<String, String> aSubMap = new HashMap<String, String>();

        for (String aKey : theMap.keySet()) {
            if (aKey.startsWith(thePrefix)) {
                aSubMap.put(aKey.substring(thePrefix.length() + 1), theMap.get(aKey));
            }
        }

        return aSubMap;
    }

    public static void main(String[] args) {

//        Injector injector = Guice.createInjector(new EmpireOptions.DefaultEmpireModule());
//
//        TestDI test = injector.createChildInjector(new CustomModule()).getInstance(TestDI.class);
//
//        System.err.println(test.mgr);

//        System.err.println(injector.createChildInjector(new CustomModule()).getInstance(EntityManagerFactoryImpl.class).mDataSourceFactoryProvider);
//
//        System.err.println(injector.createChildInjector(new CustomModule()).getInstance(EntityManagerFactoryImpl.class).mDataSourceFactoryProvider.get());
//
//        System.err.println(((EntityManagerFactoryImpl)injector.createChildInjector(new CustomModule()).getInstance(EmpirePersistenceProvider.class).createEntityManagerFactory("", new HashMap())).mDataSourceFactoryProvider.get());

        Map<String, String> m = new HashMap<String, String>();
        Map<String, String> sub = new HashMap<String, String>();
        String prefix = "testdb";

        m.put("testdb.one", "one");
        m.put("testdb.two", "two");
        m.put("three.testdb", "three");
        m.put("test.db.three.testdb", "four");

        for (String aKey : m.keySet()) {
            if (aKey.startsWith(prefix)) {
                sub.put(aKey.substring(prefix.length() + 1), m.get(aKey));
            }
        }

        System.err.println(sub);
    }

    public static class TestDI {
        @PersistenceContext(properties={@PersistenceProperty(name="property.name", value="property value")
})
        EntityManager mgr;
    }
}
