package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers.ParamInfo;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisguiseModifyEntityCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(
                    TranslateType.MESSAGE.get(ChatColor.RED + "You may not use this command from the " + "console!"));
            return true;
        }

        if (getPermissions(sender).isEmpty()) {
            sender.sendMessage(TranslateType.MESSAGE.get(ChatColor.RED + "You are forbidden to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, getPermissions(sender));
            return true;
        }

        LibsDisguises.getInstance().getListener().setDisguiseModify(sender.getName(), args);

        sender.sendMessage(String.format(TranslateType.MESSAGE.get(
                ChatColor.RED + "Right click a disguised entity " + "in the next %s seconds to modify their disguise!"),
                DisguiseConfig.getDisguiseEntityExpire()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return tabs;
        }

        String[] args = getArgs(origArgs);

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> perms = getPermissions(sender);

        if (perms.isEmpty())
            return tabs;

        for (DisguisePerm perm : perms.keySet()) {
            boolean addMethods = true;

            if (args.length > 1) {
                String prevArg = args[args.length - 1];

                ParamInfo info = ReflectionFlagWatchers.getParamInfo(perm.getType(), prevArg);

                if (info != null) {
                    if (info.getParamClass() != boolean.class)
                        addMethods = false;

                    if (info.isEnums()) {
                        for (String e : info.getEnums(origArgs[origArgs.length - 1])) {
                            tabs.add(e);
                        }
                    } else {
                        if (info.getParamClass() == String.class) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                tabs.add(player.getName());
                            }
                        }
                    }
                }
            }

            if (addMethods) {
                // If this is a method, add. Else if it can be a param of the previous argument, add.
                for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(
                        perm.getType().getWatcherClass())) {
                    tabs.add(method.getName());
                }
            }
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     *
     * @param sender
     * @param map
     */
    @Override
    protected void sendCommandUsage(CommandSender sender,
            HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(map);

        sender.sendMessage(TranslateType.MESSAGE.get(
                ChatColor.DARK_GREEN + "Choose the options for a disguise then right click a entity to modify it!"));
        sender.sendMessage(
                String.format(TranslateType.MESSAGE.get(ChatColor.DARK_GREEN + "You can modify the " + "disguises: %s"),
                        ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
