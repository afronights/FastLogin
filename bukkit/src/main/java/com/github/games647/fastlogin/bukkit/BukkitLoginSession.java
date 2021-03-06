package com.github.games647.fastlogin.bukkit;

import com.github.games647.fastlogin.core.PlayerProfile;
import com.github.games647.fastlogin.core.shared.LoginSession;

import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

/**
 * Represents a client connecting to the server.
 *
 * This session is invalid if the player disconnects or the login was successful
 */
public class BukkitLoginSession extends LoginSession {

    private final String serverId;
    private final byte[] verifyToken;

    private UUID uuid;
    private boolean verified;

    private String encodedSkinData;
    private String skinSignature;

    public BukkitLoginSession(String username, String serverId, byte[] verifyToken, boolean registered
            , PlayerProfile profile) {
        super(username, registered, profile);

        this.serverId = serverId;
        this.verifyToken = ArrayUtils.clone(verifyToken);
    }

    //available for bungeecord
    public BukkitLoginSession(String username, boolean registered) {
        this(username, "", ArrayUtils.EMPTY_BYTE_ARRAY, registered, null);
    }

    //cracked player
    public BukkitLoginSession(String username, PlayerProfile profile) {
        this(username, "", ArrayUtils.EMPTY_BYTE_ARRAY, false, profile);
    }

    /**
     * Gets the random generated server id. This makes sure the request sent from the client is just for this server.
     *
     * See this for details http://www.sk89q.com/2011/09/minecraft-name-spoofing-exploit/
     *
     * Empty if it's a BungeeCord connection
     *
     * @return random generated server id
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets the verify token the server sent to the client.
     *
     * Empty if it's a BungeeCord connection
     *
     * @return the verify token from the server
     */
    public byte[] getVerifyToken() {
        return ArrayUtils.clone(verifyToken);
    }

    public synchronized String getEncodedSkinData() {
        return encodedSkinData;
    }

    public synchronized String getSkinSignature() {
        return skinSignature;
    }

    /**
     * Sets the premium skin property which was retrieved by the session server
     *
     * @param encodedData
     * @param skinSignature
     */
    public synchronized void setSkin(String encodedData, String skinSignature) {
        this.encodedSkinData = encodedData;
        this.skinSignature = skinSignature;
    }

    /**
     * Sets whether the player has a premium (paid account) account and valid session
     *
     * @param verified whether the player has valid session
     */
    public synchronized void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Get the premium UUID of this player
     *
     * @return the premium UUID or null if not fetched
     */
    public synchronized UUID getUuid() {
        return uuid;
    }

    /**
     * Set the online UUID if it's fetched
     *
     * @param uuid premium UUID
     */
    public synchronized void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Get whether the player has a premium (paid account) account and valid session
     *
     * @return whether the player has a valid session
     */
    public synchronized boolean isVerified() {
        return verified;
    }
}
