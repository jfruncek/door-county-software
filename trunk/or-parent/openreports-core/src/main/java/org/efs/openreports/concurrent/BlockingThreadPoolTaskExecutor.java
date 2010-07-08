package org.efs.openreports.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * Virtual duplication of {@link ThreadPoolTaskExecutor}. The major difference is that
 * <ul>
 * <li>We use our own custom BlockingThreadPoolExecutor instead of ThreadPoolExecutor</li>
 * <li>We call executeWithBlock, so that it will block when the queue is at capacity, rather than throw an exception</li>
 * <li>We initialize the executor with a ArrayBlockingQueue, rather than a LinkedBlockingQueue, 
 *     so that there is a max capacity</li>
 * </ul>
 * 
 */
public class BlockingThreadPoolTaskExecutor implements SchedulingTaskExecutor, Executor, BeanNameAware,
        InitializingBean, DisposableBean {

    protected final Log logger = LogFactory.getLog( getClass() );

    private final Object poolSizeMonitor = new Object();

    private int corePoolSize = 1;

    private int maxPoolSize = Integer.MAX_VALUE;

    private int keepAliveSeconds = 60;

    private int queueCapacity = Integer.MAX_VALUE;

    private ThreadFactory threadFactory = Executors.defaultThreadFactory();

    private BlockingThreadPoolExecutor.BlockingRejectedExecutionHandler rejectedExecutionHandler = new BlockingThreadPoolExecutor.AbortPolicy();

    private String beanName;

    private BlockingThreadPoolExecutor threadPoolExecutor;

    /**
     * Set the ThreadPoolExecutor's core pool size. Default is 1.
     * <p>
     * <b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setCorePoolSize( int corePoolSize ) {
        synchronized( this.poolSizeMonitor ) {
            this.corePoolSize = corePoolSize;
            if( this.threadPoolExecutor != null ) {
                this.threadPoolExecutor.setCorePoolSize( corePoolSize );
            }
        }
    }

    /**
     * Return the ThreadPoolExecutor's core pool size.
     */
    public int getCorePoolSize() {
        synchronized( this.poolSizeMonitor ) {
            return this.corePoolSize;
        }
    }

    /**
     * Set the ThreadPoolExecutor's maximum pool size. Default is <code>Integer.MAX_VALUE</code>.
     * <p>
     * <b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setMaxPoolSize( int maxPoolSize ) {
        synchronized( this.poolSizeMonitor ) {
            this.maxPoolSize = maxPoolSize;
            if( this.threadPoolExecutor != null ) {
                this.threadPoolExecutor.setMaximumPoolSize( maxPoolSize );
            }
        }
    }

    /**
     * Return the ThreadPoolExecutor's maximum pool size.
     */
    public int getMaxPoolSize() {
        synchronized( this.poolSizeMonitor ) {
            return this.maxPoolSize;
        }
    }

    /**
     * Set the ThreadPoolExecutor's keep-alive seconds. Default is 60.
     * <p>
     * <b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setKeepAliveSeconds( int keepAliveSeconds ) {
        synchronized( this.poolSizeMonitor ) {
            this.keepAliveSeconds = keepAliveSeconds;
            if( this.threadPoolExecutor != null ) {
                this.threadPoolExecutor.setKeepAliveTime( keepAliveSeconds, TimeUnit.SECONDS );
            }
        }
    }

    /**
     * Return the ThreadPoolExecutor's keep-alive seconds.
     */
    public int getKeepAliveSeconds() {
        synchronized( this.poolSizeMonitor ) {
            return this.keepAliveSeconds;
        }
    }

    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue. Default is
     * <code>Integer.MAX_VALUE</code>.
     * <p>
     * Any positive value will lead to a LinkedBlockingQueue instance; any other value will lead to
     * a SynchronousQueue instance.
     * 
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    public void setQueueCapacity( int queueCapacity ) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Set the ThreadFactory to use for the ThreadPoolExecutor's thread pool. Default is the
     * ThreadPoolExecutor's default thread factory.
     * 
     * @see java.util.concurrent.Executors#defaultThreadFactory()
     */
    public void setThreadFactory( ThreadFactory threadFactory ) {
        this.threadFactory = ( threadFactory != null ? threadFactory : Executors.defaultThreadFactory() );
    }

    /**
     * Set the RejectedExecutionHandler to use for the ThreadPoolExecutor. Default is the
     * ThreadPoolExecutor's default abort policy.
     * 
     * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
     */
    public void setRejectedExecutionHandler( BlockingThreadPoolExecutor.BlockingRejectedExecutionHandler rejectedExecutionHandler ) {
        this.rejectedExecutionHandler =
                ( rejectedExecutionHandler != null ? rejectedExecutionHandler : new BlockingThreadPoolExecutor.AbortPolicy() );
    }

    public void setBeanName( String name ) {
        this.beanName = name;
    }

    /**
     * Calls <code>initialize()</code> after the container applied all property values.
     * 
     * @see #initialize()
     */
    public void afterPropertiesSet() {
        initialize();
    }

    /**
     * Creates the BlockingQueue and the ThreadPoolExecutor.
     * 
     * @see #createQueue
     */
    public void initialize() {
        if( logger.isInfoEnabled() ) {
            logger
                    .info( "Initializing ThreadPoolExecutor"
                            + ( this.beanName != null ? " '" + this.beanName + "'" : "" ) );
        }
        BlockingQueue<Runnable> queue = createQueue( this.queueCapacity );
        this.threadPoolExecutor =
                new BlockingThreadPoolExecutor( this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
                        queue, this.threadFactory, this.rejectedExecutionHandler );
    }

    /**
     * Create the BlockingQueue to use for the ThreadPoolExecutor.
     * <p>
     * A LinkedBlockingQueue instance will be created for a positive capacity value; a
     * SynchronousQueue else.
     * 
     * @param queueCapacity the specified queue capacity
     * @return the BlockingQueue instance
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    protected BlockingQueue<Runnable> createQueue( int queueCapacity ) {
        if( queueCapacity > 0 ) {
            return new ArrayBlockingQueue<Runnable>(queueCapacity);
            // return new LinkedBlockingQueue( queueCapacity );
        } else {
            return new SynchronousQueue<Runnable>();
        }
    }

    /**
     * Return the underlying ThreadPoolExecutor for native access.
     * 
     * @return the underlying ThreadPoolExecutor (never <code>null</code>)
     * @throws IllegalStateException if the ThreadPoolTaskExecutor hasn't been initialized yet
     */
    public BlockingThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        Assert.state( this.threadPoolExecutor != null, "ThreadPoolTaskExecutor not initialized" );
        return this.threadPoolExecutor;
    }

    /**
     * Implementation of both the JDK 1.5 Executor interface and the Spring TaskExecutor interface,
     * delegating to the ThreadPoolExecutor instance.
     * 
     * @see java.util.concurrent.Executor#execute(Runnable)
     * @see org.springframework.core.task.TaskExecutor#execute(Runnable)
     */
    public void execute( Runnable task ) {
        BlockingThreadPoolExecutor executor = getThreadPoolExecutor();
        try {
            executor.executeWithBlock( task );
        } catch( RejectedExecutionException ex ) {
            throw new TaskRejectedException( "Executor [" + executor + "] did not accept task: " + task, ex );
        }
    }

    /**
     * This task executor prefers short-lived work units.
     */
    public boolean prefersShortLivedTasks() {
        return true;
    }

    /**
     * Return the current pool size.
     * 
     * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
     */
    public int getPoolSize() {
        return getThreadPoolExecutor().getPoolSize();
    }

    /**
     * Return the number of currently active threads.
     * 
     * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
     */
    public int getActiveCount() {
        return getThreadPoolExecutor().getActiveCount();
    }

    /**
     * Calls <code>shutdown</code> when the BeanFactory destroys the task executor instance.
     * 
     * @see #shutdown()
     */
    public void destroy() {
        shutdown();
    }

    /**
     * Perform a shutdown on the ThreadPoolExecutor.
     * 
     * @see java.util.concurrent.ThreadPoolExecutor#shutdown()
     */
    public void shutdown() {
        if( logger.isInfoEnabled() ) {
            logger.info( "Shutting down ThreadPoolExecutor"
                    + ( this.beanName != null ? " '" + this.beanName + "'" : "" ) );
        }
        this.threadPoolExecutor.shutdown();
    }

}
