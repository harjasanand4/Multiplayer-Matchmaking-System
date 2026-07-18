## GUI Use Case Descriptions

### Use Case: Game Selection

- **Use Case:** Selecting a board game from the available game library.
- **Iteration:** 1st iteration
- **Primary Actor:** Player
- **Goal in Context:** To allow the player to browse and select a game they wish to play.
- **Preconditions:**
  - The player is logged into their account.
  - The system displays a list of available games.
  - Game thumbnails and descriptions are preloaded.
- **Triggers:** The player interacts with the game selection screen.
- **Scenario:**
  1. The system displays a list of available board games with their descriptions.
  2. The player browses the game list using scrolling or filtering options.
  3. The player selects a game by clicking or tapping on its thumbnail. The system loads the selected game’s details, including available rooms and game settings.
  4. The player confirms their selection and is redirected to the matchmaking or lobby screen.
- **Post Conditions:**
  - The selected game is loaded, and the player is ready to join or create a match.
  - The system updates the player’s recent game history.
- **Exceptions:**
  1. If the player attempts to select a game that is temporarily unavailable, an error message is displayed.
  2. If the system fails to load the game details, the player is prompted to try again.
  3. If there is a network issue, the player is notified and returned to the main menu.
- **Priority:** High; essential for accessing and playing games.
- **When Available:** Available as soon as the player logs in.
- **Frequency of Use:** High; every session starts with a game selection.
- **Channel to Actor:** Touchscreen, mouse/keyboard interaction.
- **Secondary Actors:** None.
- **Channel to Secondary Actors:** N/A.
- **Open Issues:**
  - Should the game selection screen have a recommended game feature?
  - How should unavailable games be displayed (grayed out, hidden, or marked with a notification)?

___

### Use Case: Creating a New Game Lobby

- **Use Case:** Creating a private or public game room.
- **Iteration:** 1st iteration
- **Primary Actor:** Player
- **Goal in Context:** To allow a player to create a new game lobby and invite others.
- **Preconditions:**
  - The player has selected a game.
  - The system is connected to the matchmaking service.
- **Triggers:** The player chooses to create a new game.
- **Scenario:**
  1. The player clicks on “Create Game.”
  2. The system prompts the player to select game settings (e.g., time limit, board size, AI/bots).
  3. The player sets the game to public or private.
  4. If private, the player invites friends or shares a room code.
  5. The system creates the game lobby and waits for other players.
  6. The player clicks “Start Game” once enough players have joined.
- **Post Conditions:**
  - A game session is successfully created.
  - The invited players receive notifications to join.
  - The system updates the database with the new game session.
- **Exceptions:**
  1. If the player disconnects before starting the game, the lobby is deleted.
  2. If a private game invite fails, the system retries sending it.
  3. If no players join within a set time, the system offers to convert it to an AI match.
- **Priority:** High; core functionality for multiplayer games.
- **When Available:** After selecting a game.
- **Frequency of Use:** Moderate; occurs when players want to host a match.
- **Channel to Actor:** Touchscreen, mouse/keyboard.
- **Secondary Actors:** Invited players.
- **Channel to Secondary Actors:** Notifications, chat invites.
- **Open Issues:**
  - Should private game lobbies expire after a certain time?
  - Should players be able to customize game rules before starting?

___

### Use Case: In-Game Chat System

- **Use Case:** Sending and receiving chat messages during a game.
- **Iteration:** 1st iteration
- **Primary Actor:** Player
- **Goal in Context:** To enable real-time text-based communication between players.
- **Preconditions:**
  - The player is inside a game session.
  - The chat system is enabled for the game.
- **Triggers:** A player sends a message in the chat.
- **Scenario:**
  1. The player types a message in the chat input box.
  2. The system sends the message to the game server.
  3. The message appears in the chat window for all players.
  4. The system timestamps and logs the chat message.
- **Post Conditions:**
  - The message is visible to all game participants.
  - The system records the message for moderation if needed.
- **Exceptions:**
  1. If the player sends a message that violates community guidelines, it is flagged or blocked.
  2. If there is a network issue, the message is queued and sent when reconnected.
  3. If chat is disabled, the player is notified that messaging is unavailable.
- **Priority:** Medium; enhances user experience but is not core to gameplay.
- **When Available:** During an active game session.
- **Frequency of Use:** Moderate; varies based on player interaction.
- **Channel to Actor:** Keyboard input, touchscreen.
- **Secondary Actors:** Other players in the game.
- **Channel to Secondary Actors:** Chat window.
- **Open Issues:**
  - Should messages be stored permanently or disappear after a game ends?
  - Should players be able to mute individual opponents?
