# OMG – Online Multiplayer Gaming

## Game Objective
**OMG** is an online multiplayer arcade-style game where players compete in real time. The objective is to connect with friends, join matches, and win rounds through responsive gameplay and strategic decisions. It incorporates authentication, matchmaking, and leaderboard tracking — all in one cohesive JavaFX interface.

---
##  Prerequisites

Before running the project, make sure you have the following installed:

- **Java Development Kit (JDK)** (version 17 or higher recommended)
- **JavaFX SDK** (version 17 or higher)
##  Dependencies

This project uses **JavaFX**, which is not bundled with the JDK. You must download and configure the JavaFX SDK separately.

You can download JavaFX SDK from:  
https://gluonhq.com/products/javafx/

Make sure to:

1. **Extract** the SDK.
2. **Set the path** to the JavaFX SDK in your IDE or project settings.

##  How to Run the Program

###  Requirements
- Java JDK 17 or newer
- IntelliJ IDEA (or any Java IDE)
- Internet connection (for email-based authentication
### Steps
1. **Clone or unzip the project** into a working directory.
2. **Open in IntelliJ**
3. **Run the Main GUI Class**:
    - Navigate to: `src/com/game/gui/Main.java`
    - Run the `main()`

---

## Project Structure

```
src/com/game/
├── auth            # Handles registration, login, 2FA, and user verification
├── gamelogic       # Core game engine, animation, and mechanics
├── gui             # JavaFX-based user interface
├── leaderboard     # Tracks and displays player rankings
├── matchmaking     # Finds and connects players for online matches
├── networking      # Handles client-server requests and friend management
```

Other directories:
- `test/` – Unit tests
- `lib/` – External libraries (e.g., SendGrid, JUnit)
- `resources/` – GUI assets or config files
- `user_authentication_data/` – data storage for login sessions  
