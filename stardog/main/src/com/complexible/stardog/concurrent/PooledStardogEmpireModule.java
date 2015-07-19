package com.complexible.stardog.concurrent;

import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.util.EmpireModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Created by Anand on 3/3/15.
 */
public class PooledStardogEmpireModule  extends AbstractModule implements EmpireModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), DataSourceFactory.class)
                .addBinding().to(PooledStardogEmpireDataSourceFactory.class);
    }
}
