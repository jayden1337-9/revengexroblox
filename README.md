# IconForge

IconForge is being rebuilt on top of **Folio Launcher**, an iPhone-inspired Android launcher, rather than recreating its UI from scratch.

## Upstream foundation

- Project: [McCal-Codes/Folio](https://github.com/McCal-Codes/folio)
- Pinned source: Folio v0.6.9 (eea5f4aa1bd009e583f6ba06e62281f13b8b30d2)
- License: MIT (see the upstream [LICENSE](https://github.com/McCal-Codes/folio/blob/main/LICENSE))
- Original credits and third-party notices remain part of the upstream source.
- The current preview build changes the launcher app label to **IconForge Preview**. The app's internal Folio branding and attribution are retained while the rebrand is developed.

## Device compatibility

Folio requires Android 12 or newer (API 31+). The target device for testing is an onn.11 pro running Android 14 (API 34), which meets that minimum. The upstream developer primarily tests on a Galaxy Z Fold, so phone-specific layout and behavior still need real-device testing.

## Build

Push to main or run **Build IconForge (Folio base)** manually in GitHub Actions. The workflow checks out the pinned Folio source, builds its optimized fast variant, and uploads the **IconForge-preview-apk** artifact.

The preview uses Folio's own application package with a debug signing key. It can be installed alongside the original Folio release, but it is not a signed production release.

## Next steps

1. Install and test the preview on the target device.
2. Fix phone-layout issues and verify launcher/default-Home behavior.
3. Rebrand the in-app UI and app identity without removing upstream credits or license notices.
4. Add IconForge-specific features after the inherited launcher is stable.
