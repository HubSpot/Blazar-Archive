package com.hubspot.blazar;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;

public class CorsBundle implements Bundle {
  private static final String FILTER_NAME = "Cross Origin Request Filter";

  @Override
  public void initialize(Bootstrap<?> bootstrap) {}

  @Override
  public void run(Environment environment) {
    final Filter corsFilter = new CrossOriginFilter();
    final FilterConfig corsFilterConfig = new FilterConfig() {

      @Override
      public String getFilterName() {
        return FILTER_NAME;
      }

      @Override
      public ServletContext getServletContext() {
        return null;
      }

      @Override
      public String getInitParameter(final String name) {
        return null;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return Iterators.asEnumeration(Collections.<String>emptyIterator());
      }
    };

    try {
      corsFilter.init(corsFilterConfig);
    } catch (final Exception e) {
      throw Throwables.propagate(e);
    }

    environment.servlets().addFilter(FILTER_NAME, corsFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
  }
}
