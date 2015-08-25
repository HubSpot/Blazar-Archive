package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.inject.BindingAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class HostUtils {
  private static final Logger LOG = LoggerFactory.getLogger(HostUtils.class);

  @BindingAnnotation
  @Retention(RUNTIME)
  @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
  public @interface Host {}

  @BindingAnnotation
  @Retention(RUNTIME)
  @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
  public @interface Port {}

  public static Optional<String> getHostName() {
    try {
      InetAddress address = InetAddress.getLocalHost();
      return Optional.fromNullable(address.getHostName());
    } catch (Throwable t) {
      LOG.error("Error find host name", t);
      return Optional.absent();
    }
  }

  public static Optional<String> getHostAddress() {
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface current = interfaces.nextElement();
        if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
          continue;
        }

        Enumeration<InetAddress> addresses = current.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress currentAddr = addresses.nextElement();
          if (!currentAddr.isLoopbackAddress() && currentAddr instanceof Inet4Address) {
            return Optional.of(currentAddr.getHostAddress());
          }
        }
      }
    } catch (Throwable t) {
      LOG.error("Error finding host address", t);
    }

    return Optional.absent();
  }

  private HostUtils() {
    throw new AssertionError();
  }
}
