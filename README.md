# Roblox Revenge

An experimental Android project exploring whether Roblox gameplay can be presented inside a separate Discord/Revenge-based client.

## Project identity

- App name: Roblox Revenge
- Android application ID target: `com.roblox.revenge`
- Base client: Revenge Classic (regular Revenge), not Revenge Next
- Target platform: Android

## Important architecture note

The official [Revenge bundle](https://github.com/revenge-mod/revenge-bundle) is a JavaScript client-modification bundle loaded by a compatible Android loader. It is **not** a standalone Android APK and does not contain Roblox's game engine.

Changing the Android application ID therefore belongs in the Android host/packaging project, not in the JavaScript bundle alone. The Roblox portion will need a separately validated, supported way to launch or display the official Roblox Android client. This repository must not pretend that a WebView or a Revenge plugin can embed Roblox's native game engine.

## Upstream

- [Official Revenge Classic source](https://github.com/revenge-mod/revenge-bundle)
- [Official Revenge Android manager](https://github.com/revenge-mod/revenge-manager)
- [Official Revenge documentation](https://revenge.palmdevs.me/docs/)

Revenge bundle is licensed under the BSD 3-Clause license. Preserve its license and notices when incorporating or modifying its code. Check the license of every additional upstream component before copying it.

## Development status

- [x] Project repository initialized
- [ ] Import/pin a compatible Revenge Classic source revision
- [ ] Identify the Android host/loader that can be modified for application ID `com.roblox.revenge`
- [ ] Build a separate Android package without replacing the user's existing Discord installation
- [ ] Prototype a supported Roblox launch/display experience
- [ ] Verify behavior on a physical Android device

## Scope and limitations

This is an experimental project. Embedded, in-process rendering of Roblox's native Android game engine is not established. Do not claim that Roblox is running inside Discord until a working implementation has been built and tested.

Never commit Discord or Roblox account credentials, session tokens, signing keys, or other secrets.
