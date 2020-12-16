package io.github.aws404;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "fabric-marketplace")
public class MarketplaceConfig implements ConfigData {
    @Comment("How long, in minutes, an order will be on the market before expiring.")
    public int orderExpireTime = 10080;

    @Comment("The currency blacklist, any currencies listed here will be disabled.")
    public String[] currencyBlacklist = {"experience_levels"};

    @Comment("The order type blacklist, any order types listed here will be disabled.")
    public String[] orderTypeBlacklist = {};
}