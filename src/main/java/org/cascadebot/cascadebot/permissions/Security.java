/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.permissions;

import org.cascadebot.cascadebot.CascadeBot;
import org.cascadebot.cascadebot.data.Config;
import org.cascadebot.shared.SecurityLevel;

import java.util.Set;

/**
 * Security levels defined by Role IDs or User IDs, A level can be defined by an unlimited amount of roles
 * and users. Security levels are declared in ascending order.
 */
public class Security {

    /**
     * Returns the list of role and user IDs that will authenticate against this security level
     *
     * @param level The level to get IDs for
     * @return The list of IDs authenticated against this level
     */
    public static Set<Long> getIds(SecurityLevel level) {
        return Config.INS.getSecurityLevels().get(level);
    }

    /**
     * Checks if the user's level is greater than or equal to the level we are comparing against.
     *
     * @param userId         The user to check the level of.
     * @param comparingLevel The level to compare against.
     * @return where level >= comparing level.
     */
    public static boolean isAuthorised(long userId, SecurityLevel comparingLevel) {
        SecurityLevel level = CascadeBot.INS.getPermissionsManager().getUserSecurityLevel(userId);
        if (level == null) return false;
        return level.isAuthorised(comparingLevel);
    }

    /**
     * Checks if the level is greater than or equal to the level we are comparing against.
     *
     * @param level          The level we are checking.
     * @param comparingLevel The level to compare against.
     * @return where level >= comparing level.
     */
    public static boolean isAuthorised(SecurityLevel level, SecurityLevel comparingLevel) {
        return level.isAuthorised(comparingLevel);
    }

    /**
     * Returns the highest security level a user has access to.
     *
     * @param userId  The ID of the user to check.
     * @param roleIds The list of role IDs to check against, this will almost always be the roles from the official server.
     * @return The highest security level the user has access to or {@code null} if they do not have access to anything.
     */
    public static SecurityLevel getLevelById(long userId, Set<Long> roleIds) {
        for (int i = SecurityLevel.values().length - 1; i >= 0; i--) {
            SecurityLevel level = SecurityLevel.values()[i];
            if (Security.getIds(level).contains(userId)) return level;
            if (roleIds.stream().anyMatch(id -> Security.getIds(level).contains(id))) return level;
        }
        return null;
    }

}
