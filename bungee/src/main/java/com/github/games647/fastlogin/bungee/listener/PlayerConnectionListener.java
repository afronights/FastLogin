package com.github.games647.fastlogin.bungee.listener;

import com.github.games647.fastlogin.bungee.FastLoginBungee;
import com.github.games647.fastlogin.bungee.tasks.AsyncPremiumCheck;
import com.github.games647.fastlogin.bungee.tasks.ForceLoginTask;
import com.github.games647.fastlogin.core.PlayerProfile;
import com.github.games647.fastlogin.core.shared.LoginSession;
import com.google.common.base.Charsets;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Enables online mode logins for specified users and sends
 * plugin message to the Bukkit version of this plugin in
 * order to clear that the connection is online mode.
 */
public class PlayerConnectionListener implements Listener {

    private final FastLoginBungee plugin;

    public PlayerConnectionListener(FastLoginBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent preLoginEvent) {
        if (preLoginEvent.isCancelled()) {
            return;
        }

        preLoginEvent.registerIntent(plugin);
        
        PendingConnection connection = preLoginEvent.getConnection();
        AsyncPremiumCheck asyncPremiumCheck = new AsyncPremiumCheck(plugin, preLoginEvent, connection);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, asyncPremiumCheck);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(LoginEvent loginEvent) {
        if (loginEvent.isCancelled()) {
            return;
        }

        //use the loginevent instead of the postlogin event in order to send the loginsuccess packet to the client
        //with the offline uuid this makes it possible to set the skin then
        PendingConnection connection = loginEvent.getConnection();
        String username = connection.getName();
        if (connection.isOnlineMode()) {
            String ip = connection.getAddress().getAddress().getHostAddress();
            plugin.getCore().getPendingLogins().remove(ip + username);

            LoginSession session = plugin.getSession().get(connection);
            PlayerProfile playerProfile = session.getProfile();
            playerProfile.setUuid(connection.getUniqueId());

            //bungeecord will do this automatically so override it on disabled option
            InitialHandler initialHandler = (InitialHandler) connection;
            if (!plugin.getConfig().getBoolean("premiumUuid")) {
                try {
                    UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));

                    //bungeecord doesn't support overriding the premium uuid
                    //so we have to do it with reflection
                    Field idField = InitialHandler.class.getDeclaredField("uniqueId");
                    idField.setAccessible(true);
                    idField.set(connection, offlineUUID);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to set offline uuid", ex);
                }
            }

            if (!plugin.getConfig().getBoolean("forwardSkin")) {
                //this is null on offline mode
                LoginResult loginProfile = initialHandler.getLoginProfile();
                if (loginProfile != null) {
                    loginProfile.setProperties(new Property[]{});
                }
            }
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent serverConnectedEvent) {
        ProxiedPlayer player = serverConnectedEvent.getPlayer();
        ForceLoginTask loginTask = new ForceLoginTask(plugin, player, serverConnectedEvent.getServer());
        ProxyServer.getInstance().getScheduler().runAsync(plugin, loginTask);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        ProxiedPlayer player = disconnectEvent.getPlayer();
        plugin.getSession().remove(player.getPendingConnection());
        plugin.getCore().getPendingConfirms().remove(player.getUniqueId());
    }
}
