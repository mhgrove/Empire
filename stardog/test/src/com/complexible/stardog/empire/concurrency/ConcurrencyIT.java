package com.complexible.stardog.empire.concurrency;

import com.complexible.stardog.empire.concurrency.entity.Book;
import com.complexible.stardog.empire.concurrency.fixture.BookFixture;
import com.complexible.stardog.empire.concurrency.util.EmpireConnection;
import org.junit.Test;
import static org.junit.Assert.*;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by anand on 30/03/15.
 */
public class ConcurrencyIT {

    @Test
    public void testDefaultStardogModule() {
        final EmpireConnection stardogConnection = EmpireConnection.create();
        int n = 4;
        AtomicInteger atomicInteger = test(stardogConnection, n, n, "default");
        try {
            Thread.sleep(n * 3 * 1000); //Let's sleep for n x 3 seconds to check if any entity is persisted
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Default module supports com.complexible.stardog.empire.concurrency too!!!", atomicInteger.get() != n);
    }

    @Test
    public void testPooledConcurrentStardogModule() {
        final EmpireConnection stardogConnection = EmpireConnection.createPooledEmpireConnection();
        int n = 4;
        AtomicInteger atomicInteger = test(stardogConnection, n, n, "pooled");
        try {
            Thread.sleep(n * 3 * 1000); //Let's sleep for n x 3 seconds to check if any entity is persisted
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Pooled Concurrent module too doesn't support com.complexible.stardog.empire.concurrency", atomicInteger.get() == n);
    }


    private AtomicInteger test(final EmpireConnection empireConnection, int poolSize, final int numEntities, final String tag) {
        final ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        final AtomicInteger completedCount = new AtomicInteger(0);
        for (int i = 0; i < numEntities; i++) {
            final int t = i;
            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    Book aNewBook = BookFixture.create();
                                    empireConnection.saveObject(aNewBook);
                                    System.out.println("["+tag+"]Saved object" + t);
                                    final int cc = completedCount.incrementAndGet();
                                    if(cc == numEntities) {
                                        executor.shutdown();
                                    }
                                }
                            }
            );

        }
        return completedCount;
    }

}
