package com.hubspot.blazar.data.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.Event;
import com.hubspot.blazar.data.dao.EventDao;

import java.util.List;

public class EventService {

  private EventDao eventDao;

  @Inject
  public EventService(EventDao eventDao) {
    this.eventDao = eventDao;
  }

  public List<BuildState> fetch(String username, String gitHubHost, long since) {
    return eventDao.getAllEventsForUser(username.toLowerCase(), gitHubHost, since);
  }

  public void add(Event event) {
    int affectedRows = eventDao.insert(event);
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }

}
