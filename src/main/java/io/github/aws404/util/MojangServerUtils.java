package io.github.aws404.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import io.github.aws404.mixin.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class MojangServerUtils {

    private static final Gson GSON = new Gson();
    private final YggdrasilAuthenticationService authenticationService;
    private final HashMap<UUID, String> uuidNameCache = new HashMap<>();

    public MojangServerUtils(MinecraftServer server) {
        YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) ((MinecraftServerAccessor) server).getSessionService();
        authenticationService = sessionService.getAuthenticationService();
    }

    public String getNameFromUUID(UUID uuid) {
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        String name = getPlayerData(uuid).get("name").getAsString();
        uuidNameCache.put(uuid, name);
        return name;
    }

    public JsonObject getPlayerData(UUID uuid) {
        String SESSION_SERVER_BASE = "https://sessionserver.mojang.com/session/minecraft/";
        URL url = HttpAuthenticationService.constantURL(SESSION_SERVER_BASE + "profile/" + UUIDTypeAdapter.fromUUID(uuid));
        url = HttpAuthenticationService.concatenateURL(url, "unsigned=false");

        try {
            JsonObject jsonResult = makeRequestJson(url);
            return GSON.fromJson(jsonResult, JsonObject.class);
        } catch (final IllegalStateException | JsonParseException ignored) { }

        return null;
    }

    /**
     * Makes a request to the given url and parses the response to a json object
     * @param url the url to get
     * @return the parsed json
     */
    private JsonObject makeRequestJson(URL url) {
        try {
            String result = authenticationService.performGetRequest(url);

            return GSON.fromJson(result, JsonObject.class);

        } catch (IOException | IllegalStateException | JsonParseException | NullPointerException ignored) { }
        return null;
    }
}
