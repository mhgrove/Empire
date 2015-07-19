package com.complexible.stardog.empire.concurrency.util;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;


import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.util.EmpireModule;
import com.complexible.stardog.concurrent.PooledStardogEmpireModule;
import com.complexible.stardog.empire.StardogEmpireModule;


/**
 * EmpireConnection - Handles the mapping between Stardog and Ontology Classes
 * Performing operations on Stardog with Empire
 *
 * @author Anand
 */
public class EmpireConnection {

    EntityManager aManager;

    private EmpireConnection(EmpireModule empireModule) {
        try {
            PropertiesConfigReader configReader = new PropertiesConfigReader();
            EmpireConfiguration conf = configReader.read(EmpireConnection.class.getResourceAsStream("/stardog.properties"));
//            System.setProperty("empire.configuration.file", "stardog/test/resources/stardog.properties");
            Empire.init(conf, empireModule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PersistenceProvider p = Empire.get().persistenceProvider();
        EntityManagerFactory managerfactory = p.createEntityManagerFactory("socure", null);
        aManager = managerfactory.createEntityManager();

        System.out.println("Connection Creation is Done");

    }

    //Factory methods
    public static EmpireConnection create() {
        //Create StardogModule based EmpireConnection by default
        return create(new StardogEmpireModule());
    }

    public static EmpireConnection createPooledEmpireConnection() {
        return create(new PooledStardogEmpireModule());
    }

    public static EmpireConnection create(EmpireModule empireModule) {
        //Create StardogModule based EmpireConnection by default
        return new EmpireConnection(empireModule);
    }

    /**
     * Saves the Entity to OWLIM
     *
     * @param entity
     */
    public void saveObject(Object entity) {
        this.aManager.persist(entity);
        this.aManager.flush();
        this.aManager.refresh(entity);
    }


}
