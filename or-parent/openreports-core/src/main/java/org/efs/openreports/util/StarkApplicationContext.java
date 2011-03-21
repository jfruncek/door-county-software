package org.efs.openreports.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

/**
 * Just a little decorator class to simplify getting beans. ApplicationContextAware beans can
 * wrapper a real ApplicationContext with this to make getting beans a bit cleaner.
 * 
 * Note that there's a number of SupressWarnings directives because ApplicationContext requires raw
 * types.
 * 
 * @author mconner
 * 
 */
public class StarkApplicationContext implements ApplicationContext {
    ApplicationContext applicationContext;

    
    @SuppressWarnings("unchecked")
    public <T> T getSpringBean(String beanId, Class<T> expectedType) {
        // TODO: add some logic to deal with exceptions??
        return (T) applicationContext.getBean( beanId, expectedType );
    }
    
    public ApplicationContext getUnderlyingApplicationContext() {
        return applicationContext;
    }

// Delegate methods follow     
    
    public boolean containsBean( String arg0 ) {
        return applicationContext.containsBean( arg0 );
    }

    public boolean containsBeanDefinition( String arg0 ) {
        return applicationContext.containsBeanDefinition( arg0 );
    }

    public boolean containsLocalBean( String arg0 ) {
        return applicationContext.containsLocalBean( arg0 );
    }

    public String[] getAliases( String arg0 ) {
        return applicationContext.getAliases( arg0 );
    }

    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return applicationContext.getAutowireCapableBeanFactory();
    }

    @SuppressWarnings("rawtypes")
    public Object getBean( String arg0, Class arg1 ) throws BeansException {
        return applicationContext.getBean( arg0, arg1 );
    }

    public Object getBean( String arg0 ) throws BeansException {
        return applicationContext.getBean( arg0 );
    }

    public int getBeanDefinitionCount() {
        return applicationContext.getBeanDefinitionCount();
    }

    public String[] getBeanDefinitionNames() {
        return applicationContext.getBeanDefinitionNames();
    }

    @SuppressWarnings("rawtypes")
    public String[] getBeanNamesForType( Class arg0, boolean arg1, boolean arg2 ) {
        return applicationContext.getBeanNamesForType( arg0, arg1, arg2 );
    }

    @SuppressWarnings("rawtypes")
    public String[] getBeanNamesForType( Class arg0 ) {
        return applicationContext.getBeanNamesForType( arg0 );
    }

    @SuppressWarnings("rawtypes")
    public Map getBeansOfType( Class arg0, boolean arg1, boolean arg2 ) throws BeansException {
        return applicationContext.getBeansOfType( arg0, arg1, arg2 );
    }

    @SuppressWarnings("rawtypes")
    public Map getBeansOfType( Class arg0 ) throws BeansException {
        return applicationContext.getBeansOfType( arg0 );
    }

    public ClassLoader getClassLoader() {
        return applicationContext.getClassLoader();
    }

    public String getDisplayName() {
        return applicationContext.getDisplayName();
    }

    public String getMessage( MessageSourceResolvable arg0, Locale arg1 ) throws NoSuchMessageException {
        return applicationContext.getMessage( arg0, arg1 );
    }

    public String getMessage( String arg0, Object[] arg1, Locale arg2 ) throws NoSuchMessageException {
        return applicationContext.getMessage( arg0, arg1, arg2 );
    }

    public String getMessage( String arg0, Object[] arg1, String arg2, Locale arg3 ) {
        return applicationContext.getMessage( arg0, arg1, arg2, arg3 );
    }

    public ApplicationContext getParent() {
        return applicationContext.getParent();
    }

    public BeanFactory getParentBeanFactory() {
        return applicationContext.getParentBeanFactory();
    }

    public Resource getResource( String arg0 ) {
        return applicationContext.getResource( arg0 );
    }

    public Resource[] getResources( String arg0 ) throws IOException {
        return applicationContext.getResources( arg0 );
    }

    public long getStartupDate() {
        return applicationContext.getStartupDate();
    }

    @SuppressWarnings("rawtypes")
    public Class getType( String arg0 ) throws NoSuchBeanDefinitionException {
        return applicationContext.getType( arg0 );
    }

    public boolean isPrototype( String arg0 ) throws NoSuchBeanDefinitionException {
        return applicationContext.isPrototype( arg0 );
    }

    public boolean isSingleton( String arg0 ) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton( arg0 );
    }

    @SuppressWarnings("rawtypes")
    public boolean isTypeMatch( String arg0, Class arg1 ) throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch( arg0, arg1 );
    }

    public void publishEvent( ApplicationEvent arg0 ) {
        applicationContext.publishEvent( arg0 );
    }

    public StarkApplicationContext( ApplicationContext applicationContext ) {
        this.applicationContext = applicationContext;
    }

    public Object getBean(String paramString, Object[] paramArrayOfObject) throws BeansException {
        return this.applicationContext.getBean(paramString, paramArrayOfObject);
    }

    public String getId() {
        return this.applicationContext.getId();
    }
    
    

}
