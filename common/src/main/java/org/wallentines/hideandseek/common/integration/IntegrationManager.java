package org.wallentines.hideandseek.common.integration;

public class IntegrationManager {

    private static Boolean MIDNIGHT_ESSENTIALS;

    public static boolean isMidnightEssentialsPresent() {

        if(MIDNIGHT_ESSENTIALS == null) {
            try {
                Class.forName("org.wallentines.midnightessentials.api.MidnightEssentialsAPI");
                MIDNIGHT_ESSENTIALS = true;
            } catch (ClassNotFoundException ex) {
                MIDNIGHT_ESSENTIALS = false;
            }
        }

        return MIDNIGHT_ESSENTIALS;
    }

}
