package com.hubspot.blazar.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ServiceConfigurationError;

/**
 * Copied and tweaked from JDK because we want to return Class objects and get them from Guice
 */
public final class BlazarServiceLoader<S> implements Iterable<Class<? extends S>> {
  private static final String PREFIX = "META-INF/services/";
  private final Class<S> service;
  private final ClassLoader loader;
  private Map<String,Class<? extends S>> providers = new LinkedHashMap<>();
  private LazyIterator lookupIterator;

  public void reload() {
    providers.clear();
    lookupIterator = new LazyIterator(service, loader);
  }

  private BlazarServiceLoader(Class<S> svc, ClassLoader cl) {
    service = Objects.requireNonNull(svc, "Service interface cannot be null");
    loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
    reload();
  }

  private static void fail(Class<?> service, String msg, Throwable cause) {
    throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
  }

  private static void fail(Class<?> service, String msg) {
    throw new ServiceConfigurationError(service.getName() + ": " + msg);
  }

  private static void fail(Class<?> service, URL u, int line, String msg) {
    fail(service, u + ":" + line + ": " + msg);
  }

  private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names) throws IOException {
    String ln = r.readLine();
    if (ln == null) {
      return -1;
    }

    int ci = ln.indexOf('#');
    if (ci >= 0) {
      ln = ln.substring(0, ci);
    }
    ln = ln.trim();

    int n = ln.length();
    if (n != 0) {
      if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
        fail(service, u, lc, "Illegal configuration-file syntax");
      }

      int cp = ln.codePointAt(0);
      if (!Character.isJavaIdentifierStart(cp)) {
        fail(service, u, lc, "Illegal provider-class name: " + ln);
      }

      for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
        cp = ln.codePointAt(i);
        if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
          fail(service, u, lc, "Illegal provider-class name: " + ln);
        }
      }

      if (!providers.containsKey(ln) && !names.contains(ln)) {
        names.add(ln);
      }
    }
    return lc + 1;
  }

  private Iterator<String> parse(Class<?> service, URL u) {
    List<String> names = new ArrayList<>();

    try (InputStream in = u.openStream()) {
      try (BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
        int lc = 1;
        while ((lc = parseLine(service, u, r, lc, names)) >= 0) {};
      }
    } catch (IOException e) {
      fail(service, "Error reading configuration file", e);
    }

    return names.iterator();
  }

  private class LazyIterator implements Iterator<Class<? extends S>> {
    Class<S> service;
    ClassLoader loader;
    Enumeration<URL> configs = null;
    Iterator<String> pending = null;
    String nextName = null;

    private LazyIterator(Class<S> service, ClassLoader loader) {
      this.service = service;
      this.loader = loader;
    }

    private boolean hasNextService() {
      if (nextName != null) {
        return true;
      }

      if (configs == null) {
        try {
          String fullName = PREFIX + service.getName();
          if (loader == null) {
            configs = ClassLoader.getSystemResources(fullName);
          } else {
            configs = loader.getResources(fullName);
          }
        } catch (IOException x) {
          fail(service, "Error locating configuration files", x);
        }
      }

      while ((pending == null) || !pending.hasNext()) {
        if (!configs.hasMoreElements()) {
          return false;
        }
        pending = parse(service, configs.nextElement());
      }

      nextName = pending.next();
      return true;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends S> nextService() {
      if (!hasNextService()) {
        throw new NoSuchElementException();
      }
      String cn = nextName;
      nextName = null;
      Class<?> c = null;

      try {
        c = Class.forName(cn, false, loader);
      } catch (ClassNotFoundException x) {
        fail(service, "Provider " + cn + " not found");
      }

      if (!service.isAssignableFrom(c)) {
        fail(service, "Provider " + cn  + " not a subtype");
      }

      Class<? extends S> service = (Class<? extends S>) c;
      providers.put(cn, service);
      return service;
    }

    public boolean hasNext() {
      return hasNextService();
    }

    public Class<? extends S> next() {
      return nextService();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public Iterator<Class<? extends S>> iterator() {
    return new Iterator<Class<? extends S>>() {

      Iterator<Class<? extends S>> knownProviders = providers.values().iterator();

      public boolean hasNext() {
        return knownProviders.hasNext() || lookupIterator.hasNext();
      }

      public Class<? extends S> next() {
        return knownProviders.hasNext() ? knownProviders.next() : lookupIterator.next();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <S> BlazarServiceLoader<S> load(Class<S> service, ClassLoader loader) {
    return new BlazarServiceLoader<>(service, loader);
  }

  public static <S> BlazarServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return BlazarServiceLoader.load(service, cl);
  }

  public String toString() {
    return "com.hubspot.blazar.util.BlazarServiceLoader[" + service.getName() + "]";
  }
}
