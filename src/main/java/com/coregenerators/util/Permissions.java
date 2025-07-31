package com.coregenerators.util;

public final class Permissions {

    // === Allgemeine Rechte ===
    public static final String PLACE = "coregens.place";            // Generator platzieren
    public static final String USE = "coregens.use";                // GUI verwenden etc.

    // === Teamverwaltung ===
    public static final String TEAM_INVITE = "coregens.team.invite"; // Teammitglied einladen
    public static final String TEAM_KICK = "coregens.team.kick";     // Teammitglied entfernen

    // === Adminrechte ===
    public static final String ADMIN_GIVE = "coregens.admin.give";     // /coregen give
    public static final String ADMIN_RELOAD = "coregens.admin.reload"; // /coregen reload
    public static final String ADMIN_REMOVE = "coregens.admin.remove"; // /coregen remove
    public static final String ADMIN_DEBUG = "coregens.admin.debug";   // /coregen debug (optional)

    private Permissions() {
        // Keine Instanziierung erlaubt
    }
}