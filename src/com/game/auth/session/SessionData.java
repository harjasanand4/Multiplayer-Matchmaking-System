package com.game.auth.session;

/**
 * Represents session metadata for a specific user session.
 * This class is intended to be used for storage of ANY session-related data.
 *
 * @author Thomas Tabur, Maneet Singh, Boya Liu, Michael Olsen
 * @email: thomas.tabur@ucalgary.ca, maneet.singh1@ucalgary.ca, boya.liu@ucalgary.ca, olsen.olsen@ucalgary.ca
 */
public class SessionData {
    private long lastActiveTime;
    private boolean isPromptShown;

    /**
     * Constructs a SessionData object with the given last active timestamp.
     *
     * @param lastActiveTime The time the session was last active (in milliseconds).
     */
    public SessionData(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
        this.isPromptShown = false; // Initialize prompt status to false
    }

    /**
     * Returns the timestamp of the user's last activity.
     *
     * @return The last active time in milliseconds.
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * Updates the timestamp of the user's last activity.
     *
     * @param lastActiveTime The new last active time in milliseconds.
     */
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    /**
     * Returns whether the inactivity prompt has already been shown.
     *
     * @return true if the prompt was shown; false otherwise.
     */
    public boolean isPromptShown() {
        return isPromptShown;
    }

    /**
     * Sets the status of whether the inactivity prompt has been shown.
     *
     * @param promptShown true if the prompt is shown; false otherwise.
     */
    public void setPromptShown(boolean promptShown) {
        this.isPromptShown = promptShown;
    }

}
