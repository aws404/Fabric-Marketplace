package io.github.aws404.market;

import io.github.aws404.api.events.MarketLifecycleEvents;
import io.github.aws404.market.orders.MarketListing;
import io.github.aws404.market.tasks.MarketTask;
import io.github.aws404.market.tasks.UpdateAllTask;
import io.github.aws404.util.ThreadExecutorConsumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class MarketInstance extends ThreadExecutorConsumer<MarketTask, MarketInstance> {

    public static MarketInstance MARKET_INSTANCE;
    public static final Logger LOGGER = LogManager.getLogger();

    private final Thread marketThread;
    private final Connection database;
    private final MinecraftServer server;
    public final Profiler profiler;

    public boolean online = false;
    private int tick = 0;

    private MarketInstance(Thread marketThread, MinecraftServer server) {
        super("Market");
        this.marketThread = marketThread;
        this.server = server;
        this.profiler = new ProfilerSystem(Util.nanoTimeSupplier, () -> this.tick, false);

        MARKET_INSTANCE = this;

        database = connect(new File(FabricLoader.getInstance().getConfigDir().toFile(), "market.db"));
    }

    public static void startMarket(MinecraftServer server) {
        LOGGER.info("Starting market!");
        if (MARKET_INSTANCE != null && MARKET_INSTANCE.online) {
            throw new RuntimeException("Market already started");
        }

        AtomicReference<MarketInstance> atomicReference = new AtomicReference<>();
        Thread thread = new Thread(() -> atomicReference.get().start(), "Market thread");
        MARKET_INSTANCE = new MarketInstance(thread, server);
        atomicReference.set(MARKET_INSTANCE);

        thread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            LOGGER.error("There was an error on the market thread: " + e.getMessage());
        });
        thread.start();
    }

    public static Connection connect(File databaseFile) {
        Connection conn = null;
        try {
            if (!databaseFile.exists()) {
                if (databaseFile.createNewFile()) {
                    LOGGER.info("No market database found, creating one ({})!", databaseFile.getCanonicalPath());
                }
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS buy_requests " +
                    "(ID               INTEGER     PRIMARY KEY AUTOINCREMENT    NOT NULL,                        " +
                    " SELLER           TEXT                                     NOT NULL,                        " +
                    " TYPE             TEXT                                     NOT NULL,                        " +
                    " CLAIMABLE        BLOB                                     DEFAULT '[]',                    " +
                    " SERIALISED       BLOB                                     NOT NULL,                        " +
                    " TIMESTAMP        INT                                      DEFAULT (strftime('%s','now')) );";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public void start() {
        MarketLifecycleEvents.MARKET_STARTED.invoker().onMarketEvent(MARKET_INSTANCE);

        LOGGER.info("Started market task execution!");
        this.online = true;

        ++this.executionsInProgress;

        try {
            while (online) {

                profiler.startTick();
                profiler.push("marketTickEvent");
                MarketLifecycleEvents.MARKET_TICK.invoker().onMarketEvent(this);
                profiler.swap("runMarketTasks");
                if (!this.runTask()) {
                    this.waitForTasks();
                }
                profiler.pop();

                tick++;
                if (tick % 1200 == 0) {
                    if (server.getPlayerManager().getPlayerList().size() > 1) {
                        executeTask(new UpdateAllTask());
                    }
                }
                profiler.endTick();
            }
        } finally {
            --this.executionsInProgress;
        }
    }

    public Connection getDatabase() {
        return database;
    }

    public void stop() {
        MarketLifecycleEvents.MARKET_STOPPED.invoker().onMarketEvent(this);

        LOGGER.info("Stopping market!");

        int tick = 20;
        while (getTaskCount() > 1) {
            if (tick % 20 == 0) {
                LOGGER.info("Awaiting " + getTaskCount() + " tasks to complete before stopping market");
            }
            tick++;
        }
        online = false;
        try {
            database.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            marketThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Market stopped!");

        if (profiler instanceof ReadableProfiler) {
            ((ReadableProfiler) profiler).getResult().save(FabricLoader.getInstance().getConfigDir().resolve("market_profiler.txt").toFile());
        }
    }

    public ArrayList<MarketListing> getBuyOrders(Predicate<MarketListing> predicate) {
        ArrayList<MarketListing> orders = new ArrayList<>();
        try {
            Statement stmt = database.createStatement();
            String sql = "SELECT * FROM buy_requests";
            ResultSet set = stmt.executeQuery(sql);

            while (set.next()) {
                MarketListing order = MarketRegistry.ORDER_TYPES.get(new Identifier(set.getString("TYPE"))).fromSet(this, set);
                if (predicate == null || predicate.test(order)) {
                    orders.add(order);
                }
            }

            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return orders;
    }

    public ArrayList<MarketListing> getBuyOrders(UUID uuid, Predicate<MarketListing> predicate) {
        ArrayList<MarketListing> orders = new ArrayList<>();
        try {
            Statement stmt = database.createStatement();
            String sql = String.format("SELECT * FROM buy_requests WHERE SELLER='%s'", uuid.toString());
            ResultSet set = stmt.executeQuery(sql);

            while (set.next()) {
                MarketListing order = MarketRegistry.ORDER_TYPES.get(new Identifier(set.getString("TYPE"))).fromSet( this, set);
                if (predicate == null || predicate.test(order)) {
                    orders.add(order);
                }
            }

            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return orders;
    }

    public MarketListing getBuyOrder(int id) {
        try {
            Statement stmt = database.createStatement();
            String sql = String.format("SELECT * FROM buy_requests WHERE ID='%s'", id);
            ResultSet set = stmt.executeQuery(sql);

            set.next();
            MarketListing order = MarketRegistry.ORDER_TYPES.get(new Identifier(set.getString("TYPE"))).fromSet( this, set);
            stmt.close();

            return order;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public void writeUpdatesToOrder(MarketListing order) {
        if (!isOnThread()) {
            throw new UnsupportedOperationException("The writeUpdatesToOrder method must only be completed from the Market thread. Use a MarketTask to do this.");
        }

        try {
            Statement stmt = database.createStatement();
            String sql = String.format("UPDATE buy_requests SET " +
                    "SELLER='%s',     " +
                    "TYPE='%s',       " +
                    "CLAIMABLE='%s',  " +
                    "SERIALISED='%s' " +
                    "WHERE ID=%s;     ",
                    order.getSeller().getId(),
                    order.getIdentifier(),
                    order.serialiseClaimable(),
                    order.serialiseOrder(),
                    order.id
            );
            stmt.execute(sql);
            stmt.close();
            order.refresh();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    protected MarketTask createTask(Runnable runnable) {
        return new MarketTask(runnable);
    }

    @Override
    protected boolean canExecute(MarketTask task) {
        return online;
    }

    @Override
    protected Thread getThread() {
        return marketThread;
    }

}
