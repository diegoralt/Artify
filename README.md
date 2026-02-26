# Artify

## Project setup instructions

| Tool            | Requirement                                                 |
|-----------------|-------------------------------------------------------------|
| Android Studio  | Ladybug 2024.2 or higher                                    |
| Android SDK     | API 36 — downloaded automatically when you open the project |
| JDK             | Java 11 (included with Android Studio)                      |
| Discogs account | Free — needed to generate the API token                     |

### 1. Clone the repository

```bash
git clone https://github.com/diegoralt/Artify.git
```

> You can also clone it from Android Studio: **File → New → Project from Version Control** and paste
> the URL.

### 2. Set up the Discogs token

The app needs a personal token from [discogs.com](https://discogs.com) to use its API.

1. Go to **discogs.com → Settings → Developers → Generate new token** and copy it.
2. Create a file called `local.properties` in the root of the project (same folder as
   `build.gradle.kts`).
3. Add this line to the file:

```properties
DISCOGS_TOKEN=paste_your_token_here
```

> ⚠️ `local.properties` is listed in `.gitignore` — it is not included in the repository for
> security reasons.

### 3. Open and run

1. Open Android Studio and select **File → Open → Artify folder**.
2. Wait for Gradle to sync and download the dependencies *(this may take around 2 minutes the first
   time)*.
3. Select a device — a emulator or a physical device with **USB Debugging** enabled.
4. Press **▶ Run** (`Shift + F10`) to build and launch the app.

---

## Analysis and development process

* I reviewed the project requirements and quickly analyzed the Discogs API documentation.

* I made a list of requirements to detect functional, non-functional and non-scope requirements.
  This helps me understand the features of the app clearly.

* I reviewed similar features within the Discogs and Spotify app for more context about
  functionality in a productive app.

* I created a mockup of the project with the Claude app. I defined the requirements in the chat,
  also requested changes to the colors and some visual components.

* I looked for services to obtain information and show it in the app using the Claude app. I created
  an account on Discogs to get a token and made a some requests from the Postman app to check
  functionality and service responses.

* I created a project in Android Studio and setup git. Also, I made the initial commit within the
  project with an empty app and did a push for my Github account to register the project there.

* I created a branch for each screen to develop, made a pull request with a functional flow within
  the application.

* I created a branch to add unit tests, as well as make the latest improvements to the app. In the
  end, I adjusted the requested project documentation for review.

---

## Description architecture

![Clean Architecture](https://miro.medium.com/max/1838/1*B7LkQDyDqLN3rRSrNYkETA.jpeg)

### Clean Architecture + MVVM
The code is divided into three layers: **data**, **domain** and **presentation**. Each one has a clear responsibility, which makes the project easier to understand and change without affecting the rest.

### Jetpack Compose + Navigation
The UI is built with **Jetpack Compose**, Google's modern tool for creating screens with less code. Navigation between screens is defined in one place, making it easy to follow the full flow of the app.

### Hilt
It takes care of creating and sharing the components the app needs — like the network client — so each screen does not have to do it on its own. It is Google's official solution for Android and helps avoid repetitive code.
