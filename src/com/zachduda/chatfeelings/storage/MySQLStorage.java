package com.zachduda.chatfeelings.storage;

import com.zachduda.chatfeelings.Main;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores player data in MySQL/MariaDB so it can be shared between servers on a network.
 * <p>
 * Uses the MySQL driver bundled with Spigot/Paper and a single, lazily re-opened connection.
 * ChatFeelings only touches the database on join/quit, when a feeling is sent and for a few
 * admin commands, so a pool isn't worth shading in. All access is serialized on {@link #lock}.
 */
public class MySQLStorage implements PlayerStorage {
    private static final Pattern SAFE_PREFIX = Pattern.compile("^[A-Za-z0-9_]{0,32}$");
    private static final String[] DRIVERS = {"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"};

    private final Object lock = new Object();
    private final String url;
    private final Properties props = new Properties();

    private final String players;
    private final String ignores;
    private final String stats;
    private final String globalStats;

    private Connection connection;

    public MySQLStorage(String host, int port, String database, String username, String password,
                        boolean useSsl, String tablePrefix, String extraProperties) {
        if (tablePrefix == null || !SAFE_PREFIX.matcher(tablePrefix).matches()) {
            throw new IllegalArgumentException("Table-Prefix may only contain letters, numbers and underscores (max 32).");
        }
        this.players = tablePrefix + "players";
        this.ignores = tablePrefix + "ignores";
        this.stats = tablePrefix + "stats";
        this.globalStats = tablePrefix + "global_stats";

        StringBuilder sb = new StringBuilder("jdbc:mysql://").append(host).append(':').append(port).append('/').append(database)
                .append("?useSSL=").append(useSsl)
                .append("&allowPublicKeyRetrieval=").append(!useSsl)
                .append("&characterEncoding=utf8&useUnicode=true");
        if (extraProperties != null && !extraProperties.isBlank()) {
            sb.append('&').append(extraProperties.startsWith("&") || extraProperties.startsWith("?")
                    ? extraProperties.substring(1) : extraProperties);
        }
        this.url = sb.toString();

        props.setProperty("user", username == null ? "" : username);
        props.setProperty("password", password == null ? "" : password);
        props.setProperty("connectTimeout", "5000");
        props.setProperty("socketTimeout", "15000");
    }

    @Override
    public String getName() {
        return "MySQL";
    }

    /** Connects and creates the tables if needed. Throws if the database can't be reached. */
    public void init() throws SQLException {
        loadDriver();
        synchronized (lock) {
            try (Statement st = conn().createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + players + " ("
                        + "uuid CHAR(36) NOT NULL PRIMARY KEY,"
                        + "username VARCHAR(64) NOT NULL,"
                        + "ip VARCHAR(64) NULL,"
                        + "last_on BIGINT NOT NULL,"
                        + "allow_feelings TINYINT(1) NOT NULL DEFAULT 1,"
                        + "muted TINYINT(1) NOT NULL DEFAULT 0,"
                        + "total_sent INT NOT NULL DEFAULT 0,"
                        + "INDEX idx_" + players + "_username (username)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + ignores + " ("
                        + "uuid CHAR(36) NOT NULL,"
                        + "ignored_uuid CHAR(36) NOT NULL,"
                        + "PRIMARY KEY (uuid, ignored_uuid),"
                        + "FOREIGN KEY (uuid) REFERENCES " + players + "(uuid) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + stats + " ("
                        + "uuid CHAR(36) NOT NULL,"
                        + "feeling VARCHAR(64) NOT NULL,"
                        + "sent INT NOT NULL DEFAULT 0,"
                        + "PRIMARY KEY (uuid, feeling),"
                        + "FOREIGN KEY (uuid) REFERENCES " + players + "(uuid) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + globalStats + " ("
                        + "feeling VARCHAR(64) NOT NULL PRIMARY KEY,"
                        + "sent BIGINT NOT NULL DEFAULT 0"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            }
        }
    }

    private static void loadDriver() throws SQLException {
        for (String driver : DRIVERS) {
            try {
                Class.forName(driver);
                return;
            } catch (ClassNotFoundException ignored) {
                // try the next one
            }
        }
        throw new SQLException("No MySQL JDBC driver found on this server.");
    }

    /** Must be called while holding {@link #lock}. */
    private Connection conn() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            closeQuietly();
            connection = DriverManager.getConnection(url, props);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    private void closeQuietly() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // already gone
            }
            connection = null;
        }
    }

    private void error(String action, SQLException err) {
        Main.log("MySQL error while trying to " + action + ": " + err.getMessage(), true, true);
        if (Main.debug()) {
            err.printStackTrace();
        }
    }

    /** Runs {@code body} inside a transaction, rolling back on failure. Must hold {@link #lock}. */
    private <T> T transaction(SqlFunction<T> body) throws SQLException {
        Connection c = conn();
        c.setAutoCommit(false);
        try {
            T result = body.apply(c);
            c.commit();
            return result;
        } catch (SQLException err) {
            try {
                c.rollback();
            } catch (SQLException ignored) {
                // connection is likely dead, it'll be replaced on next use
            }
            throw err;
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // see above
            }
        }
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection c) throws SQLException;
    }

    // ------------------------------------------------------------------------------------------

    private PlayerData readPlayer(Connection c, ResultSet rs) throws SQLException {
        final String uuidStr = rs.getString("uuid");
        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException bad) {
            return null;
        }

        Set<String> ignoring = new LinkedHashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT ignored_uuid FROM " + ignores + " WHERE uuid = ?")) {
            ps.setString(1, uuidStr);
            try (ResultSet irs = ps.executeQuery()) {
                while (irs.next()) {
                    ignoring.add(irs.getString(1));
                }
            }
        }

        Map<String, Integer> sent = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT feeling, sent FROM " + stats + " WHERE uuid = ?")) {
            ps.setString(1, uuidStr);
            try (ResultSet srs = ps.executeQuery()) {
                while (srs.next()) {
                    sent.put(srs.getString(1), srs.getInt(2));
                }
            }
        }

        return new PlayerData(uuid, rs.getString("username"), rs.getString("ip"), rs.getLong("last_on"),
                rs.getBoolean("allow_feelings"), rs.getBoolean("muted"), ignoring, sent, rs.getInt("total_sent"));
    }

    @Override
    public PlayerData load(UUID uuid) {
        synchronized (lock) {
            try {
                Connection c = conn();
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + players + " WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? readPlayer(c, rs) : null;
                    }
                }
            } catch (SQLException err) {
                error("load player " + uuid, err);
                return null;
            }
        }
    }

    @Override
    public UUID findUUIDByName(String username) {
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT uuid FROM " + players + " WHERE username = ? ORDER BY last_on DESC LIMIT 1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? UUID.fromString(rs.getString(1)) : null;
                }
            } catch (SQLException | IllegalArgumentException err) {
                if (err instanceof SQLException sql) {
                    error("look up " + username, sql);
                }
                return null;
            }
        }
    }

    @Override
    public void recordLogin(UUID uuid, String username, String ip, long lastOn) {
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement("INSERT INTO " + players
                    + " (uuid, username, ip, last_on) VALUES (?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE username = VALUES(username), ip = VALUES(ip), last_on = VALUES(last_on)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.setString(3, ip);
                ps.setLong(4, lastOn);
                ps.executeUpdate();
            } catch (SQLException err) {
                error("save " + username + "'s data", err);
            }
        }
    }

    @Override
    public boolean setMuted(UUID uuid, boolean muted) {
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement("UPDATE " + players + " SET muted = ? WHERE uuid = ?")) {
                ps.setBoolean(1, muted);
                ps.setString(2, uuid.toString());
                // MySQL reports "matched" rows only with useAffectedRows=false (the default), so this is true
                // even when the value didn't change.
                return ps.executeUpdate() > 0;
            } catch (SQLException err) {
                error("change mute status of " + uuid, err);
                return false;
            }
        }
    }

    @Override
    public Boolean toggleAllowFeelings(UUID uuid) {
        synchronized (lock) {
            try {
                return transaction(c -> {
                    Boolean current = null;
                    try (PreparedStatement ps = c.prepareStatement("SELECT allow_feelings FROM " + players + " WHERE uuid = ? FOR UPDATE")) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                current = rs.getBoolean(1);
                            }
                        }
                    }
                    if (current == null) {
                        return null;
                    }
                    try (PreparedStatement ps = c.prepareStatement("UPDATE " + players + " SET allow_feelings = ? WHERE uuid = ?")) {
                        ps.setBoolean(1, !current);
                        ps.setString(2, uuid.toString());
                        ps.executeUpdate();
                    }
                    return !current;
                });
            } catch (SQLException err) {
                error("toggle ignoring all for " + uuid, err);
                return null;
            }
        }
    }

    @Override
    public Boolean toggleIgnoring(UUID owner, UUID target) {
        synchronized (lock) {
            try {
                return transaction(c -> {
                    try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM " + players + " WHERE uuid = ? FOR UPDATE")) {
                        ps.setString(1, owner.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                return null;
                            }
                        }
                    }
                    try (PreparedStatement del = c.prepareStatement("DELETE FROM " + ignores + " WHERE uuid = ? AND ignored_uuid = ?")) {
                        del.setString(1, owner.toString());
                        del.setString(2, target.toString());
                        if (del.executeUpdate() > 0) {
                            return false;
                        }
                    }
                    try (PreparedStatement ins = c.prepareStatement("INSERT INTO " + ignores + " (uuid, ignored_uuid) VALUES (?, ?)")) {
                        ins.setString(1, owner.toString());
                        ins.setString(2, target.toString());
                        ins.executeUpdate();
                    }
                    return true;
                });
            } catch (SQLException err) {
                error("update ignore list for " + owner, err);
                return null;
            }
        }
    }

    @Override
    public void incrementSent(UUID uuid, String capitalizedFeeling) {
        synchronized (lock) {
            try {
                transaction(c -> {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE " + players + " SET total_sent = total_sent + 1 WHERE uuid = ?")) {
                        ps.setString(1, uuid.toString());
                        if (ps.executeUpdate() == 0) {
                            return null; // no record for this player
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + stats + " (uuid, feeling, sent) VALUES (?, ?, 1)"
                            + " ON DUPLICATE KEY UPDATE sent = sent + 1")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, capitalizedFeeling);
                        ps.executeUpdate();
                    }
                    return null;
                });
            } catch (SQLException err) {
                error("update stats for " + uuid, err);
            }
        }
    }

    @Override
    public void incrementGlobalSent(String capitalizedFeeling) {
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement("INSERT INTO " + globalStats + " (feeling, sent) VALUES (?, 1)"
                    + " ON DUPLICATE KEY UPDATE sent = sent + 1")) {
                ps.setString(1, capitalizedFeeling);
                ps.executeUpdate();
            } catch (SQLException err) {
                error("update global stats", err);
            }
        }
    }

    @Override
    public Map<String, Integer> getGlobalSent() {
        Map<String, Integer> out = new HashMap<>();
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement("SELECT feeling, sent FROM " + globalStats);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString(1), (int) Math.min(Integer.MAX_VALUE, rs.getLong(2)));
                }
            } catch (SQLException err) {
                error("read global stats", err);
            }
        }
        return out;
    }

    @Override
    public List<PlayerData> loadAll() {
        synchronized (lock) {
            try {
                Connection c = conn();
                Map<String, Set<String>> allIgnores = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT uuid, ignored_uuid FROM " + ignores);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        allIgnores.computeIfAbsent(rs.getString(1), k -> new LinkedHashSet<>()).add(rs.getString(2));
                    }
                }

                Map<String, Map<String, Integer>> allStats = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT uuid, feeling, sent FROM " + stats);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        allStats.computeIfAbsent(rs.getString(1), k -> new HashMap<>()).put(rs.getString(2), rs.getInt(3));
                    }
                }

                List<PlayerData> out = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + players);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final String uuidStr = rs.getString("uuid");
                        try {
                            out.add(new PlayerData(UUID.fromString(uuidStr), rs.getString("username"), rs.getString("ip"),
                                    rs.getLong("last_on"), rs.getBoolean("allow_feelings"), rs.getBoolean("muted"),
                                    allIgnores.getOrDefault(uuidStr, Set.of()), allStats.getOrDefault(uuidStr, Map.of()),
                                    rs.getInt("total_sent")));
                        } catch (IllegalArgumentException bad) {
                            Main.debug("Skipping MySQL row with invalid UUID: " + uuidStr);
                        }
                    }
                }
                return out;
            } catch (SQLException err) {
                error("load all players", err);
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void delete(UUID uuid) {
        synchronized (lock) {
            try (PreparedStatement ps = conn().prepareStatement("DELETE FROM " + players + " WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate(); // ignores & stats cascade
            } catch (SQLException err) {
                error("delete " + uuid, err);
            }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            closeQuietly();
        }
    }

    // ------------------------------------------------------------------------------------------

    private boolean isEmpty() throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM " + players + " LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return !rs.next();
        }
    }

    /**
     * Copies the YAML Data folder into MySQL when the players table is still empty, so switching
     * backends doesn't wipe mutes, ignore lists or stats. The YAML files are left untouched.
     *
     * @return how many players were imported (0 if the table already had data).
     */
    public int importFromYaml(File pluginDataFolder) {
        YamlStorage yaml = new YamlStorage(pluginDataFolder);
        if (!yaml.getFolder().exists()) {
            return 0;
        }

        synchronized (lock) {
            try {
                if (!isEmpty()) {
                    return 0;
                }
                final List<PlayerData> all = yaml.loadAll();
                final Map<String, Integer> global = yaml.getGlobalSent();
                if (all.isEmpty() && global.isEmpty()) {
                    return 0;
                }

                return transaction(c -> {
                    try (PreparedStatement p = c.prepareStatement("INSERT IGNORE INTO " + players
                            + " (uuid, username, ip, last_on, allow_feelings, muted, total_sent) VALUES (?, ?, ?, ?, ?, ?, ?)");
                         PreparedStatement ig = c.prepareStatement("INSERT IGNORE INTO " + ignores + " (uuid, ignored_uuid) VALUES (?, ?)");
                         PreparedStatement st = c.prepareStatement("INSERT IGNORE INTO " + stats + " (uuid, feeling, sent) VALUES (?, ?, ?)");
                         PreparedStatement gl = c.prepareStatement("INSERT INTO " + globalStats + " (feeling, sent) VALUES (?, ?)"
                                 + " ON DUPLICATE KEY UPDATE sent = sent + VALUES(sent)")) {

                        for (PlayerData d : all) {
                            final String u = d.getUuid().toString();
                            p.setString(1, u);
                            p.setString(2, d.getUsername());
                            p.setString(3, d.getIp());
                            p.setLong(4, d.getLastOn());
                            p.setBoolean(5, d.isAllowingFeelings());
                            p.setBoolean(6, d.isMuted());
                            p.setInt(7, d.getTotalSent());
                            p.addBatch();

                            for (String ignored : d.getIgnoring()) {
                                ig.setString(1, u);
                                ig.setString(2, ignored);
                                ig.addBatch();
                            }
                            for (Map.Entry<String, Integer> e : d.getSentStats().entrySet()) {
                                st.setString(1, u);
                                st.setString(2, e.getKey());
                                st.setInt(3, e.getValue());
                                st.addBatch();
                            }
                        }
                        for (Map.Entry<String, Integer> e : global.entrySet()) {
                            gl.setString(1, e.getKey());
                            gl.setLong(2, e.getValue());
                            gl.addBatch();
                        }

                        // Parents first so the foreign keys are satisfied.
                        p.executeBatch();
                        ig.executeBatch();
                        st.executeBatch();
                        gl.executeBatch();
                    }
                    return all.size();
                });
            } catch (SQLException err) {
                error("import the YAML Data folder", err);
                return 0;
            }
        }
    }
}
