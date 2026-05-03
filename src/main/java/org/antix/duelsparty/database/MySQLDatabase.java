package org.antix.duelsparty.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.antix.duelsparty.DuelsPartyPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLDatabase implements DatabaseService {
    private final HikariDataSource dataSource;

    public MySQLDatabase(String host, int port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(password);

        // Optymalizacja pod Minecrafta
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10); // Wystarczające dla trybu Lobby

        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS produels_stats (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "wins INT DEFAULT 0, " +
                             "losses INT DEFAULT 0, " +
                             "kills INT DEFAULT 0, " +
                             "deaths INT DEFAULT 0" +
                             ")")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveMatchResult(UUID winner, UUID loser, int winnerKills, int loserKills) {
        // Wykonujemy to asynchronicznie, aby nie blokować Main Thread
        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                updatePlayer(conn, winner, true, winnerKills, 0);
                updatePlayer(conn, loser, false, loserKills, 1);
                DuelsPartyPlugin.debug("Zapisano wynik walki w MySQL dla: " + winner + " i " + loser);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePlayer(Connection conn, UUID uuid, boolean won, int kills, int deaths) throws SQLException {
        String sql = "INSERT INTO produels_stats (uuid, wins, losses, kills, deaths) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE wins = wins + ?, losses = losses + ?, kills = kills + ?, deaths = deaths + ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, won ? 1 : 0);
            stmt.setInt(3, won ? 0 : 1);
            stmt.setInt(4, kills);
            stmt.setInt(5, deaths);
            // Update part
            stmt.setInt(6, won ? 1 : 0);
            stmt.setInt(7, won ? 0 : 1);
            stmt.setInt(8, kills);
            stmt.setInt(9, deaths);
            stmt.executeUpdate();
        }
    }

    @Override
    public CompletableFuture<UserStats> loadStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Logika SELECT...
            return new UserStats(uuid, 0, 0, 0, 0, 0.0);
        });
    }

    @Override
    public void close() {
        if (dataSource != null) dataSource.close();
    }
}