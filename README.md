# WhatsApp AI Agent (Android starter)

This is a starter project for an Android notification-based WhatsApp auto-reply prototype.

## What it does
- Listens for WhatsApp notifications after the user grants Notification Access.
- Checks a locally configured allowlist of sender display names.
- Finds a notification action with `RemoteInput`.
- Sends the message text to a backend endpoint for AI reply generation.
- Submits the generated text through that notification's reply action.

## Important limitations
- This is not an official WhatsApp integration. It only works when WhatsApp posts a notification containing usable sender/message text and a compatible reply action.
- It cannot read full WhatsApp chat history, reliably identify phone numbers from every notification, or reply to every message type.
- Android/OEM battery management can delay or stop background work.
- Do not use this for sensitive conversations or unattended high-stakes messages. Add blocklists, rate limits, duplicate detection, a pause switch, and logs before daily use.
- Test with one trusted contact first. Keep the app in draft/test mode until you have verified its behavior.

## Security architecture
The Android app calls your own backend. **Do not put an OpenAI API key in the Android app.**
The included backend reads `OPENAI_API_KEY` from its environment and calls the OpenAI Responses API.

## Requirements
- Android Studio (recent stable version), JDK 17, Android SDK 35
- Node.js 20+ for the backend
- An OpenAI API key and API billing configured

## Run the backend
```bash
cd backend
npm install
OPENAI_API_KEY="your_key" npm start
```
The backend listens on port 3000. For a physical phone, deploy it to an HTTPS host or use a secure tunnel during development; `localhost` on the phone is not your computer.

Set `AGENT_API_URL` in `app/build.gradle.kts` to your HTTPS backend URL, ending in `/reply`.

## Open Android project
Open this folder in Android Studio and let Gradle sync. Build and install the app on your Android phone.
Then:
1. Open the app and enable WhatsApp auto-reply.
2. Add the exact sender display name shown in WhatsApp notifications to the allowlist.
3. Grant Notification Access when Android prompts you.
4. Send a test message from that contact.
5. Check the app/backend logs before enabling unattended replies.

## Current prototype status
The starter includes the notification listener and basic reply flow. It intentionally does not enable itself silently; the user must grant notification access. The simple allowlist is display-name based and should be upgraded to a user-confirmed contact mapping before production.

## Build APK using GitHub Actions (phone-friendly)
1. Create a GitHub repository in your mobile browser.
2. Upload the contents of this project ZIP to the repository (not the ZIP file itself).
3. Open the repository's **Actions** tab and enable workflows if prompted.
4. Select **Build Android APK** and tap **Run workflow** (or push to `main`).
5. Open the completed workflow run, scroll to **Artifacts**, and download `WhatsAppAIAgent-debug-apk`.
6. Extract the artifact ZIP and install `app-debug.apk` on your phone. You may need to allow installs from your browser/files app.

The APK will compile with the placeholder backend URL, but AI replies will not work until you deploy the backend over HTTPS and update `AGENT_API_URL` in `app/build.gradle.kts`, then rebuild.
