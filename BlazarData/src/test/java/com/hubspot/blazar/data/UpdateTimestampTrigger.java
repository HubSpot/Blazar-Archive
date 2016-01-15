package com.hubspot.blazar.data;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateTimestampTrigger extends TriggerAdapter {

  @Override
  public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
    int i = 1;
  }
}
