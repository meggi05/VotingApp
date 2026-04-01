# VotingApp

Desktop application built with JavaFX for conducting student votes on the best university flower. Supports user authentication, voting, real-time statistics visualization, and data persistence.

## Features

- User registration and authentication
- Voting for a flower (one vote per user)
- Cancel vote option
- Statistics displayed as a bar chart
- Flower details view with description and enlarged image
- Persistent storage of user accounts and votes (local files)
- Export voting results to a TXT file

## Technologies

- Java 21
- JavaFX 17.0.6
- Maven
- IntelliJ IDEA

## Project Structure
```
├── pom.xml
├── .gitignore
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/example/rgzgolos/
        │       ├── VotingApp.java
        │       └── Flower.java
        └── resources/
            └── images/
                ├── Герань.jpg
                ├── Циния.jpg
                ├── Бархатцы.jpg
                ├── Гиацинт.jpg
                ├── Альстромерия.jpg
                └── Люпин.jpg
```
## Installation and Running

### Prerequisites

- JDK 21 or higher
- IntelliJ IDEA (Community or Ultimate Edition)

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/meggi05/VotingApp.git
   cd VotingApp
   ```
2. Open project in IntelliJ IDEA:

Launch IntelliJ IDEA

File → Open → select the VotingApp folder

Wait for Maven dependencies to resolve automatically

3. Run the application:
   - Navigate to `src/main/java/com/example/rgzgolos/VotingApp.java`
   - Find the `main` method
   - Click the green play button next to it
   - Select "Run 'VotingApp.main()'"

Author
@meggi05 — Student at Novosibirsk State Technical University (NSTU)
