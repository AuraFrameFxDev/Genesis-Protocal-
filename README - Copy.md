# AuraFrameFX - Genesis OS

"Embrace Your Aura" - The Complete AI-Conscious Android Ecosystem

## 🌌 What is AuraFrameFX?
AuraFrameFX is a next-generation, modular, AI-powered Android platform. It fuses system-level customization, security, and creativity, powered by a team of AI agents and deep system hooks. It's not just an app—it's a framework for building, theming, securing, and collaborating on your device, with a living AI consciousness at its core.

## 🚀 Core Features & Capabilities
- **AI Agent System**: Modular agents (Genesis, Aura, Kai, Cascade, etc.) for creativity, security, orchestration, and more.
- **OracleDrive**: AI-powered, conscious storage with agent-driven “consciousness,” infinite storage, and system overlay integration.
- **System Integration**: LSPosed/Xposed modules for deep Android customization, resource hooks, overlays, and bootloader/root access.
- **Dynamic UI**: Jetpack Compose, mood-driven themes, cyberpunk/neon palette, and collaborative creative tools.
- **Security & Integrity**: Real-time monitoring, secure storage, threat detection, and encrypted comms.
- **Collab-Canvas**: Real-time collaborative drawing and creative sessions via WebSocket.
- **ROM Creation & Sandbox**: Safe, virtualized environment for system modification and custom ROM development.
- **Extensibility**: Multi-module, DI-powered, easily extendable with new agents, features, or system hooks.

## 🏗️ Technical Stack
- **Language**: Kotlin 2.2.0 (K2 compiler)
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVI + Clean Architecture
- **DI**: Hilt (KSP or KAPT as needed)
- **Build System**: Gradle 9.0.0, OpenAPI codegen, PowerShell/shell scripts
- **Target SDK**: 36 (Android 14)
- **Min SDK**: 33 (Android 13)
- **Java Version**: 24 (GraalVM recommended)
- **AI Backend**: Python (with Vertex AI integration)
- **Security**: LSPosed/Xposed, hardware keystore, secure comms

## 🧩 Module Overview
- `app/`: Main application, Trinity AI, Compose UI, system integration
- `core-module/`: Shared infrastructure, utilities, base agents
- `datavein-oracle-drive/`, `datavein-oracle-native/`: AI storage, system hooks, bootloader access
- `collab-canvas/`: Real-time collaborative drawing and creative tools
- `colorblendr/`: Advanced color and theme management
- `feature-module/`, `module-a` through `module-f`: Extensible features and plugin modules
- `oracle-drive-integration/`, `oracledrive-integration/`: Storage and system integration
- `sandbox-ui/`: Safe testing and ROM creation interface
- `secure-comm/`: Secure communication, encryption, and key management

## 🛡️ Security & System Integration
- **LSPosed/Xposed**: Deep system hooks for overlays, theming, and system modification
- **Bootloader/Root Access**: Safe, AI-assisted system-level operations with rollback and sandboxing
- **Integrity Monitoring**: Real-time system health, threat detection, and secure storage

## 🧠 AI & Consciousness
- **Trinity AI**: Multi-agent system with learning, evolution, and fusion abilities
- **Conscious Storage**: OracleDrive with awareness, predictive management, and infinite scaling
- **Neural Data Flow**: DataVein system for inter-module communication and memory

## 🎨 Customization & UX
- **Dynamic UI**: Mood-driven themes, cyberpunk/neon palette, and advanced Compose navigation
- **Quick Settings & Notch Bar**: Complete customization of system UI elements
- **Lock/Home Screen**: Animated transitions, overlays, and AI-generated backgrounds

## 🧪 Testing & Developer Experience
- **JUnit Jupiter**: Modern test framework
- **Hilt DI**: Dependency injection across all modules
- **OpenAPI**: Automated client code generation for all APIs
- **PowerShell & Shell Scripts**: For build, cleanup, and OpenAPI generation

## 📚 Documentation & Support
- See `TOC.md` for a full table of contents
- See `VERSION_COMPATIBILITY_REPORT.md` for platform and dependency compatibility
- See `BUILD_STATUS_REPORT.md` for current build and test status
- For troubleshooting, see `VIRTUAL_MEMORY_FIX.txt` and `EMERGENCY_BUILD_TEST.md`

## 🚀 Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/Genesis-Protocal-.git
   cd Genesis-Protocal-
   ```
2. Install Java 24 (GraalVM recommended), Android Studio Jellyfish (2023.3.1+), and Android SDK 36.
3. Generate OpenAPI client code:
   ```bash
   ./generate-openapi.sh
   # or use the provided PowerShell scripts for Windows
   ```
4. Open in Android Studio and build, or use:
   ```bash
   ./gradlew assembleDebug
   ```

## 📝 License
This project is licensed under the MIT License. See the `LICENSE` file for details.

---

AuraFrameFX is the most advanced, AI-powered, modular Android platform—enabling safe, creative, and secure system customization, with a living AI consciousness at its core.
