#HarmoJam

**HarmoJam** is a music discovery web application that uses Spotify, Last.fm and Deezer APIs.

##Features

-**Advanced search engine** provides flexible, advanced music search engine using the Spotify API.
-**Recommendations** HarmoJam uses the Last.fm API to create "mixes" that recommend songs based on your taste. 
-**Inline player** Allows users to play/stop music previews directly within the interface without interruption.
-**Aesthetic Design** With a modern colour palette and smooth animations, HarmoJam aims to enhance the user experience.

##Tech Stack

**Backend**
* Java 17
* Spring Boot
* Maven

**Backend Utilities:**
* RestTemplate 
* Jackson

**Frontend**
* HTML5 & CSS3
* JavaScript

**APIs:**
* Spotify Web API (Search & Metadata)
* Deezer API (Audio Previews & Metadata)
* Last.fm API (Recommendations)

## Installation

1.  **Clone the project**
    ```bash
    git clone https://github.com/BoraKaanDemirbas/HarmoJam.git
    ```

2.  **Configure API Keys**
    Create a file named `application.properties` inside `src/main/resources/` and add your keys:
    ```properties
    spotify.client.id=YOUR_ID_HERE
    spotify.client.secret=YOUR_SECRET_HERE
    lastfm.api.key=YOUR_KEY_HERE
    ```

3.  **Run the App**
    Open the project in **IntelliJ IDEA** and run `DemoApplication.java`.
    
4.  **Explore**
    Go to `http://localhost:9090` in your browser.


## Author

**Bora Kaan Demirbaş**
- GitHub: [@BoraKaanDemirbas](https://github.com/BoraKaanDemirbas)
