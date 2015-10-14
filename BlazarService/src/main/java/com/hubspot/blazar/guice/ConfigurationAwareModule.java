package com.hubspot.blazar.guice;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Message;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class ConfigurationAwareModule<Configuration> implements Module {
  private volatile Configuration configuration = null;

  @Override
  public final void configure(Binder binder) {
    configure(decorate(binder), getConfiguration());
  }

  public void setConfiguration(Configuration configuration) {
    checkState(this.configuration == null, "configuration was already set!");
    this.configuration = checkNotNull(configuration, "configuration is null");
  }

  protected Configuration getConfiguration() {
    return checkNotNull(this.configuration, "configuration was not set!");
  }

  protected abstract void configure(final Binder binder, final Configuration configuration);

  private Binder decorate(final Binder binder) {
    return new Binder() {

      @Override
      @SuppressWarnings("unchecked")
      public void install(Module module) {
        if (module instanceof ConfigurationAwareModule<?>) {
          ((ConfigurationAwareModule<Configuration>) module).setConfiguration(getConfiguration());
        }
        binder.install(module);
      }

      @Override
      public void bindInterceptor(Matcher<? super Class<?>> classMatcher,
                                  Matcher<? super Method> methodMatcher,
                                  MethodInterceptor... interceptors) {
        binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
      }

      @Override
      public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        binder.bindScope(annotationType, scope);
      }

      @Override
      public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        return binder.bind(key);
      }

      @Override
      public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return binder.bind(typeLiteral);
      }

      @Override
      public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
      }

      @Override
      public AnnotatedConstantBindingBuilder bindConstant() {
        return binder.bindConstant();
      }

      @Override
      public <T> void requestInjection(TypeLiteral<T> type, T instance) {
        binder.requestInjection(type, instance);
      }

      @Override
      public void requestInjection(Object instance) {
        binder.requestInjection(instance);
      }

      @Override
      public void requestStaticInjection(Class<?>... types) {
        binder.requestStaticInjection(types);
      }

      @Override
      public Stage currentStage() {
        return binder.currentStage();
      }

      @Override
      public void addError(String message, Object... arguments) {
        binder.addError(message, arguments);
      }

      @Override
      public void addError(Throwable t) {
        binder.addError(t);
      }

      @Override
      public void addError(Message message) {
        binder.addError(message);
      }

      @Override
      public <T> Provider<T> getProvider(Key<T> key) {
        return binder.getProvider(key);
      }

      @Override
      public <T> Provider<T> getProvider(Dependency<T> dependency) {
        return binder.getProvider(dependency);
      }

      @Override
      public <T> Provider<T> getProvider(Class<T> type) {
        return binder.getProvider(type);
      }

      @Override
      public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return binder.getMembersInjector(typeLiteral);
      }

      @Override
      public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return binder.getMembersInjector(type);
      }

      @Override
      public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
        binder.convertToTypes(typeMatcher, converter);
      }

      @Override
      public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        binder.bindListener(typeMatcher, listener);
      }

      @Override
      public void bindListener(Matcher<? super Binding<?>> bindingMatcher, ProvisionListener... listeners) {
        binder.bindListener(bindingMatcher, listeners);
      }

      @Override
      public Binder withSource(Object source) {
        return binder.withSource(source);
      }

      @Override
      public Binder skipSources(Class... classesToSkip) {
        return binder.skipSources(classesToSkip);
      }

      @Override
      public PrivateBinder newPrivateBinder() {
        return binder.newPrivateBinder();
      }

      @Override
      public void requireExplicitBindings() {
        binder.requireExplicitBindings();
      }

      @Override
      public void disableCircularProxies() {
        binder.disableCircularProxies();
      }

      @Override
      public void requireAtInjectOnConstructors() {
        binder.requireAtInjectOnConstructors();
      }

      @Override
      public void requireExactBindingAnnotations() {
        binder.requireExactBindingAnnotations();
      }

      @Override
      public void scanModulesForAnnotatedMethods(ModuleAnnotatedMethodScanner scanner) {
        binder.scanModulesForAnnotatedMethods(scanner);
      }
    };
  }
}
