# UI / UX SPECIFICATION
## Personal Local Learning & Study Management App

**Status:** Product-level visual principles are approved; detailed screen layouts and navigation are a proposed structure pending confirmation. Do not treat proposed screen arrangements as final user decisions.

---

## 1. Experience Vision

The app should feel like a polished, premium native Android learning product:
- Clean, minimal, and stock-Android-inspired.
- Clear hierarchy and predictable navigation.
- High-quality typography, spacing, iconography, and motion.
- Learning content should be the visual priority.
- Management tools should be available but discreet, not dominate the everyday learning experience.
- Advanced player capabilities must remain discoverable without cluttering the main playback surface.

### Player-specific visual direction
The player is an approved hybrid:
- YouTube-inspired clean, intuitive presentation.
- PLAYit-inspired advanced playback controls and gestures.
- Bespoke learning tools integrated into a premium, minimalist player.
- Main controls should remain simple; secondary and advanced controls belong in organized menus, sheets, or panels.
- Avoid permanently displaying every advanced feature.

## 2. Information Architecture

The app has two major functional areas:

1. **Learning Interface** — the primary everyday area for browsing courses, opening lessons, watching videos, and tracking study.
2. **Management / Admin Area** — a less prominent area for creating/editing courses and learning hierarchy, assigning local media, selecting thumbnails, and organizing content. Entry should be discreetly available through Settings or a similar location.

The exact navigation component (bottom navigation, navigation rail, drawer, tabs, etc.) is **TBD**. The following screen inventory is a functional proposal, not a finalized wireframe.

## 3. Proposed Screen Inventory

### 3.1 Home / Learning Dashboard
**Purpose:** Give the learner a quick overview and a starting point.

Potential elements:
- Continue Watching section.
- Course library or course collections.
- Current/next planned study item.
- Quick access to Focus Session.
- Learning progress/statistics summary.
- Smart recommendations.
- Recent learning activity.

**Behavior**
- Selecting a course opens its overview.
- Selecting Continue Watching opens the relevant class/player and resumes according to LP-001.
- Selecting a recommendation opens its target lesson or relevant action.
- Do not display unassigned device videos.

**States**
- Empty: no courses yet; guide user to create/import content through the Management entry.
- Loading: show lightweight placeholders while local data is read.
- Error: explain local database/content loading issue and provide a retry path.

### 3.2 Course Library
**Purpose:** Browse all courses and custom collections.

Elements:
- Course cards/list rows with thumbnail, title, and available progress summary.
- Collection filters or collection sections.
- Search entry.
- Sorting/reordering affordance where appropriate.

Interactions:
- Tap course → Course Overview.
- Open collection → filtered course list.
- Reorder only where the user has enabled/entered organizer mode; avoid accidental drag actions.

States:
- Empty library.
- Empty collection.
- Search with no results.
- Loading and recoverable error.

### 3.3 Course Overview
**Purpose:** Show the course structure and learning status.

Elements:
- Course cover/thumbnail, title, details.
- Course progress summary (once LP-002 formula is defined).
- Subject list and/or direct chapter list depending on TBD-01.
- Course-specific playback profile indicator/settings entry.
- Related planner/recommendation context where useful.

Interactions:
- Tap Subject → Subject Overview.
- If direct chapters are supported, tap Chapter → Chapter Overview.
- Course appearance customization should be accessed without cluttering the learning view.

### 3.4 Subject Overview
**Purpose:** Browse chapters within a subject.

Elements:
- Subject thumbnail, title, details.
- Chapter list/cards with thumbnail and progress summary.
- Ordering behavior.
- Search/filter if needed.

### 3.5 Chapter Overview
**Purpose:** Browse classes within a chapter.

Elements:
- Chapter thumbnail, title, details.
- Class list with class title, thumbnail, assigned-media status, progress/resume indicator.
- Clear visual distinction between assigned and unassigned classes.

Interactions:
- Tap class → Class Details or directly Player depending on final navigation decision.
- Unassigned class should show a clear no-video state; management action is not necessarily exposed in the learning interface.

### 3.6 Class / Lesson Details
**Purpose:** Provide a focused entry point to a lesson.

Potential elements:
- Class thumbnail/title/details.
- Assigned video availability.
- Continue/Start action.
- Progress/resume information.
- Notes, bookmarks, revision points, transcript availability.
- Next/previous class context.

**TBD:** Whether this is a separate screen or a compact sheet before playback.

### 3.7 Video Player
**Purpose:** Deliver the main study playback experience.

#### Main control layer
Keep the primary surface uncluttered:
- Play/Pause.
- Seekbar and current/total time.
- Playback speed shortcut or profile indicator.
- Fullscreen/orientation control.
- Next/Previous class where queue context exists.

#### Quick control layer
Accessible through a compact panel/menu:
- Brightness.
- Volume.
- Subtitle.
- Zoom/aspect mode.
- Lock controls.
- Sleep timer.

#### Learning tools layer
Organized in a panel or sheet:
- Bookmark.
- Notes.
- Transcript.
- Video chapter markers.
- Screenshot/frame capture.
- A-B repeat.
- Revision list.

#### Advanced settings layer
Separate settings/panel:
- Audio track and audio controls.
- Subtitle customization and delay.
- Gesture settings.
- Playback profile.
- Appearance/control layout.
- Performance-related settings where user-facing.

#### Gesture interactions
Approved gestures include:
- Brightness and volume gestures.
- Pinch to zoom.
- Horizontal swipe to seek.
- Double-tap seek.
- Press-and-hold temporary 2x.
- Optional three-finger gestures.
- Customizable zones/sensitivity and enable/disable controls.

Exact zones, mappings, sensitivity, defaults, and gesture conflict behavior are TBD.

#### Timeline
The timeline may show:
- Playback position.
- Watched/unwatched indication.
- Bookmark markers.
- Note markers.
- User-created chapter markers.
- Seekbar thumbnail preview.

The exact visual encoding and marker collision behavior are TBD.

#### Player states
- Loading/buffering: clear progress indicator; avoid blocking unrelated controls unless necessary.
- Playing/paused: controls appear on interaction and may auto-hide according to a TBD timeout.
- Seeking: responsive preview and clear target time.
- Missing file: explain that the assigned video is unavailable and provide a recovery route.
- Unsupported/corrupt file: show a readable error and possible next action.
- Audio/subtitle unsupported: show unavailable state, not a fake selectable option.
- PiP/background: use system-supported presentation and preserve playback state.
- Locked controls: visibly indicate lock and provide an intentional unlock gesture/action.

### 3.8 Transcript Panel
**Purpose:** Read and navigate a lesson transcript.

Elements:
- Transcript text segments.
- Search within transcript.
- Active/current segment highlight where supported.
- Timestamp association.
- Tap segment to seek.

States:
- Transcript unavailable.
- Transcript loading.
- Search no results.
- Malformed or unsynchronized transcript with clear warning.

Transcript source and format are TBD; do not imply automatic transcription is approved.

### 3.9 Notes and Bookmarks
**Purpose:** Review personal learning annotations.

Potential tabs/filters:
- Notes.
- Bookmarks.
- Video chapter markers.
- Revision points.

Interactions:
- Tap timestamp entry → open player at that time.
- Edit/delete annotation with confirmation where destructive.
- Search entries through Global Search.

### 3.10 Study Planner
**Purpose:** View and manage planned learning.

Potential views:
- Upcoming study items.
- Calendar/time-based schedule.
- Task/list view.
- Course/class association.
- Completion status.

Exact calendar/list format, recurring schedule UI, and rescheduling behavior are TBD. Do not assume external calendar integration.

### 3.11 Focus / Pomodoro
**Purpose:** Provide a distraction-minimized focus timer.

Elements:
- Timer and session state.
- Start, pause, resume, finish controls.
- Optional linked course/class (TBD).
- Break state and session summary (TBD).

The visual treatment should be calm and focused, not gamified unless separately approved.

### 3.12 Statistics and Learning Heatmap
**Purpose:** Show study activity and progress.

Potential elements:
- Study time.
- Course/lesson progress.
- Activity history.
- Heatmap calendar.
- Date range/filter.

All displayed values must be based on defined recorded events. Active-study counting and metric definitions are TBD.

### 3.13 Global Search
**Purpose:** Search across courses, subjects, chapters, classes, notes, bookmarks, and transcripts.

Expected interactions:
- Search input.
- Results grouped or filterable by content type.
- Tap course/entity → open relevant screen.
- Tap timestamp result → open player at relevant time.

Empty, no-results, loading, and indexing-error states should be designed.

### 3.14 AI Study Assistant
**Purpose:** Native chat UI for the proposed external Personal Study Assistant.

Desired elements:
- Conversation list/history (if approved after privacy design).
- Message composer.
- User and assistant message bubbles.
- Sending/loading state.
- Error/retry/session-expired state.

Important: this UI must not be presented as operational until the consumer-website automation route is verified as technically and contractually permitted. Do not silently switch to an API or local model. Provider candidate is Gemini only at this stage, and is preliminary.

### 3.15 Settings
**Purpose:** Manage app-wide preferences and access secondary tools.

Potential categories:
- Playback preferences and profiles.
- Appearance.
- Gestures.
- Notifications/reminders.
- Storage information.
- Backup and restore.
- Privacy/security.
- About/help.
- Discreet entry to Management/Admin Area.

Exact categories and ordering are TBD.

### 3.16 Management / Admin Area
**Purpose:** Create and maintain the user's learning library.

Potential sections:
- Course management.
- Subject management.
- Chapter management.
- Class management.
- Thumbnail and details editing.
- Local media assignment/relinking.
- Smart Media Library.
- Custom collections.
- Drag & Drop organizer.
- Course appearance customization.

Management should be discoverable but visually secondary to daily learning.

**Important media rule:** Use Android file selection/SAF; only explicitly assigned files appear in Learning Interface. Avoid copying large files unnecessarily. Clearly communicate permission and missing-file states.

### 3.17 Storage Intelligence
**Purpose:** Explain how much storage assigned learning media uses.

Potential elements:
- Internal storage and SD-card usage.
- Per-course storage summary.
- Missing media indicator.
- Duplicate awareness.

Exact calculation and duplicate rules are TBD. Never automatically delete source files.

### 3.18 Backup and Restore
**Purpose:** Protect app learning data.

UI should distinguish:
- Course structure/metadata.
- Progress, notes, bookmarks, planner, settings.
- Media references.
- Actual media files, if ever included.

Backup scope, destination, encryption, and restore conflict policy are TBD. Never imply media is backed up if only references are included.

## 4. Navigation and Interaction Rules

1. Learning is the primary entry experience; Management is secondary/discreet.
2. Navigation must preserve hierarchy context when moving from Course to Subject to Chapter to Class.
3. Returning from Player should restore the previous learning context.
4. Timestamp links from notes, bookmarks, transcript, and search should open the correct video and seek to the target position.
5. Missing media should not strand the user; offer a clear route to relink through Management.
6. Advanced player controls should be grouped and discoverable, not all shown at once.
7. Drag-and-drop must have clear affordances and must not accidentally start while the user is scrolling.
8. Destructive actions (delete course, hierarchy node, annotation, or media reference) require clear confirmation and must not delete original media silently.
9. UI must account for Android back navigation, lifecycle changes, screen rotation, PiP, split screen, and process recreation.
10. Accessibility: maintain readable contrast, scalable text, touch targets, and gesture alternatives.

## 5. Visual System Requirements

Approved direction:
- Premium, clean, stock-Android-inspired.
- Minimalist layout and restrained visual density.
- Consistent typography, spacing, iconography, surfaces, and motion.
- Course thumbnails/covers provide visual identity.
- Player primary controls remain prominent; advanced controls are layered.
- Custom course/player appearance is supported but should preserve usability.

TBD:
- Color palette and design tokens.
- Light/dark theme behavior.
- Shape/radius system.
- Typography scale.
- Animation duration/curves.
- Grid/list breakpoints and tablet layouts.
- Exact button sizes and seekbar styles.
- Accessibility contrast thresholds.

## 6. Common UI States

Every data-driven screen should define:

### Empty
Explain why there is no content and provide the next relevant action. Example: no courses → create a course; no assigned video → assign one in Management.

### Loading
Use lightweight progress feedback. Avoid indefinite spinners; long operations should provide status and cancellation where safe.

### Error
Use clear, human-readable explanations and a recovery action. Do not expose raw stack traces to users.

### Unavailable / Permission denied
Explain what access is missing and how the user can reselect or restore access. Never pretend the file is still playable.

### Offline / Local-first behavior
Core local learning content should remain usable without a network connection when its file references and data are available. The AI website feature necessarily depends on external network/provider availability.

### Partial data
If a thumbnail, transcript, metadata, or media reference is absent, render a consistent fallback and keep the rest of the screen usable.

## 7. UI Decisions Still Required

- Final top-level navigation model.
- Whether Course Overview can show direct Chapters or only Subjects.
- Whether Class Details is separate or playback launches directly.
- Final dashboard composition.
- Player control auto-hide timing and exact layer presentation.
- Transcript panel placement (overlay, side panel, bottom sheet, separate screen).
- Planner calendar/list presentation.
- Statistics/heatmap visual model.
- Management access location and exact admin navigation.
- Settings grouping.
- Light/dark theme policy and design tokens.
- Tablet/foldable layout behavior.

Claude must treat these as design questions, not fill them in silently.
