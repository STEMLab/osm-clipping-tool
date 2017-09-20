package io.github.stemlab.configuration.core;

import io.github.stemlab.configuration.WebConfig;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;


/**
 * @brief This class initialize Spring MVC
 * <p>
 * This is the preferred approach for applications that use Java-based Spring configuration.
 * Java-based configuration replaces web.xml
 *
 * @author Bolat Azamat
 * @see AbstractAnnotationConfigDispatcherServletInitializer
 */
public class SpringMvcInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * @return Web configuration class
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /**
     * @return filter which encodes character using UTF-8
     */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        return new Filter[]{characterEncodingFilter};
    }

}
