package com.hubspot.blazar.test.base.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.h2.tools.TriggerAdapter;

import com.google.common.base.Objects;

public class UpdateTimestampTrigger extends TriggerAdapter {

  @Override
  public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
    if (Objects.equal(oldRow.getTimestamp("updatedTimestamp"), newRow.getTimestamp("updatedTimestamp"))) {
      String query = String.format("UPDATE %s SET updatedTimestamp = ? WHERE id = ?", tableName);
      PreparedStatement statement = conn.prepareStatement(query);
      statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
      statement.setInt(2, newRow.getInt("id"));

      int updated = statement.executeUpdate();
      if (updated != 1) {
        throw new SQLException("Expected to update 1 row but updated " + updated);
      }
    }
  }
}
