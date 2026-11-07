# Learvia — Requirement Coverage (honest status, after Slice 3)

Legend: DONE = implemented (assumptions noted) · PARTIAL · NOT STARTED.
**Nothing has been compiled or run yet.** Source: docs/FEATURE_SPEC.md (original, not yet revised).

| ID | Feature | Status | Note |
|---|---|---|---|
| CM-001 | Multi-course library | PARTIAL | create/rename/delete done; no description editing UI |
| CM-002 | Hierarchy | DONE | Subject optional (assumed) |
| CM-003 | Metadata & thumbnails | PARTIAL | thumbnails done; description field has no edit UI |
| CM-004 | Local media assignment (SAF) | DONE | persisted URI, missing-media state, re-link |
| CM-005 | Custom collections | DONE | a course can be in several collections; filter chips on home |
| CM-006 | Drag & drop organizer | PARTIAL | move up/down for every level; drag gesture NOT built |
| CM-007 | Smart media library | DONE | missing files + duplicates (same name and size) |
| CM-008 | Course appearance | PARTIAL | accent color + thumbnail; custom icons/layouts not built |
| LP-001 | Resume / Continue watching | DONE | home list, resume, clamped to duration |
| LP-002 | Progress tracking | DONE | furthest position, 90% = completed (assumed), aggregated per chapter/course |
| LP-003 | Bookmarks | DONE | list + tap to seek; no seekbar markers |
| LP-004 | Notes / timestamp notes | DONE | searchable; plain text only |
| LP-005 | Video chapter markers | DONE | list + tap to seek; no seekbar markers |
| LP-006 | Playback history | DONE | session log (foreground playing time only) |
| LP-007 | Watched/unwatched timeline | NOT STARTED | seekbar overlay not built |
| LP-008 | Revision list | DONE | rule-based: all marked points in video order |
| LP-009 | Frame capture | DONE | saved to app folder; not shared with any AI |
| LP-010 | Study statistics | DONE | watch time, focus time, sessions, completed, streak |
| LP-011 | Heatmap | DONE | 17 weeks, from recorded events |
| LP-012 | Distraction-free mode | DONE | player-only scope (assumed) |
| SP-001 | Planner | PARTIAL | dated tasks; not linked to classes, no recurrence |
| SP-002 | Focus / Pomodoro | PARTIAL | 25/5 and 50/10; runs only while app process lives |
| SP-003 | Smart reminder | PARTIAL | one daily reminder at a chosen hour (WorkManager) |
| SP-004 | Recommendation | DONE | rule-based next unfinished class |
| SP-005 | Playback profiles | PARTIAL | per-course Study/Revision/Relaxed/Custom speed; other settings not covered |
| SP-006 | Auto-play queue | PARTIAL | next/previous class + auto-play next; no shuffle/playlist/cross-course queue |
| SP-007 | Global search | DONE | all 7 scopes; no results for transcripts when none imported |
| SP-008 | Storage intelligence | DONE | internal vs SD detected via provider id; other providers grouped |
| VP-000 | Player concept | PARTIAL | layered menu + panels; not visually tuned |
| VP-001 | Advanced playback | PARTIAL | speed (built-in), seek interval, double-tap, frame step (approx.), A-B, loop, sleep timer, next/prev; NO seekbar thumbnails, shuffle/playlist queue |
| VP-002 | Gestures & display | PARTIAL | brightness, volume, swipe seek, hold 2x, aspect modes, rotate; NO pinch zoom, three-finger, zone/sensitivity settings |
| VP-003 | Learning tools in player | PARTIAL | everything via menu/panels except seekbar markers |
| VP-004 | Transcript | DONE | user-imported .srt/.vtt only; tap to seek, search |
| VP-005 | Audio & subtitles | NOT STARTED | only whatever Media3 built-in settings menu offers (unverified); no external subtitle, delay, styling, boost, equalizer |
| VP-006 | Multitasking | PARTIAL | PiP, headphone-unplug pause, audio focus; NO background audio, notification/lock-screen controls, floating mini player |
| VP-007 | Comparison, casting, customization | NOT STARTED | feasibility NOT VERIFIED |
| VP-008 | Performance & reliability | PARTIAL | ExoPlayer defaults, error/missing states; not device-tested |
| AI-001/002 | AI Study Assistant | EXCLUDED THIS ROUND | per owner instruction; nothing built |
| BK-001 | Backup & restore | DONE | metadata + references only (no media files) |
| SEC-001 | App Lock | NOT STARTED | undecided in original spec |
| EX-001..004 | Excluded items | CONFIRMED ABSENT | no quiz, AI API, local model, multi-provider |

Counts (AI excluded): see table. Not finished: LP-007, VP-005, VP-007 not started; 14 entries partial.

## Project infrastructure gaps
- No Gradle wrapper (CI uses setup-gradle); no unit/instrumented tests (CLAUDE.md section 9).
- Room schema is rebuilt destructively on version change (pre-release); explicit migrations needed before release.
- Documentation review fixes were NOT applied to docs/ (original files).
