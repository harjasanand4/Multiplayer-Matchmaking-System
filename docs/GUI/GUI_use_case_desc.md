### Use Case: View Profile

- **Use Case:** Player Views their Profile
- **Iteration:** 1
- **Primary Actor:** Player
- **Goal in Context:** To allow the player to view their profile, displaying stats, rankings, win/loss records
- **Preconditions:**
    - The player has an account and is logged in
- **Triggers:** The player clicks onto the profile screen
- **Scenario:**
    1. The player opens their profile screen
    2. The system retrieves the player's stats, rankings, and win/loss records
    3. The system displays the player's stats, rankings, and win/loss records.
    4. The player has the option to return back to the main menu/game library
    - **Post Conditions:**
        - The player views their profile information
- **Exceptions:**
    1. If the player has no game history, the system displays a message indication no data is available to view
    2. If the data is unavailable, the system displays an error message (e.g. "Profile unavailable. Try again later.")
- **Priority:** Medium -- Important for user satisfaction, but not essential for gameplay
- **When Available:**
- **Frequency of Use:** Whenever the player wants to check their stats
- **Channel to Actor:** GUI (System screen, keyboard)
- **Secondary Actors:** Account Database
- **Channel to Secondary Actors:** Retrieves profile data from the account database
- **Open Issues:** None