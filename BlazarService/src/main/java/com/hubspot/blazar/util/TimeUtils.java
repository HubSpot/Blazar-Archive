package com.hubspot.blazar.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeUtils {

  public static long nowInUtcMillis() {
    return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000L;
  }
}
