package com.hubspot.blazar.base.listener;

import com.hubspot.blazar.base.ModuleBuild;

public interface ModuleBuildListener {
  void buildChanged(ModuleBuild build) throws Exception;
}
