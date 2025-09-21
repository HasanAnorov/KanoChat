# 📡 KanoMessenger

**KanoMessenger** is a mobile application for **local communication without using the Internet**.  
It enables text, audio, and file messaging inside a private LAN using **three different connection methods**, giving you flexible offline chat and sharing.

Built with **Kotlin**, **Jetpack Compose**, and low-level **socket networking**.

---

## ✨ Features at a Glance
- **Three Local Communication Modes**
  - **Local-Only Hotspot**: temporary Wi-Fi access point without Internet sharing.
  - **Group Networking**: Wi-Fi Direct group owner for configurable multi-device LAN.
  - **Peer Networking**: quick P2P links between two devices.
- **Rich Messaging**
  - Send **text, emojis, photos, contacts, files**, and **voice messages**.
  - Real-time chat with online/offline presence.
- **Flexible Networking**
  - Configurable hotspot name & password.
  - Custom proxy port for server connections.
  - Shows server address, connection status, and connected devices.
- **User Experience**
  - Multi-language support.
  - Light/Dark theme toggle.
  - Adjustable broadcast frequency (2.4 GHz / 5 GHz).

---

## 📱 App Screens & Flow


### 1️⃣ Login
<img src="screenshots/login.jpg" width="250">

Simple greeting screen with a username field and **Login** button.  
Once logged in, you are taken to the **Home** page.

---

### 2️⃣ Home – Tabs Overview
Home is divided into **three tabs**.

| Tab | Purpose |
|-----|---------|
| **Chats** | View and open past conversations. |
| **Networking** | Create or join local networks. |
| **Connections** | Create a socket server or join one. |

---

### 3️⃣ Networking (second tab)
<img src="screenshots/networking.jpg" width="250">

Core of KanoMessenger: choose how to establish an offline network.

**Communication Methods**

1. **Start Local-Only Hotspot**  
   - Uses Android’s `LocalOnlyHotspot` API.  
   - Creates a temporary Wi-Fi hotspot (no Internet).  
   - Other devices join to send/receive data.  
   - Range ~100 m.  
   - Quick backup method, no manual SSID/password configuration.

2. **Group Networking (recommended)**  
   - Uses Wi-Fi Direct (`WifiP2pManager`).  
   - One device becomes Group Owner (like a router, no Internet).  
   - Configurable **network name and password**.  
   - Range ~100–200 m.

3. **Peer Networking**  
   - Device-to-device link via Wi-Fi Direct peer discovery.  
   - Good for quick file transfers or direct pairing.  
   - Range ~200 m outdoors.

Additional info shown on this page:
- **Hotspot Name & Password** (if created)
- **Wi-Fi Status** and **Networking Status**
- **Available Wi-Fi Networks** list for peer discovery

---

### 4️⃣ Connections (third tab)
<img src="screenshots/connections.jpg" width="250">

Where actual messaging sockets are created.

- **Create Server**: start a socket server (configure custom port to avoid conflicts).  
- **Connect**: join as a client to an existing server.  
- **Connection Details**: live status, server IP address, and list of connected devices.

This tab powers all real-time messaging once devices are on the same network.

---

### 5️⃣ Chats (first tab)
<img src="screenshots/chats.jpg" width="250">

- Shows all previous conversations.  
- If none exist, displays a **create-network** prompt.

Open any chat to see full history and partner presence (online/offline).

**Conversation view**  
<img src="screenshots/conversation.jpg" width="250">

- Supports **text, audio, file, contact, emoji, and photo** messages.
- Bottom bar includes:
  - Text input
  - Emoji selector
  - Photo picker
  - Contact/file sharing
  - Microphone button for voice messages

Messages are exchanged instantly when both users are online within the chosen network.

---

### 6️⃣ Settings
<img src="screenshots/settings.jpg" width="250">

- **General**  
  - Language selector  
  - App theme (light/dark)
- **Broadcast Frequency**  
  - 2.4 GHz (slower, more compatible)  
  - 5 GHz (faster, shorter range)
- **Logout** button

---

## 🛠 Tech Stack & Architecture
- **Language**: Kotlin (100%) + some Java
- **UI**: Jetpack Compose
- **Networking**:  
  - Socket programming (TCP)  
  - Android `WifiP2pManager` (Wi-Fi Direct)  
  - `LocalOnlyHotspot`
- **Core Capabilities**:
  - Real-time messaging
  - Emulator detection system
  - Unique device identification
  - Multi-language & theme management
- **Build System**: Gradle (Kotlin DSL)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio **Arctic Fox (2020.3.1)** or newer
- Android **API 26+** (Oreo or higher)
- Device with Wi-Fi Direct capability


## 💡 Usage Tips

- All communication happens **inside your local network**.  
  No data ever goes to the Internet.
- For best results, use **Group Networking** for stable multi-device chats.
- Use **Peer Networking** for quick one-to-one connections.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome.  
Feel free to open an issue or submit a pull request.

---

## 👤 Author

**Khasan Anorov (Kano)**  
Developer of KanoMessenger

---

### Folder Structure (overview)

- .idea/             # Android Studio configs
- app/              # Main application code
- gradle/           # Gradle wrapper
- build.gradle.kts  # Project build script
- settings.gradle.kts
- README.md


---

> ⚠️ **Privacy Note**  
> KanoMessenger never requires Internet and does not send user data to external servers.
