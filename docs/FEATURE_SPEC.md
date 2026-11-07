# FEATURE SPECIFICATION
## Approved Product Scope, Behavior, Rules, Dependencies, and Open Decisions

**Status vocabulary**
- **APPROVED:** accepted product requirement.
- **PROPOSED DIRECTION:** intended approach, subject to technical/design validation.
- **TBD:** the product owner has not made the decision. Do not invent it.
- **EXCLUDED:** rejected; do not implement unless explicitly re-approved.

This document is the detailed feature source of truth. Screen presentation is described in `UI_UX_SPEC.md`; project principles and boundaries are in `PROJECT_BRIEF.md`.

---

## 1. Content and Course Management

### CM-001 — Multi-course library
**Status:** APPROVED  
**Purpose:** Let the learner maintain multiple independent courses.

**Behavior**
- User can create and maintain multiple courses.
- Each course can have a title, thumbnail, and details/description.
- Course count is user-controlled; do not impose a small fixed product limit without a technical reason.
- Course data is local-first.

**Interactions:** Create, open, edit, organize, and delete actions belong in the Management area; the Learning Interface displays the resulting course library.

**Rules / edge cases**
- Empty library must show a helpful empty state and an entry point to create/import a course.
- Deleting a course must not silently delete original media files. Confirm destructive actions and define metadata/child handling.
- Exact delete/archive semantics: TBD.

### CM-002 — Hierarchical learning structure
**Status:** APPROVED for Course → Subject → Chapter → Class as the canonical structure; optional Subject level is TBD.

**Behavior**
- Course may contain user-defined subjects; subjects contain chapters; chapters contain classes.
- User controls the number of subjects, chapters, and classes.
- Each level can carry a thumbnail and details.
- Example: HSC Preparation → Physics → chapter → class → local video.

**TBD:** Whether a course may also contain Chapters directly without a Subject. Do not assume Subject is optional or mandatory until clarified (`TBD-01`).

**Rules**
- Preserve parent-child relationships and ordering.
- Moving a child between parents must update its hierarchy without losing associated learning data.
- Deletion behavior for non-empty parent nodes requires confirmation and a defined cascade/archive policy.

### CM-003 — Content metadata and thumbnails
**Status:** APPROVED at a high level; exact fields TBD.

**Behavior**
- Course, Subject, Chapter, and Class each support a thumbnail and details.
- User can choose or change thumbnails during management.
- Keep image selection and crop/fit behavior consistent.

**TBD:** Exact fields beyond title, thumbnail, and general details; image formats, crop ratios, and fallback assets.

### CM-004 — Local media assignment
**Status:** APPROVED

**Purpose:** Connect a class to a video stored on the device.

**Workflow**
1. In Management, user opens/creates a Class.
2. User selects a local video using Android's system file picker / SAF.
3. App requests and retains access where Android permits.
4. App stores a URI/reference and metadata rather than duplicating large video files by default.
5. Assigned video becomes available from that class in the Learning Interface.

**Business rules**
- Only explicitly assigned files are shown as course learning content.
- Do not scan and expose the entire device video library as the user's course library.
- Support internal storage and SD card where Android provider/device access permits.
- Direct SD-card playback is an approved player reliability requirement.
- A class may have no assigned video; its empty/unassigned state must be clear.
- Whether multiple videos can be assigned to one class is TBD.

**Edge cases**
- Permission revoked, provider unavailable, file moved/renamed/deleted, SD card removed, URI invalid, unsupported codec/container, corrupted file, or file inaccessible after reboot.
- Show a recoverable missing-media state and provide a re-link workflow; exact recovery UI is TBD-14.
- Never delete or move the user's original media without explicit action and confirmation.

### CM-005 — Custom Course Collections
**Status:** APPROVED

**Purpose:** Group courses into user-defined collections beyond the primary hierarchy.

**Expected behavior:** User can create named collections and place courses into them; a course may potentially belong to more than one collection, but this membership rule is TBD. Collections are organizational views, not a replacement for Course/Subject/Chapter/Class.

### CM-006 — Drag & Drop Course Organizer
**Status:** APPROVED

**Purpose:** Reorder courses and relevant content visually.

**Expected behavior:** Drag-and-drop changes persist and are reflected consistently in Learning and Management views. Exact supported levels, multi-select behavior, and cross-parent move confirmation are TBD.

**Dependencies:** CM-001, CM-002, persistent sort/order fields.

### CM-007 — Smart Media Library
**Status:** APPROVED

**Purpose:** Help the user understand and maintain assigned media.

**Approved scope includes**
- Media assignment and organization.
- Identification of missing/unavailable assigned files.
- Duplicate-file awareness.
- Media metadata visibility/management.

**TBD:** Duplicate definition (same URI, file hash, filename/size, or perceptual match), scan timing, whether the app can inspect all selected files only, and user actions for duplicates. Respect Android permissions; do not broaden access beyond user-selected media without consent.

### CM-008 — Customizable Course Appearance
**Status:** APPROVED

**Purpose:** Let users personalize how courses are presented.

**Approved scope:** Custom colors, covers, icons, and layouts.

**TBD:** Theme presets, per-course vs global appearance precedence, accessibility contrast rules, and exact layout choices. Customization must not undermine readability or core navigation.

---

## 2. Learning and Progress

### LP-001 — Continue Watching / Resume Playback
**Status:** APPROVED

- Save the last playback position for each class/video.
- Resume from the saved position when the user returns, where appropriate.
- Distinguish never-started, in-progress, and completed states once completion rules are defined.
- Persist progress across app restarts.
- Handle file replacement or changed media duration carefully; do not blindly seek beyond the new duration.

**TBD:** Save frequency, completion threshold, and behavior after the user manually restarts a lesson.

### LP-002 — Course and Lesson Progress Tracking
**Status:** APPROVED

- Track viewing/progress at lesson level and aggregate to chapter, subject, and course.
- Progress should be consistent across course cards, lesson lists, planner, recommendations, and statistics.
- **TBD:** Whether progress is based on watched duration, furthest position, unique watched segments, manual completion, or a combination; aggregation formula; completion threshold; treatment of seeking/skipping and repeated viewing.

### LP-003 — Bookmarks and Timestamp Bookmarks
**Status:** APPROVED

- User can bookmark a point in a video.
- Timestamp bookmarks can have a name/comment.
- Selecting a bookmark seeks to its timestamp.
- Bookmarks should be associated with the correct class/video.
- Show markers on the timeline and in a revision/bookmark list.
- Edge cases: duplicate timestamps, deleted video, changed duration, and bookmark ordering require graceful handling.

### LP-004 — Notes and Timestamp Notes
**Status:** APPROVED

- User can create notes while studying.
- Timestamp notes are linked to a playback time; selecting one seeks to that time.
- Notes should be searchable through Global Search.
- Exact rich-text/formatting, attachments, autosave, and export behavior are TBD.
- Notes must remain associated with the relevant learning item even if the user reorders its parent hierarchy.

### LP-005 — User-created Video Chapter Markers
**Status:** APPROVED

- User can mark sections within a video with a timestamp and label.
- Markers help navigate long lessons and support revision.
- Timeline markers and a section list should expose these points.
- Do not confuse these user-created markers with the app's Course → Chapter hierarchy.

### LP-006 — Playback History and Viewing Statistics
**Status:** APPROVED

- Maintain playback history and learning activity statistics.
- Statistics should support understanding what was studied and when.
- Exact metrics, retention period, and privacy controls are TBD.
- Avoid counting background playback as active study without a defined rule.

### LP-007 — Watched/Unwatched Timeline Indicators
**Status:** APPROVED

- Show visual indication of watched and unwatched portions where technically and logically supported.
- Coordinate with progress tracking and timeline markers.
- **TBD:** Whether tracking records continuous watched segments or only a furthest position. Do not imply complete segment-level accuracy until that logic is decided.

### LP-008 — Revision List / Smart Revision Points
**Status:** APPROVED

- Provide a list of marked sections useful for revision.
- Smart revision points may use bookmarks, notes, chapter markers, or related learning signals.
- Exact ranking, thresholds, and algorithm are TBD-15.
- Do not invent AI-generated revision points or quiz questions.

### LP-009 — Screenshot / Frame Capture
**Status:** APPROVED

- Capture a frame from the video where supported.
- User may use/share the captured frame with the AI Study Assistant if the eventual integration permits it.
- Exact save location, permissions, image naming, and privacy behavior are TBD.
- Respect protected/DRM content restrictions and platform limitations; do not bypass them.

### LP-010 — Study Statistics
**Status:** APPROVED

- Provide an overview of learning activity, time, progress, and consistency.
- Statistics are intended to help the learner understand their study habits.
- Exact dashboard metrics, date ranges, and treatment of idle/background time are TBD.

### LP-011 — Learning Heatmap
**Status:** APPROVED

- Visualize study activity over time as a heatmap.
- Exact granularity (day/week/month), activity threshold, timezone behavior, and colors/accessibility are TBD.
- Heatmap should derive from defined activity events rather than fabricated estimates.

---

## 3. Study Planning and Productivity

### SP-001 — Personal Study Planner
**Status:** APPROVED

**Purpose:** Let the learner plan study activity against courses/classes and personal time.

**Expected behavior**
- User can create and view a personal study plan.
- Planner should connect to course/lesson context where possible.
- Progress/completion should reflect the actual learning data only after LP-002 rules are defined.

**TBD:** Calendar vs task list vs both, manual/automatic scheduling, recurring sessions, duration estimates, overdue handling, rescheduling, conflicts, and calendar integration. Do not assume external calendar access.

### SP-002 — Focus Session / Pomodoro
**Status:** APPROVED

**Purpose:** Provide a focused study timer/work session.

**Expected behavior**
- User can start a focus session and see its state.
- Support a Pomodoro-style workflow.
- Session data may feed study statistics and heatmap, subject to a defined rule.

**TBD:** Work/break durations, long-break cadence, custom presets, pause/skip behavior, background operation, notification behavior, and whether a focus session must be linked to a course/class.

### SP-003 — Smart Study Reminder
**Status:** APPROVED

**Purpose:** Remind the user about planned or relevant study activity.

**TBD:** Trigger logic, schedule, quiet hours, notification permissions, snooze/repeat, missed-reminder behavior, and whether recommendations can create reminders. Respect Android notification and exact-alarm restrictions.

### SP-004 — Smart Learning Recommendation
**Status:** APPROVED

**Purpose:** Suggest a useful next learning action based on the user's learning context.

**TBD:** Inputs (unfinished lessons, planner, progress, recent activity, course order, bookmarks), recommendation ranking, explanation, dismissal, feedback, and whether suggestions are rule-based or another method. Do not silently assume AI/API usage; the user rejected AI API and local AI model.

### SP-005 — Smart Playback Profiles
**Status:** APPROVED

**Purpose:** Provide named playback behavior presets such as Study, Revision, Relaxed, and Custom.

**Expected behavior**
- User can choose a profile.
- Profiles may configure playback speed and relevant player settings.
- Earlier selection also includes per-course playback profile assignment (e.g. different speeds by course).

**TBD:** Exact settings in each profile, default values, whether profile is global/per-course/per-video, precedence when a user manually changes a setting, and custom profile limits.

### SP-006 — Smart Auto-Play Queue
**Status:** APPROVED

**Purpose:** Automatically prepare or continue a sequence of learning videos.

**Expected behavior:** Queue may connect current class to next/previous classes and user-selected items. The player already includes playback queue, next/previous class, playlist queue, repeat/shuffle queue as approved player capabilities.

**TBD:** Queue-generation rules, course-order behavior, whether completed lessons are skipped, manual override, cross-course behavior, and how Smart Auto-Play Queue differs from a manually curated queue.

### SP-007 — Global Search Engine
**Status:** APPROVED

**Search scope**
- Courses
- Subjects
- Chapters
- Classes
- Notes
- Bookmarks
- Transcripts

**Expected behavior:** Search results should navigate to the relevant entity or timestamp where possible. Respect content availability and permissions.

**TBD:** Filters, ranking, typo tolerance, offline indexing, transcript indexing lifecycle, and whether search includes file metadata.

### SP-008 — Storage Intelligence
**Status:** APPROVED

**Purpose:** Help the user understand local storage used by learning content.

**Approved scope**
- Internal storage and SD-card usage visibility.
- Per-course storage information.

**TBD:** Exact calculation method, cached vs source file size, duplicate accounting, stale/missing file display, refresh frequency, and whether cleanup suggestions are allowed. The app must not delete user files automatically.

---

## 4. Advanced Video Player

### VP-000 — Overall player concept
**Status:** APPROVED

A hybrid premium player combining:
- YouTube-like clean, intuitive clarity.
- PLAYit-like advanced controls and gestures.
- Custom learning-oriented tools.

Visual requirement: premium, clean, minimalist, stock-Android-inspired. Avoid clutter. Keep advanced controls in separate menus/panels. Exact layout is TBD; see `UI_UX_SPEC.md`.

### VP-001 — Advanced Playback Controls
**Status:** APPROVED

Features:
- Playback speed control (example range discussed: 0.25x to 3x or more; exact range TBD).
- Custom seek interval.
- Double-tap seek.
- Frame-by-frame forward/backward.
- A-B repeat.
- Whole-video loop.
- Sleep timer.
- Auto replay.
- Seekbar thumbnail preview.
- Playback queue.
- Resume playback.
- Next/previous class.
- Playlist queue.
- Repeat/shuffle queue.

Rules and edge cases:
- A-B repeat requires valid A and B positions and B > A; reset or explain invalid selections.
- Frame stepping depends on decoder/media support.
- Seekbar thumbnails require an available thumbnail source or generation strategy; method TBD.
- Sleep timer must behave safely across backgrounding and app lifecycle.
- Auto replay and whole-video loop must have clear, non-conflicting states.
- Queue behavior must preserve user order and handle unavailable media.

### VP-002 — Gesture and Display Controls
**Status:** APPROVED

Features:
- Brightness gesture.
- Volume gesture.
- Pinch to zoom.
- Horizontal swipe to seek.
- Press-and-hold temporary 2x speed.
- Optional three-finger gestures.
- Customizable gesture zones and sensitivity; enable/disable options.
- Fit, fill, crop, and stretch aspect modes.
- Rotation control.
- Orientation lock.

TBD:
- Gesture zones, sensitivity ranges, exact defaults, three-finger gesture mapping, and conflict resolution with system navigation/accessibility gestures.
- Whether brightness changes system brightness or player-only brightness.
- Rotation behavior across device/app orientation settings.

### VP-003 — Learning Tools inside Player
**Status:** APPROVED

Features:
- Smart bookmarks / timestamp bookmarks with names/comments.
- Timestamp notes; tapping a note seeks to its time.
- User-created chapter markers/sections inside a video.
- Transcript display.
- Transcript search.
- Tap transcript sentence to seek.
- Smart revision points based on bookmarks/notes or similar.
- Playback history and viewing statistics.
- Screenshot/frame capture, potentially shareable to AI Study Assistant.
- Watched/unwatched timeline indicators.
- Timeline markers for bookmarks, notes, and chapters.
- Revision list of marked sections.

Dependencies:
- LP-003 through LP-009.
- Transcript data and timestamp alignment for transcript seeking.
- AI frame sharing depends on the AI module being feasible and approved for that data flow.

### VP-004 — Transcript System
**Status:** APPROVED

Features:
- Display transcript alongside/within the player.
- Search transcript text.
- Select a transcript sentence/segment to jump to its timestamp.
- Transcript is also indexed by Global Search.

TBD:
- Transcript source: user import, manually entered, external file, speech-to-text, or another method.
- Supported file formats, timestamp schema, languages, editing, synchronization, and generation dependencies.
- Do not assume AI transcription or an API/local model; the user has rejected AI API and local AI models.

### VP-005 — Audio and Subtitle
**Status:** APPROVED

Features:
- Multiple audio track selection.
- External subtitle support (example: SRT).
- Subtitle track selection.
- Subtitle font size, color, background, and position customization.
- Subtitle delay adjustment.
- Audio delay adjustment.
- Audio boost (device/player support dependent).
- Audio equalizer (e.g. Bass/Treble; audio pipeline/device dependent).

TBD:
- Supported formats/codecs, external subtitle association/import UX, persistence scope of preferences, equalizer bands/presets, audio boost limits, and device-specific support.
- If a feature is unsupported by a file/device, show a clear unavailable state rather than pretending it works.

### VP-006 — Multitasking and System Playback
**Status:** APPROVED

Features:
- Picture-in-Picture.
- Background audio playback.
- Notification playback controls.
- Lock-screen playback controls.
- Split-screen support.
- Floating mini player.
- Continue using other apps while audio plays.
- Headphone disconnect auto-pause.
- Bluetooth headset controls.

Dependencies / edge cases:
- Android version, permissions, lifecycle, audio focus, PiP support, and device/OEM behavior.
- Define whether video transitions to audio-only on backgrounding or whether playback pauses; exact policy TBD.
- Handle audio focus loss, phone calls, Bluetooth disconnect/reconnect, and process recreation.

### VP-007 — Advanced Player Options
**Status:** APPROVED

Features:
- Video comparison (two videos side-by-side).
- Cast to TV (local video casting).
- Player theme/layout customization.
- Customizable control layout, button size, and seekbar style.
- Player profiles: Study, Revision, Relaxed, Custom.
- Per-course playback profile assignment.

TBD:
- Video comparison synchronization, audio focus, device performance, and screen layout.
- Casting protocols/devices, local-network discovery, DRM/codec compatibility, and whether casting can preserve local-file access.
- Exact customization limits and profile precedence.

### VP-008 — Performance and Reliability
**Status:** APPROVED

Requirements:
- Smooth seeking and fast loading.
- Hardware-accelerated decoding where supported.
- Multiple video format support.
- Unsupported/corrupt file error handling.
- Audio-video synchronization.
- Portrait and landscape video support.
- Playback buffer management.
- Battery and memory efficiency.
- SD-card direct playback.
- Media-reference recovery if a file is moved/deleted.
- High-resolution playback.

Rules:
- Actual format support depends on Android platform, Media3/decoder, device hardware, and file encoding.
- Do not promise universal codec support.
- Provide meaningful errors and recovery options.
- Avoid loading full video files into memory.
- Test low-memory devices and large/high-resolution files.

---

## 5. AI Study Assistant

### AI-001 — Native AI Study Assistant Chat
**Status:** USER-DESIRED / TECHNICAL FEASIBILITY NOT VERIFIED

**User's intended workflow**
1. User enters a question in the app's own native chat interface.
2. App submits it to the selected AI provider's consumer website automatically.
3. Provider processes it using the user's own consumer account/session and normal consumer usage allowance.
4. App retrieves the answer and renders it in the app's native chat UI.
5. The provider receives preconfigured instructions to behave as a Personal Study Assistant.

**Provider decision**
- Gemini is the preliminary single-provider candidate recommended in discussion.
- The user wants one best provider, not multiple providers.
- Provider choice is not a verified technical decision until feasibility and terms are checked.

**Hard constraints**
- No API key.
- No API integration.
- No local AI model.
- Do not silently switch to API or another provider if website automation is not possible.
- Do not bypass login security, CAPTCHA, anti-automation protections, rate limits, or terms.
- A consumer account's normal usage allowance does not automatically authorize third-party automated access.

**Technical considerations**
- Chrome Custom Tabs generally do not provide app access to website DOM/JavaScript or reliable hidden message automation.
- Embedded WebView may permit app-page communication through a JavaScript bridge, but Google sign-in may be blocked in embedded WebViews and website changes can break automation.
- Any browser automation must be checked against provider policies and technical protections.
- The native chat must not be represented as working until a permitted, stable request/response path is proven.

**TBD / gate before implementation**
- Whether Gemini consumer website permits this interaction.
- Authentication and session persistence model.
- Whether automated input/response extraction is technically and contractually permitted.
- How to handle CAPTCHA, MFA, session expiry, rate limits, website redesign, and failures.
- Whether context from current course/chapter/class/notes is sent; no such data flow is approved yet.
- Conversation storage, deletion, privacy, and data-sharing disclosure.
- If infeasible, stop and present options to the user; do not choose a fallback without consent.

### AI-002 — Personal Study Assistant Instructions
**Status:** APPROVED CONCEPT; final prompt TBD

The external assistant should be instructed to act as a Personal Study Assistant. Exact tutor behavior, supported subjects, language, tone, and context injection need a separate specification. Do not assume it may access all app data.

---

## 6. Backup and Restore

### BK-001 — Backup and Restore
**Status:** APPROVED

**Purpose:** Protect the user's course structure and learning records.

**TBD**
- Whether backup includes only database/settings or also media files.
- Destination: local folder, SD card, or other explicitly approved location.
- Encryption/password protection.
- Versioning, restore conflict handling, partial restore, and compatibility across app versions.
- Whether original media is copied or only references are backed up.

**Important:** Since media is user-owned and may be large, do not assume the app should duplicate or upload it. Clearly distinguish metadata backup from media backup.

---

## 7. Rejected / Excluded Requirements

### EX-001 — Self Assessment / Quiz
**Status:** REJECTED / OUT OF SCOPE

Reason recorded in discussion: no dependable question source was identified, and AI/API-based quiz generation would introduce usage/limit complications. Do not implement a quiz bank, automatic quiz generation, or self-assessment feature as part of approved scope.

### EX-002 — AI API / API Key
**Status:** REJECTED

The user does not want API keys or AI API integration for the Study Assistant.

### EX-003 — Local AI Model
**Status:** REJECTED

Do not add an on-device AI model.

### EX-004 — Multi-provider AI
**Status:** REJECTED

The user wants one best provider, not a provider selector or multi-provider framework.

---

## 8. Cross-Feature Dependency Map

- CM-001 → CM-002 → CM-003 / CM-004: course hierarchy and metadata must exist before assigning class media.
- CM-004 → VP-001 / VP-008: player depends on a valid local media reference and reliable file access.
- LP-001 / LP-002 → course progress UI, Smart Recommendation, Planner, Statistics, Heatmap.
- LP-003 / LP-004 / LP-005 → VP-003 timeline markers, Revision List, Global Search.
- VP-004 → Global Search transcript scope and transcript-to-timestamp navigation.
- SP-005 → VP-001 and VP-007 playback configuration.
- SP-006 → VP-001 queue and next/previous class behavior.
- SP-003 → Android notification permission and scheduling constraints.
- SP-002 → LP-006 / LP-010 / LP-011 only after active-study counting rules are defined.
- SP-008 → CM-004 / CM-007 file metadata and storage access.
- BK-001 → Room data model, settings, URI references, and media recovery rules.
- AI-001 ↔ AI-002: assistant behavior depends on a permitted and reliable provider connection.
- AI frame sharing (VP-003) depends on AI-001 feasibility and explicit user consent/data-flow design.

## 9. Acceptance and Test Scenario Baseline

The following are minimum validation scenarios; detailed numeric expected results remain TBD where business rules are undecided.

| ID | Scenario | Expected result |
|---|---|---|
| AT-001 | Create multiple courses | Each course is independently visible and manageable. |
| AT-002 | Build Course → Subject → Chapter → Class | Parent-child structure and ordering persist. |
| AT-003 | Assign a local video to a class | Only that assigned video appears as class content. |
| AT-004 | Open unassigned class | Clear empty/unassigned state; no unrelated device videos shown. |
| AT-005 | Remove SD card or revoke file access | App reports unavailable media and offers recovery; no crash. |
| AT-006 | Play, exit, reopen a lesson | Resume behavior follows saved position rules. |
| AT-007 | Add timestamp bookmark/note | Entry is saved and seeks to the correct timestamp. |
| AT-008 | Add video chapter marker | Marker appears in section list/timeline and navigates correctly. |
| AT-009 | Search a note/bookmark/transcript | Relevant result opens the item or timestamp. |
| AT-010 | Change playback profile | Applicable playback settings update according to precedence rules. |
| AT-011 | Start focus session | Timer state remains coherent through supported app lifecycle changes. |
| AT-012 | Use planner/reminder | Scheduled item/reminder follows configured time and permission rules. |
| AT-013 | View heatmap/statistics | Values derive from recorded events and defined activity rules. |
| AT-014 | Use subtitle/audio controls | Supported tracks/settings work; unsupported cases are clearly explained. |
| AT-015 | Enter PiP/background playback | Behavior matches Android/device support and audio-focus policy. |
| AT-016 | Use A-B repeat/loop/sleep timer | Controls behave predictably and can be stopped/reset. |
| AT-017 | Backup then restore | Included data restores without corrupting unrelated data. |
| AT-018 | Attempt AI connection | No API/local model fallback; only a permitted verified route is used. If unavailable, explain and stop. |
| AT-019 | Attempt quiz feature | No quiz/self-assessment appears in approved build. |
| AT-020 | Delete course with child content | App confirms and follows the explicitly approved deletion policy; never deletes source media silently. |

## 10. Clarification Register

| ID | Question | Why it matters |
|---|---|---|
| TBD-01 | Can a Course contain Chapters directly, or is Subject mandatory? | Data model and navigation. |
| TBD-02 | Exact metadata fields at each hierarchy level? | Forms, database schema, search. |
| TBD-03 | Lesson/course progress and completion formula? | Resume, progress, recommendations, heatmap. |
| TBD-04 | Recommendation inputs and ranking? | Recommendation engine and explainability. |
| TBD-05 | Planner structure and scheduling rules? | Calendar/task model and rescheduling. |
| TBD-06 | Reminder cadence, quiet hours, snooze? | Notifications and WorkManager/alarm strategy. |
| TBD-07 | Transcript source, format, and editing? | Transcript import/generation and indexing. |
| TBD-08 | Is consumer-site automation permitted and reliable? | AI module go/no-go. |
| TBD-09 | Backup scope, location, encryption, media inclusion? | Data safety and storage. |
| TBD-10 | App lock and local privacy/security requirements? | Security architecture. |
| TBD-11 | Final navigation and screen hierarchy? | UI architecture. |
| TBD-12 | Exact player control ranges/defaults? | Player settings and tests. |
| TBD-13 | Video comparison/casting device and protocol requirements? | Technical feasibility. |
| TBD-14 | Missing/moved file recovery steps? | Media reference lifecycle. |
| TBD-15 | Smart revision point logic? | Revision prioritization. |
| TBD-16 | Duplicate detection and storage calculation rules? | Media library accuracy. |
| TBD-17 | Multiple videos per class? | Content model and player entry behavior. |
| TBD-18 | Collection membership and sorting rules? | Course organization. |
| TBD-19 | Deletion/archive/cascade behavior? | Data integrity and user safety. |
| TBD-20 | Exact statistics and active-study counting rules? | Analytics validity. |

Claude must maintain this register, resolve questions with the user, and update cross-referenced documents when decisions are made.
