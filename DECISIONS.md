# Learvia — Engineering Decisions (Slice 1)

Product owner instruction (2026-11-07): proceed without further questions; engineering decisions
below were made by the assistant under that delegation. Each one is reversible and should be
revisited when the matching TBD in FEATURE_SPEC.md is formally closed.

| Area | Decision taken | Related TBD |
|---|---|---|
| Hierarchy | Subject is **optional**: `chapters.subjectId` is nullable, so a Chapter can sit under a Subject or directly under a Course. | TBD-01 |
| Delete | Deleting any node cascades **Learvia metadata only** after a confirm dialog. Video files are never touched. | TBD-19 |
| Media | One video per class (`mediaUri`), stored as a SAF URI with a persisted read grant. No copying. | TBD-17 |
| Missing media | Player checks the URI can be opened; otherwise shows "Video unavailable" and points to Settings → Management → Re-link. | TBD-14 |
| Resume | Position saved every 5 s while playing and on exit; never seeks past the real duration. Completion rule not implemented. | TBD-03 |
| minSdk / targetSdk | 26 / 35 (compileSdk 35). | TBD-21/22 |
| Language | English + Bangla string resources; follows system language. | TBD-23 |
| Tablet | Not designed yet; phone-first. | TBD-24 |
| Theme | Material 3, dynamic color on Android 12+, brand blue fallback. | UI tokens |
| Backup | `allowBackup=false` until BK-001 is designed. | TBD-09 |

## Slice 2 additions
- Adaptive launcher icon (foreground cropped and padded from the supplied PNG to fit the safe zone; background `#0A2FC0`).
- Thumbnails (CM-003) for Course/Subject/Chapter/Class via image picker; stored as persisted URI references (Coil for display).
- Player: double-tap left/right third seeks ±10 s (placeholder default, TBD-12); built-in Media3 controls
  stay available as the non-gesture alternative and already include the playback-speed menu.

## Slice 3 assumptions (all reversible; owner delegated decisions)
| Area | Assumption |
|---|---|
| Progress (TBD-03) | furthest position / duration; completed at 90%, never un-completed |
| Recommendation (TBD-04) | next unfinished class after the last watched, in course order; no AI |
| Revision points (TBD-15) | every bookmark / timestamp note / marker, in video order |
| Transcript source (TBD-07) | user-imported .srt/.vtt only; search is silent (no error) when none exist |
| Active study (TBD-20) | video playing in foreground + finished focus sessions; background not counted |
| Duplicates (TBD-16) | same file name and same size |
| Backup (TBD-09) | metadata + references as JSON; restore replaces everything after confirmation |
| Reminders (TBD-06) | one daily WorkManager reminder (inexact), hour chosen from presets |
| Focus presets | 25/5 and 50/10 (placeholders) |
| Profiles (TBD-12) | Study 1x, Revision 1.5x, Relaxed 0.75x, Custom 1.25x (placeholders) |
| Distraction-free | player-only: hides back button and menu, keeps one dim exit button |
| Brightness gesture | changes the app window brightness only, not system brightness |
| DB | version 2, destructive migration while pre-release |

## Not in this slice (not started, not faked)
Thumbnails, drag-and-drop, collections, notes, bookmarks, transcript, search, planner, focus,
statistics, PiP/background audio, gestures, subtitles, casting, comparison, AI assistant, App Lock.
No quiz, AI API, local AI model, or multi-provider code exists.

## Honest status
Written without network access, so it has **not been compiled or run**. Library versions are
ones I know (AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.10.01, Room 2.6.1, Media3 1.4.1) and
may be outdated; bump them if Android Studio suggests. Open the `Learvia` folder in Android
Studio and let it generate the Gradle wrapper, then Sync and Run.

## Icon
`design/icon-source.png` is the supplied icon; launcher resources are done (see Slice 2). A monochrome
(themed icon) layer is not made yet.
