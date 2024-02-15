package com.yd.backendmessaging;

import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BackendMessaging extends JavaPlugin implements Listener, PluginMessageListener {
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, "velocitygui:warpchannel", this);
        getLogger().info("PluginMessageListener for 'velocitygui:warpchannel' has been registered.");
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getLogger().info("PluginMessageListener has been unregistered.");
    }

    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("velocitygui:warpchannel")) {
            getLogger().warning("Received message for unrecognized channel: " + channel);
            return;
        }
        String warpName = null;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            try {
                String playerName = in.readUTF();
                warpName = in.readUTF();
                getLogger().info("Received warp request for " + playerName + " to warp " + warpName);
                IEssentials ess = (IEssentials)getServer().getPluginManager().getPlugin("Essentials");
                if (ess == null) {
                    getLogger().severe("EssentialsX plugin not found.");
                    in.close();
                    return;
                }
                Location warpLocation = ess.getWarps().getWarp(warpName);
                Player targetPlayer = getServer().getPlayer(playerName);
                if (targetPlayer == null) {
                    getLogger().warning("Player " + playerName + " not found for warp " + warpName);
                    in.close();
                    return;
                }
                targetPlayer.teleport(warpLocation);
                player.sendMessage(ChatColor.GOLD + "워프 완료!");
                getLogger().info("Player " + playerName + " warped to " + warpName);
                in.close();
            } catch (Throwable throwable) {
                try {
                    in.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException e) {
            getLogger().severe("Error processing warp request: " + e.getMessage());
        } catch (WarpNotFoundException|net.ess3.api.InvalidWorldException e) {
            getLogger().warning("Warp " + warpName + " not found: " + e.getMessage());
        } catch (Exception e) {
            getLogger().severe("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
