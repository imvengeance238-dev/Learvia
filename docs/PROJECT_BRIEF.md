# PROJECT BRIEF
## Personal Local Learning & Study Management App

**Document status:** Product knowledge transfer baseline  
**Audience:** Product owner, Claude, Android developers, QA, UI/UX designers  
**Implementation status:** No implementation authorized by this package alone; planning and clarification come first.  
**Primary language/platform:** Kotlin, native Android

---

## 1. Product Overview

The product is a premium, personal learning application for Android. It lets a user organize their own courses and learning materials, play locally stored lesson videos with advanced learning-oriented controls, track study activity, plan sessions, and use productivity tools in one cohesive app.

It is not intended to be a conventional cloud-hosted course marketplace or a public online course platform. The user's course content is personally curated and primarily stored on the device's internal storage or SD card. The app provides the structure, player, learning workflow, and management tools around that content.

The intended experience combines:
- A clean, premium, stock-Android-inspired learning interface.
- Flexible course and lesson organization.
- A powerful video player designed for study, not merely entertainment.
- Study planning, focus, progress, and productivity tools.
- A discreet content-management area for importing and organizing personal media.
- A proposed external AI Study Assistant, subject to a strict feasibility and policy check.

## 2. Problem the App Solves

A learner may have many downloaded or personally collected lesson videos, but ordinary file managers and media players do not provide a coherent course structure or learning workflow. Generic video players also lack the combined course progress, lesson notes, timestamp bookmarks, transcript navigation, revision markers, study planning, and learning statistics needed for structured study.

The app aims to solve this by connecting:
1. **Content organization** — courses, subjects, chapters, classes, thumbnails, metadata, and local media assignment.
2. **Learning experience** — lesson playback with study-specific tools and reliable resume behavior.
3. **Learning management** — progress, planner, reminders, focus sessions, recommendations, and statistics.
4. **Content administration** — controlled import, assignment, ordering, and maintenance of the user's own media.

## 3. Target Users

### Primary target user
An individual learner who:
- Studies from locally stored lesson videos.
- Wants to organize material into courses and nested learning units.
- May maintain multiple courses and subjects at once.
- Wants to resume lessons, take timestamped notes, bookmark important moments, and revise efficiently.
- Wants personal study planning, focus support, and visibility into learning consistency.
- Prefers a polished native Android experience and local control of personal learning content.

### Example use case
A learner creates an “HSC Preparation” course, adds Physics, Chemistry, and Mathematics subjects, creates chapters under each subject, adds classes under chapters, and assigns a local video to each class. The exact hierarchy's optionality is not fully settled; see `TBD-01`.

## 4. Product Vision

Create a premium personal learning environment that makes locally stored educational content feel like a structured, intelligent learning platform—without requiring the user's lesson library to be hosted in the cloud.

The app should make it easy to move from “I have videos” to “I have an organized learning system.”

## 5. Core Product Principles

1. **Local-first content:** User-selected course media remains on internal storage or SD card; do not assume cloud content hosting.
2. **Explicit media assignment:** Only media explicitly assigned to a course/class is shown as learning content. Do not expose every video on the device as a course library.
3. **Native Android:** Build for Android using Kotlin. Flutter is not the chosen platform.
4. **Premium simplicity:** The interface should be clean, minimal, stock-Android-inspired, and uncluttered. Advanced tools should be discoverable through organized panels rather than permanently crowding the player.
5. **Learning-first player:** Playback controls and study tools should support comprehension, note-taking, revision, and continuity.
6. **User-controlled organization:** Support multiple courses and user-defined quantities of subjects, chapters, and classes.
7. **No invented product decisions:** Unresolved behavior must remain explicitly marked as TBD until the product owner decides.
8. **No silent scope changes:** Do not remove, replace, or add approved features without discussion and documentation.
9. **Reliable local media handling:** Missing, moved, inaccessible, or unsupported files need clear handling and recovery paths.
10. **Privacy-conscious AI exploration:** The AI Study Assistant must not silently switch to an API, local model, or another provider if the requested consumer-website approach proves infeasible.

## 6. Overall Operating Model

### A. Initial setup and content administration
1. User opens the app and creates a course.
2. User adds the applicable subjects, chapters, and classes according to the supported hierarchy.
3. User selects thumbnails and enters titles/details at relevant hierarchy levels.
4. User explicitly picks local video files through Android's file-selection mechanism and assigns them to classes.
5. The app stores course metadata and durable file references/permissions where supported; it should avoid copying large media files unnecessarily.
6. Only assigned content is surfaced in the Learning Interface.

### B. Learning
1. User browses courses and their learning units.
2. User opens a class and starts its assigned video.
3. The player restores the prior position when applicable and records viewing activity.
4. During playback, user can use approved playback controls, gestures, notes, bookmarks, transcript, markers, subtitles, and other player tools.
5. Course/lesson progress and study statistics are updated according to rules still to be finalized.
6. User can continue to the next class or use a queue where configured.

### C. Study productivity
The user can use a personal planner, reminders, focus/Pomodoro sessions, learning recommendations, playback profiles, and a learning heatmap. Their detailed algorithms, scheduling rules, and completion semantics require product-owner clarification where noted in `FEATURE_SPEC.md`.

### D. AI Study Assistant
A native chat interface is desired. The user's preferred concept is to use one external provider's consumer website/session (Gemini is the preliminary candidate), without an API key or API integration, and present the interaction inside the app's native chat UI. This is **not yet a verified or approved technical implementation**. Feasibility, provider terms, authentication restrictions, website automation permissions, and response extraction must be investigated before implementation. Never bypass CAPTCHA, authentication controls, anti-automation measures, rate limits, or provider restrictions. Do not silently substitute an API or local model.

## 7. Product Decisions and Constraints

### Confirmed direction
- Native Android application written in Kotlin.
- Local/internal-storage and SD-card lesson media.
- Multiple courses and user-controlled content quantities.
- Course → Subject → Chapter → Class is the canonical example hierarchy.
- Thumbnails/details at course and lower content levels.
- Separate Learning Interface and less prominent Management/Admin area, reachable through Settings or a similar discreet entry point.
- Only explicitly assigned media appears in the learning library.
- Premium, clean, stock-Android-inspired UI.
- Hybrid player design: YouTube-like clarity and PLAYit-like advanced controls/gestures.
- All previously proposed advanced video-player features are approved for scope.
- Selected productivity, recommendation, management, automation, and transcript features listed in `FEATURE_SPEC.md` are approved.

### Rejected / excluded from approved scope
- Self Assessment / Quiz generation or a quiz feature dependent on an unknown question bank.
- AI API integration / API-key-based AI use.
- A local AI model.
- Multi-provider AI selection: the user wants one best provider, not a provider selector or multi-provider architecture.

### Technical direction (accepted as intended stack, details remain designable)
- Kotlin.
- Jetpack Compose for UI.
- Room for structured local data.
- Android Media3 / ExoPlayer for playback.
- DataStore for preferences.
- WorkManager for background work where appropriate.
- Storage Access Framework (SAF) and persisted URI permissions for user-selected files, subject to platform behavior.

These are the intended choices, not a license to skip architecture review. Verify current Android APIs, library compatibility, lifecycle constraints, and feature feasibility before implementation.

## 8. Scope Boundaries

### In scope
- Local course and media management.
- Learning UI and advanced video playback.
- Progress, notes, bookmarks, transcripts, revision tools, and statistics.
- Study planning, focus sessions, reminders, recommendations, heatmap.
- Course organization and media intelligence.
- Productivity automation and global search.
- AI Study Assistant feasibility investigation for the single-provider consumer-website concept.

### Out of scope unless explicitly approved later
- Quiz/Self-Assessment.
- AI API integration and API keys.
- Local AI model.
- Multi-provider AI integration.
- A cloud-hosted course-content platform or public course marketplace.

## 9. Open Product Questions

The following are deliberately not invented:
- `TBD-01`: Is Subject mandatory, or may a Course contain Chapters directly?
- `TBD-02`: Exact metadata fields for Course, Subject, Chapter, and Class.
- `TBD-03`: What counts as lesson/course completion, and how is progress calculated?
- `TBD-04`: Exact recommendation inputs, ranking, and explanation behavior.
- `TBD-05`: Planner model, recurring schedules, task structure, and conflict handling.
- `TBD-06`: Reminder scheduling, quiet hours, and notification controls.
- `TBD-07`: Transcript source/creation/import format and language handling.
- `TBD-08`: AI consumer-website automation feasibility, provider permission, login/session model, and fallback decision.
- `TBD-09`: Backup/restore coverage, destination, encryption, and media-file inclusion policy.
- `TBD-10`: App lock, data privacy, and local data protection requirements.
- `TBD-11`: Exact navigation structure and final screen layouts.
- `TBD-12`: Player limits (speed range, seek interval defaults, gesture defaults, audio boost/equalizer implementation).
- `TBD-13`: Video comparison and casting technical support/device constraints.
- `TBD-14`: Missing/moved file recovery workflow.
- `TBD-15`: Smart revision-point algorithm.
- `TBD-16`: Exact storage intelligence metrics and duplicate-detection policy.

## 10. Related Documents

- `FEATURE_SPEC.md` — approved feature inventory, behavior, business rules, dependencies, edge cases, and TBDs.
- `UI_UX_SPEC.md` — experience principles, screen inventory, navigation proposal, screen states, and visual/interaction rules.
- `CLAUDE.md` — mandatory operating instructions for Claude and the development workflow.

## 11. Authority and Change Control

The user's explicit decisions are authoritative. This package distinguishes:
- **APPROVED:** explicitly accepted by the user.
- **REJECTED / OUT OF SCOPE:** explicitly declined or excluded.
- **PROPOSED DIRECTION:** a design/technical approach discussed but not necessarily finalized.
- **TBD / CLARIFICATION NEEDED:** not decided; Claude must ask before locking behavior.

If later user instructions conflict with this package, do not silently choose. Explain the conflict, ask which decision supersedes the earlier one, then update all affected documents consistently.
