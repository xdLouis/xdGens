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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class XdAdminCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    private static final List<String> TAGS = Arrays.stream(CrateReward.values())
            .filter(CrateReward::isTag).map(Enum::name).toList();
    private static final List<String> COLORS = Arrays.stream(CrateReward.values())
            .filter(CrateReward::isColor).map(Enum::name).toList();
    private static final List<String> CRATE_TYPES = Arrays.stream(CrateType.values())
            .map(Enum::name).toList();

    public XdAdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xdgens.admin")) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>You do not have permission.</red>");
            return true;
        }

        if (args.length == 0) { sendUsage(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "info"     -> handleInfo(sender, args);
            case "reset"    -> handleReset(sender, args);
            case "givekey"  -> handleGiveKey(sender, args);
            case "givetag"  -> handleGiveTag(sender, args);
            case "givecolor"-> handleGiveColor(sender, args);
            case "money", "tokens", "xp", "level", "prestige" -> handleModify(sender, args[0].toLowerCase(), args);
            default         -> sendUsage(sender);
        }
        return true;
    }

    // ── givekey ──────────────────────────────────────────────────────────
    // /xdadmin givekey <player> <crate> [amount]

    private void handleGiveKey(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin givekey <player> <crate> [amount]</red>");
            return;
        }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        CrateType crate;
        try { crate = CrateType.valueOf(args[2].toUpperCase()); }
        catch (IllegalArgumentException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Unknown crate: " + args[2] + ". Valid: " + CRATE_TYPES + "</red>");
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try { amount = Integer.parseInt(args[3]); }
            catch (NumberFormatException e) {
                MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Invalid amount.</red>");
                return;
            }
        }
        amount = Math.max(1, Math.min(amount, 1000));

        for (int i = 0; i < amount; i++) plugin.getVirtualKeyManager().addKey(target, crate);
        int total = plugin.getVirtualKeyManager().getKeys(target, crate);

        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Gave <white>" + amount + "x "
                + crate.getDisplayName() + " Key</white> to <yellow>" + target.getName()
                + "</yellow>. Total: <white>" + total + "</white></green>");
        MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>You received <white>" + amount + "x "
                + crate.getGradient() + crate.getDisplayName() + " Key</gradient></white>! Open it in <white>/crates</white>.");
    }

    // ── givetag ──────────────────────────────────────────────────────────
    // /xdadmin givetag <player> <tag>

    private void handleGiveTag(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin givetag <player> <tag></red>");
            MessageUtil.sendRaw(sender, "<dark_gray>Tags: " + String.join(", ", TAGS));
            return;
        }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        CrateReward reward;
        try {
            reward = CrateReward.valueOf(args[2].toUpperCase());
            if (!reward.isTag()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Unknown tag: " + args[2] + "</red>");
            MessageUtil.sendRaw(sender, "<dark_gray>Valid tags: " + String.join(", ", TAGS));
            return;
        }

        boolean isNew = plugin.getPlayerCosmeticManager().unlock(target, reward);
        String preview = reward.getCosmeticFormat();

        if (isNew) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Unlocked tag <white>"
                    + preview + "</white> for <yellow>" + target.getName() + "</yellow>.</green>");
            MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>\u2728 Admin unlocked tag: " + preview
                    + " <gray>| Use <white>/cosmetics</white> to equip.");
        } else {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <yellow>" + target.getName()
                    + " already has " + reward.getDisplayName() + ".</yellow>");
        }
    }

    // ── givecolor ─────────────────────────────────────────────────────────
    // /xdadmin givecolor <player> <color>

    private void handleGiveColor(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin givecolor <player> <color></red>");
            MessageUtil.sendRaw(sender, "<dark_gray>Colors: " + String.join(", ", COLORS));
            return;
        }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        CrateReward reward;
        try {
            reward = CrateReward.valueOf(args[2].toUpperCase());
            if (!reward.isColor()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Unknown color: " + args[2] + "</red>");
            MessageUtil.sendRaw(sender, "<dark_gray>Valid colors: " + String.join(", ", COLORS));
            return;
        }

        boolean isNew = plugin.getPlayerCosmeticManager().unlock(target, reward);
        String preview = reward.getCosmeticFormat().replace("{name}", target.getName());

        if (isNew) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Unlocked color <white>"
                    + preview + "</white> for <yellow>" + target.getName() + "</yellow>.</green>");
            MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <green>\u2728 Admin unlocked name color: " + preview
                    + " <gray>| Use <white>/cosmetics</white> to equip.");
        } else {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <yellow>" + target.getName()
                    + " already has " + reward.getDisplayName() + ".</yellow>");
        }
    }

    // ── existing handlers ────────────────────────────────────────────────────

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin info <player></red>"); return; }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        double money   = plugin.getCurrencyManager().getMoney(target);
        long tokens    = plugin.getCurrencyManager().getTokens(target);
        int level      = plugin.getProgressionManager().getLevel(target);
        int prestige   = plugin.getProgressionManager().getPrestige(target);
        double xp      = plugin.getProgressionManager().getXp(target);
        int reqXp      = plugin.getProgressionManager().getRequiredXp(target);
        int keys       = CrateType.values().length;
        StringBuilder keyInfo = new StringBuilder();
        for (CrateType ct : CrateType.values()) {
            keyInfo.append(ct.getDisplayName()).append(":").append(plugin.getVirtualKeyManager().getKeys(target, ct)).append(" ");
        }

        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <gray>Info: <yellow>" + target.getName() + "</yellow></gray>");
        MessageUtil.sendRaw(sender, "<gray>Money:</gray> <green>$" + NumberUtil.format(money) + "</green>");
        MessageUtil.sendRaw(sender, "<gray>Tokens:</gray> <gold>" + NumberUtil.format(tokens) + "</gold>");
        MessageUtil.sendRaw(sender, "<gray>Level:</gray> <aqua>" + level + "</aqua>");
        MessageUtil.sendRaw(sender, "<gray>XP:</gray> <aqua>" + NumberUtil.format(xp) + " / " + NumberUtil.format(reqXp) + "</aqua>");
        MessageUtil.sendRaw(sender, "<gray>Prestige:</gray> <gradient:#f6d365:#fda085>" + prestige + "</gradient>");
        MessageUtil.sendRaw(sender, "<gray>Keys:</gray> <white>" + keyInfo.toString().trim() + "</white>");
        MessageUtil.sendRaw(sender, "<gray>Tags:</gray> <white>" + plugin.getPlayerCosmeticManager().getUnlockedTags(target).size() + " unlocked</white>");
        MessageUtil.sendRaw(sender, "<gray>Colors:</gray> <white>" + plugin.getPlayerCosmeticManager().getUnlockedColors(target).size() + " unlocked</white>");
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin reset <player></red>"); return; }
        Player target = resolvePlayer(sender, args[1]);
        if (target == null) return;

        plugin.getCurrencyManager().setMoney(target, 0.0);
        plugin.getCurrencyManager().setTokens(target, 0L);
        setProgressValue(target, "level", 1);
        setProgressValue(target, "prestige", 0);
        setProgressValue(target, "xp", 0.0);
        plugin.getCurrencyManager().savePlayer(target);
        plugin.getProgressionManager().savePlayer(target);
        plugin.getProgressionManager().updateDisplays(target);

        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Reset <yellow>" + target.getName() + "</yellow>.</green>");
        MessageUtil.sendRaw(target, MessageUtil.PREFIX + " <red>Your data has been reset by an admin.</red>");
    }

    private void handleModify(CommandSender sender, String type, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Usage: /xdadmin " + type + " <set|add|remove> <player> <amount></red>");
            return;
        }
        String action = args[1].toLowerCase();
        Player target = resolvePlayer(sender, args[2]);
        if (target == null) return;

        double amount;
        try { amount = parseAmount(args[3]); }
        catch (NumberFormatException e) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Invalid amount.</red>");
            return;
        }
        if (amount < 0) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Amount must be positive.</red>"); return; }

        switch (type) {
            case "money"   -> handleMoney(sender, target, action, amount);
            case "tokens"  -> handleTokens(sender, target, action, Math.round(amount));
            case "xp"      -> handleXp(sender, target, action, amount);
            case "level"   -> handleLevel(sender, target, action, (int) Math.round(amount));
            case "prestige"-> handlePrestige(sender, target, action, (int) Math.round(amount));
        }
    }

    private void handleMoney(CommandSender sender, Player target, String action, double amount) {
        switch (action) {
            case "set"    -> plugin.getCurrencyManager().setMoney(target, amount);
            case "add"    -> plugin.getCurrencyManager().addMoney(target, amount);
            case "remove" -> { if (!plugin.getCurrencyManager().removeMoney(target, amount)) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Not enough money.</red>"); return; } }
            default       -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); return; }
        }
        plugin.getCurrencyManager().savePlayer(target);
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Money → <white>$" + NumberUtil.format(plugin.getCurrencyManager().getMoney(target)) + "</white></green>");
    }

    private void handleTokens(CommandSender sender, Player target, String action, long amount) {
        switch (action) {
            case "set"    -> plugin.getCurrencyManager().setTokens(target, amount);
            case "add"    -> plugin.getCurrencyManager().addTokens(target, amount);
            case "remove" -> { if (!plugin.getCurrencyManager().removeTokens(target, amount)) { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Not enough tokens.</red>"); return; } }
            default       -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); return; }
        }
        plugin.getCurrencyManager().savePlayer(target);
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Tokens → <gold>" + NumberUtil.format(plugin.getCurrencyManager().getTokens(target)) + "</gold></green>");
    }

    private void handleXp(CommandSender sender, Player target, String action, double amount) {
        double cur = plugin.getProgressionManager().getXp(target);
        double upd = switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> Math.max(0, cur - amount);
            default       -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        };
        setProgressValue(target, "xp", upd);
        plugin.getProgressionManager().savePlayer(target);
        plugin.getProgressionManager().updateDisplays(target);
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>XP → <aqua>" + NumberUtil.format(upd) + "</aqua></green>");
    }

    private void handleLevel(CommandSender sender, Player target, String action, int amount) {
        int cur = plugin.getProgressionManager().getLevel(target);
        int upd = Math.min(plugin.getProgressionManager().getMaxLevel(), Math.max(1, switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> cur - amount;
            default       -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        }));
        setProgressValue(target, "level", upd);
        plugin.getProgressionManager().savePlayer(target);
        plugin.getProgressionManager().updateDisplays(target);
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Level → <aqua>" + upd + "</aqua></green>");
    }

    private void handlePrestige(CommandSender sender, Player target, String action, int amount) {
        int cur = plugin.getProgressionManager().getPrestige(target);
        int upd = Math.max(0, switch (action) {
            case "set"    -> amount;
            case "add"    -> cur + amount;
            case "remove" -> cur - amount;
            default       -> { MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Use set, add or remove.</red>"); yield cur; }
        });
        setProgressValue(target, "prestige", upd);
        plugin.getProgressionManager().savePlayer(target);
        plugin.getProgressionManager().updateDisplays(target);
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <green>Prestige → <gradient:#f6d365:#fda085>" + upd + "</gradient></green>");
    }

    // ── tab complete ─────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("xdgens.admin")) return List.of();

        if (args.length == 1) {
            return filter(List.of("info", "reset", "givekey", "givetag", "givecolor",
                    "money", "tokens", "xp", "level", "prestige"), args[0]);
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            // player name for most subcommands
            if (List.of("info", "reset", "givekey", "givetag", "givecolor").contains(sub)) {
                return filterPlayers(args[1]);
            }
            // action for modify commands
            return filter(List.of("set", "add", "remove"), args[1]);
        }

        if (args.length == 3) {
            return switch (sub) {
                case "givekey"   -> filter(CRATE_TYPES, args[2]);
                case "givetag"   -> filter(TAGS, args[2]);
                case "givecolor" -> filter(COLORS, args[2]);
                default          -> filterPlayers(args[2]); // modify: player arg
            };
        }

        if (args.length == 4) {
            if (sub.equals("givekey")) return filter(List.of("1", "5", "10", "50"), args[3]);
            return filter(List.of("1k", "10k", "100k", "1m", "10m", "1b"), args[3]);
        }

        return List.of();
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Player resolvePlayer(CommandSender sender, String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Player '" + name + "' not found.</red>");
        return target;
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

    private double parseAmount(String input) throws NumberFormatException {
        String n = input.trim().toLowerCase().replace(",", ".");
        char last = n.charAt(n.length() - 1);
        double multiplier = 1.0;
        if (Character.isLetter(last)) {
            multiplier = switch (last) {
                case 'k' -> 1_000D; case 'm' -> 1_000_000D;
                case 'b' -> 1_000_000_000D; case 't' -> 1_000_000_000_000D;
                default  -> throw new NumberFormatException("Unknown suffix: " + last);
            };
            n = n.substring(0, n.length() - 1);
        }
        return Double.parseDouble(n) * multiplier;
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
        MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <gray>Admin commands:</gray>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin info <player></yellow>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin reset <player></yellow>");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givekey <player> <crate> [amount]</yellow> <dark_gray>— COMMON/RARE/EPIC/LEGENDARY");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givetag <player> <tag></yellow> <dark_gray>— e.g. TAG_LEGEND");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin givecolor <player> <color></yellow> <dark_gray>— e.g. COLOR_GRADIENT_FIRE");
        MessageUtil.sendRaw(sender, "<yellow>/xdadmin money/tokens/xp/level/prestige <set|add|remove> <player> <amount></yellow>");
    }
}
