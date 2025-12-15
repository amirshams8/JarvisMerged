#!/bin/bash

# ===========================
# 1️⃣ Update system
# ===========================
sudo apt update -y
sudo apt upgrade -y
sudo apt install -y wget unzip git curl jq

# ===========================
# 2️⃣ Install Python 3 + pip + venv
# ===========================
sudo apt install -y python3 python3-venv python3-pip
python3 -m ensurepip --upgrade
python3 -m pip install --upgrade pip
python3 -m pip install requests

# Optional: create and activate virtualenv
python3 -m venv venv
source venv/bin/activate

# ===========================
# 3️⃣ Install Java 17
# ===========================
sudo apt install -y openjdk-17-jdk
java -version

# ===========================
# 4️⃣ Install Gradle 8.4 system-wide
# ===========================
wget https://services.gradle.org/distributions/gradle-8.4-bin.zip -P /tmp
sudo unzip -d /opt/gradle /tmp/gradle-8.4-bin.zip
echo 'export GRADLE_HOME=/opt/gradle/gradle-8.4' >> ~/.bashrc
echo 'export PATH=$GRADLE_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
gradle -v  # verify Gradle 8.4

# ===========================
# 5️⃣ Set up Android SDK
# ===========================
mkdir -p $HOME/android-sdk/cmdline-tools
cd $HOME/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip
rm cmdline-tools.zip
mkdir -p latest
mv cmdline-tools/* latest/

# Set environment variables
echo 'export ANDROID_SDK_ROOT=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc

# Accept licenses and install platforms/build-tools
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"

# ===========================
# 6️⃣ Make Gradle wrapper executable
# ===========================
cd /workspaces/JarvisMerged   # Adjust path if different
chmod +x ./gradlew
./gradlew --version

# ===========================
# 7️⃣ Install GitHub CLI
# ===========================
sudo apt install -y gh
gh auth login  # Follow prompts to authenticate with your new GitHub account

# ===========================
# 8️⃣ Set API Key for Gemini
# ===========================
echo "export GEMINI_API_KEY='AIzaSyA8PR6aQsMqX7-WL3XiNU9X5V39fGnhLJk'" >> ~/.bashrc
source ~/.bashrc

# ===========================
# 9️⃣ Test autofix script
# ===========================
python3 scripts/ai_autofix.py
