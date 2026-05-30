package de.louis.xdGens.command;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class XdAdminCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    private static final List<String> TAGS        = rewardNames(CrateReward::isTag);
    private static final List<String> COLORS      = rewardNames(CrateReward::isColor);
    private static final List<String> CHAT_COLORS = rewardNames(CrateReward::isChatColor);
    private static final List<String> GLOWS       = rewardNames(r -> r.getType() == CrateReward.Type.GLOW);
    private static final List<String> CRATE_TYPES = Arrays.stream(CrateType.values()).map(Enum::name).toList();
    private static final List<String> MODIFY_ACTIONS = List.of("set", "add", "remove");
    private static final List<String> MODIFY_SUBS    = List.of("money", "tokens", "xp", "level", "prestige");

    /** Tab-complete suggestions for amount arguments — mirrors NumberUtil suffixes */
    private static final List<String> AMOUNT_SUGGESTIONS = List.of(
            "1k", "10k", "100k",
            "1M", "10M", "100M",
            "1B", "10B", "100B",
            "1T", "10T", "100T",
            "1Q", "10Q", "100Q",
            "1Qi", "10Qi",
            "1Sx", "1Sp", "1Oc", "1No", "1Dc"
    );

    public XdAdminCommand(Main plugin) { this.plugin = plugin; }

    // ── command dispatch ──────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdgens.admin")) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>No permission.</red>");
            return true;
        }
        if (args.length == 0) { sendUsage(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "info"          -> handleInfo(sender, args);
            case "reset"         -> handleReset(sender, args);
            case "givekey"       -> handleGiveKey(sender, args);
            case "givetag"       -> handleGiveCosmetic(sender, args, CrateReward.Type.TAG,        TAGS,        "tag");
            case "givecolor"     -> handleGiveCosmetic(sender, args, CrateReward.Type.NAME_COLOR, COLORS,      "name color");
            case "givechatcolor" -> handleGiveCosmetic(sender, args, CrateReward.Type.CHAT_COLOR, CHAT_COLORS, "chat color");
            case "giveglow"      -> handleGiveCosmetic(sender, args, CrateReward.Type.GLOW,       GLOWS,       "glow");
            default -> {
                if (MODIFY_SUBS.contains(args[0].toLowerCase())) handleModify(sender, args[0].toLowerCase(), args);
                else sendUsage(sender);
            }
        }
        return true;
    }

    // ── /xdadmin givekey <player> <crate> [amount] ─────────────────────────────

    private void handleGiveKey(CommandSender sender, String[] args) {
        if (args.length < 3) { syntax(sender, "givekey <player> <crate> [amount]"); return; }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        CrateType crate;
        try { crate = CrateType.valueOf(args[2].toUpperCase()); }
        catch (IllegalArgumentException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Unknown crate. Valid: " + CRATE_TYPES + "</red>");
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try { amount = (int) Math.max(1, Math.min(parseAmount(args[3]), 1000)); }
            catch (NumberFormatException e) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Invalid amount.</red>"); return; }
        }

        for (int i = 0; i < amount; i++) plugin.getVirtualKeyManager().addKey(target, crate);
        int total = plugin.getVirtualKeyManager().getKeys(target, crate);

        ok(sender, "Gave <white>" + amount + "x " + crate.getDisplayName() + " Key</white> to <yellow>"
                + target.getName() + "</yellow>. Total: " + total);
        MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>You received <white>" + amount + "x "
                + crate.getGradient() + crate.getDisplayName() + " Key</gradient></white>!");
    }

    // ── /xdadmin give<tag|color|chatcolor|glow> <player> <reward|*> ────────────

    private void handleGiveCosmetic(CommandSender sender, String[] args,
                                     CrateReward.Type type, List<String> validNames, String label) {
        if (args.length < 3) {
            syntax(sender, args[0].toLowerCase() + " <player> <" + label + "|*>");
            MessageUtil.sendRaw(sender, "<dark_gray>Available: " + String.join(", ", validNames));
            return;
        }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        if (args[2].equals("*")) {
            int count = 0;
            for (CrateReward r : CrateReward.values())
                if (r.getType() == type && plugin.getPlayerCosmeticManager().unlock(target, r)) count++;
            ok(sender, "Unlocked <white>" + count + " new " + label + "(s)</white> for <yellow>" + target.getName() + "</yellow>.");
            MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>\u2728 Admin unlocked ALL " + label + "s! Use <white>/cosmetics</white>.");
            return;
        }

        CrateReward reward;
        try {
            reward = CrateReward.valueOf(args[2].toUpperCase());
            if (reward.getType() != type) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Unknown " + label + ": " + args[2] + "</red>");
            MessageUtil.sendRaw(sender, "<dark_gray>Valid: " + String.join(", ", validNames));
            return;
        }

        boolean isNew = plugin.getPlayerCosmeticManager().unlock(target, reward);
        String preview = buildPreview(reward, target);

        if (isNew) {
            ok(sender, "Unlocked " + label + " <white>" + preview + "</white> for <yellow>" + target.getName() + "</yellow>.");
            MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>\u2728 Admin unlocked " + label + ": "
                    + preview + " <gray>| <white>/cosmetics</white> to equip.");
        } else {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <yellow>" + target.getName()
                    + " already has " + reward.getDisplayName() + ".</yellow>");
        }
    }

    // ── /xdadmin info <player> ───────────────────────────────────────────────

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { syntax(sender, "info <player>"); return; }
        Player t = resolvePlayer(sender, args[1]);
        if (t == null) return;

        var cosm = plugin.getPlayerCosmeticManager();
        StringBuilder keys = new StringBuilder();
        for (CrateType ct : CrateType.values())
            keys.append(ct.getDisplayName()).append(":").append(plugin.getVirtualKeyManager().getKeys(t, ct)).append("  ");

        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <gray>\u2500\u2500 Info: <yellow>" + t.getName() + "</yellow> \u2500\u2500</gray>");
        MessageUtil.sendRaw(sender, "<gray>Money:     </gray><green>$" + NumberUtil.format(plugin.getCurrencyManager().getMoney(t)) + "</green>");
        MessageUtil.sendRaw(sender, "<gray>Tokens:    </gray><gold>" + NumberUtil.format(plugin.getCurrencyManager().getTokens(t)) + "</gold>");
        MessageUtil.sendRaw(sender, "<gray>Level:     </gray><aqua>" + plugin.getProgressionManager().getLevel(t) + "</aqua>");
        MessageUtil.sendRaw(sender, "<gray>XP:        </gray><aqua>"
                + NumberUtil.format(plugin.getProgressionManager().getXp(t))
                + " / " + NumberUtil.format(plugin.getProgressionManager().getRequiredXp(t)) + "</aqua>");
        MessageUtil.sendRaw(sender, "<gray>Prestige:  </gray><gradient:#f6d365:#fda085>" + plugin.getProgressionManager().getPrestige(t) + "</gradient>");
        MessageUtil.sendRaw(sender, "<gray>Keys:      </gray><white>" + keys.toString().trim() + "</white>");
        MessageUtil.sendRaw(sender, "<gray>Tags:      </gray><white>" + cosm.getUnlockedTags(t).size() + " unlocked</white>");
        MessageUtil.sendRaw(sender, "<gray>Colors:    </gray><white>" + cosm.getUnlockedColors(t).size()
                + " / " + cosm.getUnlockedChatColors(t).size() + " chat</white>");
        MessageUtil.sendRaw(sender, "<gray>Glows:     </gray><white>" + cosm.getUnlockedGlows(t).size() + " unlocked</white>");
    }

    // ── /xdadmin reset <player> ───────────────────────────────────────────

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) { syntax(sender, "reset <player>"); return; }
        Player t = resolvePlayer(sender, args[1]);
        if (t == null) return;

        plugin.getCurrencyManager().setMoney(t, 0.0);
        plugin.getCurrencyManager().setTokens(t, 0L);
        setProgressValue(t, "level", 1);
        setProgressValue(t, "prestige", 0);
        setProgressValue(t, "xp", 0.0);
        plugin.getCurrencyManager().savePlayer(t);
        plugin.getProgressionManager().savePlayer(t);
        plugin.getProgressionManager().updateDisplays(t);

        ok(sender, "Reset <yellow>" + t.getName() + "</yellow>.");
        MessageUtil.sendRaw(t, MessageUtil.PREFIX + " <red>Your data was reset by an admin.</red>");
    }

    // ── /xdadmin <money|tokens|xp|level|prestige> <set|add|remove> <player> <amount> ──

    private void handleModify(CommandSender sender, String type, String[] args) {
        if (args.length < 4) { syntax(sender, type + " <set|add|remove> <player> <amount>"); return; }
        String action = args[1].toLowerCase();
        Player t = resolvePlayer(sender, args[2]);
        if (t == null) return;

        double amount;
        try { amount = parseAmount(args[3]); }
        catch (NumberFormatException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX
                    + " <red>Invalid amount. Example: <white>100k</white>, <white>5M</white>, <white>2B</white></red>");
            return;
        }
        if (amount < 0) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Amount must be positive.</red>"); return; }

        switch (type) {
            case "money"    -> modifyMoney(sender, t, action, amount);
            case "tokens"   -> modifyTokens(sender, t, action, Math.round(amount));
            case "xp"       -> modifyXp(sender, t, action, amount);
            case "level"    -> modifyLevel(sender, t, action, (int) Math.round(amount));
            case "prestige" -> modifyPrestige(sender, t, action, (int) Math.round(amount));
        }
    }

    private void modifyMoney(CommandSender sender, Player t, String action, double amount) {
        switch (action) {
            case "set"    -> plugin.getCurrencyManager().setMoney(t, amount);
            case "add"    -> plugin.getCurrencyManager().addMoney(t, amount);
            case "remove" -> { if (!plugin.getCurrencyManager().removeMoney(t, amount)) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Not enough money.</red>"); return; } }
            default -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); return; }
        }
        plugin.getCurrencyManager().savePlayer(t);
        ok(sender, "Money \u2192 <green>$" + NumberUtil.format(plugin.getCurrencyManager().getMoney(t)) + "</green>");
    }

    private void modifyTokens(CommandSender sender, Player t, String action, long amount) {
        switch (action) {
            case "set"    -> plugin.getCurrencyManager().setTokens(t, amount);
            case "add"    -> plugin.getCurrencyManager().addTokens(t, amount);
            case "remove" -> { if (!plugin.getCurrencyManager().removeTokens(t, amount)) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Not enough tokens.</red>"); return; } }
            default -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); return; }
        }
        plugin.getCurrencyManager().savePlayer(t);
        ok(sender, "Tokens \u2192 <gold>" + NumberUtil.format(plugin.getCurrencyManager().getTokens(t)) + "</gold>");
    }

    private void modifyXp(CommandSender sender, Player t, String action, double amount) {
        double cur = plugin.getProgressionManager().getXp(t);
        double upd = switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> Math.max(0, cur - amount);
            default -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        };
        setProgressValue(t, "xp", upd);
        plugin.getProgressionManager().savePlayer(t);
        plugin.getProgressionManager().updateDisplays(t);
        ok(sender, "XP \u2192 <aqua>" + NumberUtil.format(upd) + "</aqua>");
    }

    private void modifyLevel(CommandSender sender, Player t, String action, int amount) {
        int cur = plugin.getProgressionManager().getLevel(t);
        int max = plugin.getProgressionManager().getMaxLevel();
        int upd = Math.min(max, Math.max(1, switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> cur - amount;
            default -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        }));
        setProgressValue(t, "level", upd);
        plugin.getProgressionManager().savePlayer(t);
        plugin.getProgressionManager().updateDisplays(t);
        ok(sender, "Level \u2192 <aqua>" + upd + "</aqua>");
    }

    private void modifyPrestige(CommandSender sender, Player t, String action, int amount) {
        int cur = plugin.getProgressionManager().getPrestige(t);
        int upd = Math.max(0, switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> cur - amount;
            default -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        });
        setProgressValue(t, "prestige", upd);
        plugin.getProgressionManager().savePlayer(t);
        plugin.getProgressionManager().updateDisplays(t);
        ok(sender, "Prestige \u2192 <gradient:#f6d365:#fda085>" + upd + "</gradient>");
    }

    // ── tab complete ──────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("xdgens.admin")) return List.of();

        if (args.length == 1)
            return filter(List.of("info", "reset", "givekey", "givetag", "givecolor",
                    "givechatcolor", "giveglow",
                    "money", "tokens", "xp", "level", "prestige"), args[0]);

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            if (List.of("info", "reset", "givekey", "givetag", "givecolor", "givechatcolor", "giveglow").contains(sub))
                return filterPlayers(args[1]);
            if (MODIFY_SUBS.contains(sub)) return filter(MODIFY_ACTIONS, args[1]);
            return List.of();
        }

        if (args.length == 3) {
            return switch (sub) {
                case "givekey"       -> filter(CRATE_TYPES, args[2]);
                case "givetag"       -> withWildcard(TAGS, args[2]);
                case "givecolor"     -> withWildcard(COLORS, args[2]);
                case "givechatcolor" -> withWildcard(CHAT_COLORS, args[2]);
                case "giveglow"      -> withWildcard(GLOWS, args[2]);
                default              -> filterPlayers(args[2]);
            };
        }

        if (args.length == 4) {
            if (sub.equals("givekey")) return filter(List.of("1", "5", "10", "50"), args[3]);
            return filter(AMOUNT_SUGGESTIONS, args[3]);
        }

        return List.of();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private static List<String> rewardNames(java.util.function.Predicate<CrateReward> filter) {
        return Arrays.stream(CrateReward.values()).filter(filter).map(Enum::name).toList();
    }

    private List<String> withWildcard(List<String> base, String current) {
        java.util.List<String> merged = new java.util.ArrayList<>();
        merged.add("*");
        merged.addAll(base);
        return filter(merged, current);
    }

    private String buildPreview(CrateReward reward, Player target) {
        if (reward.isTag())       return reward.getCosmeticFormat();
        if (reward.isColor())     return reward.getCosmeticFormat().replace("{name}", target.getName());
        if (reward.isChatColor()) return reward.getCosmeticFormat().replace("{msg}", "Hello!");
        return "<yellow>\u2728 " + reward.getDisplayName() + "</yellow>";
    }

    private void ok(CommandSender sender, String msg) {
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>" + msg + "</green>");
    }

    private void syntax(CommandSender sender, String usage) {
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin " + usage + "</red>");
    }

    private Player resolvePlayer(CommandSender sender, String name) {
        Player t = Bukkit.getPlayerExact(name);
        if (t == null) MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Player '" + name + "' not found.</red>");
        return t;
    }

    private List<String> filterPlayers(String current) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(current.toLowerCase()))
                .toList();
    }

    private List<String> filter(List<String> input, String current) {
        String lower = current.toLowerCase();
        return input.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }

    /**
     * Parses an amount string with optional suffix.
     * Supported suffixes (case-insensitive): k, m, b, t, q, qi, sx, sp, oc, no, dc,
     *                                        udc, ddc, tdc, qdc, qic, sxc, spc, ocd, nod, vg
     * Examples: "100k" -> 100_000  |  "5M" -> 5_000_000  |  "2Qi" -> 2_000_000_000_000_000_000
     */
    private double parseAmount(String input) throws NumberFormatException {
        if (input == null || input.isBlank()) throw new NumberFormatException("empty");
        String n = input.trim().replace(",", ".");

        // Try to strip any known multi-char suffix first (longest match wins)
        record Entry(String suffix, double mult) {}
        Entry[] entries = {
            new Entry("vg",  1e63), new Entry("nod", 1e60), new Entry("ocd", 1e57),
            new Entry("spc", 1e54), new Entry("sxc", 1e51), new Entry("qic", 1e48),
            new Entry("qdc", 1e45), new Entry("tdc", 1e42), new Entry("ddc", 1e39),
            new Entry("udc", 1e36), new Entry("dc",  1e33), new Entry("no",  1e30),
            new Entry("oc",  1e27), new Entry("sp",  1e24), new Entry("sx",  1e21),
            new Entry("qi",  1e18), new Entry("q",   1e15), new Entry("t",   1e12),
            new Entry("b",   1e9),  new Entry("m",   1e6),  new Entry("k",   1e3),
        };

        String lower = n.toLowerCase();
        for (Entry e : entries) {
            if (lower.endsWith(e.suffix)) {
                String num = n.substring(0, n.length() - e.suffix.length());
                return Double.parseDouble(num) * e.mult;
            }
        }
        // no suffix
        return Double.parseDouble(n);
    }

    private void setProgressValue(Player player, String fieldName, Object value) {
        try {
            Field dataField = plugin.getProgressionManager().getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Object> dataMap = (Map<UUID, Object>) dataField.get(plugin.getProgressionManager());
            Object progress = dataMap.get(player.getUniqueId());
            if (progress == null) {
                for (Class<?> inner : plugin.getProgressionManager().getClass().getDeclaredClasses()) {
                    if (inner.getSimpleName().equals("PlayerProgress")) {
                        var ctor = inner.getDeclaredConstructor();
                        ctor.setAccessible(true);
                        progress = ctor.newInstance();
                        dataMap.put(player.getUniqueId(), progress);
                        break;
                    }
                }
            }
            if (progress == null) throw new IllegalStateException("Could not create PlayerProgress");
            Field f = progress.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(progress, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUsage(CommandSender sender) {
        MessageUtil.sendRaw(sender, "<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <gray>Admin Commands</gray>");
        MessageUtil.sendRaw(sender, "<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin info <player>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin reset <player>");
        MessageUtil.sendRaw(sender, "<dark_gray>\u2014 Keys");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givekey <player> <crate> [amount]");
        MessageUtil.sendRaw(sender, "<dark_gray>\u2014 Cosmetics <gray>(use * for all)");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givetag <player> <tag|*>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givecolor <player> <color|*>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givechatcolor <player> <chatcolor|*>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin giveglow <player> <glow|*>");
        MessageUtil.sendRaw(sender, "<dark_gray>\u2014 Economy <gray>(supports k, M, B, T, Q, Qi, Sx, Sp, Oc, No, Dc ...)");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin money/tokens/xp/level/prestige <set|add|remove> <player> <amount>");
        MessageUtil.sendRaw(sender, "<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
    }
}
