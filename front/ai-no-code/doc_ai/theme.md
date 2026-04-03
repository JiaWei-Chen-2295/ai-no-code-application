# AppCraft Theme

## 1. Theme Positioning

Theme name: `Creator Studio`

Product feeling:
- Clean
- Refined
- Trustworthy
- Calm futuristic
- Workbench-like

This product should feel like an AI application studio, not a generic admin panel and not an over-decorated AI landing page.
The interface goal is to let users feel they can immediately start creating, previewing, iterating, and publishing.

Design keywords:
- AI workspace
- Showcase-driven
- Soft technology
- Clear hierarchy
- Lightweight premium

One-sentence brand feeling:

> A quiet, polished AI product studio where ideas become usable apps fast.

---

## 2. Core Design Principles

### 2.1 Experience Priorities

Every page should optimize for these priorities in order:

1. Clarity
2. Actionability
3. Visual comfort
4. Delight

### 2.2 Interface Rules

- The interface must guide the user toward the next action.
- Important actions should stand out through hierarchy, not visual noise.
- Use whitespace, typography, and layout before using strong borders or heavy gradients.
- Product pages should emphasize creation results, not only configuration fields.
- Keep visual rhythm calm and consistent across all pages.

### 2.3 Avoid

- Large areas of flat pure white without hierarchy
- Too many saturated colors on the same screen
- Heavy 2px to 3px borders on every card
- Excessive gradients as decoration
- Emoji-style visuals as core UI language
- Pages that look like backend tables when they should feel like creative tools

---

## 3. Visual Direction

### 3.1 Overall Mood

The UI should combine:
- A soft warm-gray background
- Frosted translucent surfaces
- Deep teal primary actions
- Warm apricot/gold accent highlights
- Gentle shadows and restrained motion

The result should feel more premium and more product-focused than the current mixed green-blue-yellow palette.

### 3.2 Visual Metaphor

The product should feel like a creative desk:
- Background is calm and unobtrusive
- Cards are like glass sheets or project canvases
- Actions are like focused tools
- Previews are treated as hero content

---

## 4. Color System

### 4.1 Semantic Palette

Primary colors:
- `--color-primary-50: #e9f5f3`
- `--color-primary-100: #d3ebe7`
- `--color-primary-200: #a6d7cf`
- `--color-primary-300: #79c3b7`
- `--color-primary-400: #45a99d`
- `--color-primary-500: #0f766e`
- `--color-primary-600: #0c625c`
- `--color-primary-700: #0b4f4a`
- `--color-primary-800: #083c38`
- `--color-primary-900: #052927`

Accent colors:
- `--color-accent-50: #fdf5eb`
- `--color-accent-100: #fbe8c9`
- `--color-accent-200: #f8d59f`
- `--color-accent-300: #f4b860`
- `--color-accent-400: #e7a64a`
- `--color-accent-500: #d28e35`

Neutral colors:
- `--color-bg: #f6f7f4`
- `--color-bg-elevated: #fbfbf9`
- `--color-surface: rgba(255, 255, 255, 0.78)`
- `--color-surface-solid: #ffffff`
- `--color-line: rgba(19, 32, 38, 0.08)`
- `--color-line-strong: rgba(19, 32, 38, 0.14)`
- `--color-text: #132026`
- `--color-text-soft: #5f6b73`
- `--color-text-muted: #8a949b`

State colors:
- `--color-success: #3f8f72`
- `--color-warning: #d79a3c`
- `--color-error: #b85c5c`
- `--color-info: #4c89a8`

### 4.2 Usage Rules

- Primary teal is used for brand anchors, major CTAs, active states, and focused interactions.
- Accent apricot is used sparingly for highlights, featured labels, and special conversion moments.
- Neutral colors must dominate the screen area.
- Accent should never overpower primary.
- Avoid using more than one bright highlight color in the same component.

### 4.3 Recommended Ratios

- 70% neutral background and surfaces
- 20% text and structural contrast
- 10% branded color and interaction highlights

---

## 5. Surface and Depth

### 5.1 Background

Default page background:
- Use `--color-bg`
- Allow subtle radial glow or soft gradient in hero areas only

Recommended background treatments:
- `linear-gradient(180deg, #f7f7f4 0%, #f2f5f3 100%)`
- subtle radial highlight behind hero or preview area

### 5.2 Cards

Card style:
- Background: `--color-surface`
- Border: `1px solid --color-line`
- Radius: 20px to 28px
- Shadow: soft and blurred, never too dark
- Optional backdrop blur for major containers

Recommended card shadow:

```css
box-shadow:
  0 10px 30px rgba(19, 32, 38, 0.06),
  0 2px 8px rgba(19, 32, 38, 0.04);
backdrop-filter: blur(14px);
```

Card behavior:
- Hover can lift slightly
- Border color may strengthen slightly on hover
- Do not use thick outlines as the default state

### 5.3 Panels

Major workspace panels such as prompt input, chat canvas, preview area:
- Should feel larger, calmer, and more architectural than regular cards
- Use stronger radius and clearer spacing
- Use internal section dividers rather than heavy outer borders

---

## 6. Typography

### 6.1 Font Family

Recommended font stack:

- Chinese heading: `HarmonyOS Sans SC`, `Alibaba PuHuiTi 3.0`, `PingFang SC`, sans-serif
- Body: `PingFang SC`, `Noto Sans SC`, `Segoe UI`, sans-serif
- Monospace: `JetBrains Mono`, `Fira Code`, `Consolas`, monospace

### 6.2 Type Tone

- Headlines should feel confident and composed
- Body text should remain highly readable
- Metadata should be quiet but not faint

### 6.3 Type Scale

- Display title: `48px - 64px`, weight `700`
- H1: `36px - 48px`, weight `700`
- H2: `28px - 32px`, weight `600`
- H3: `20px - 24px`, weight `600`
- Body large: `18px`
- Body: `16px`
- Small: `14px`
- Caption: `12px - 13px`

### 6.4 Text Colors

- Primary title text: `--color-text`
- Secondary headings: `--color-primary-700`
- Body text: `--color-text-soft`
- Meta text: `--color-text-muted`

### 6.5 Typography Rules

- Prefer fewer font weights with clearer hierarchy
- Large titles should use tighter tracking
- Keep paragraph width controlled
- Do not rely on color alone to create hierarchy

---

## 7. Layout Language

### 7.1 Global Layout

The layout should feel spacious and editorial.

Rules:
- Use wider content breathing room
- Group content into clear zones
- Avoid stacking too many unrelated cards tightly together

Recommended content widths:
- Standard content max width: `1200px - 1280px`
- Hero content max width: `1280px - 1360px`
- Reading content width: `720px - 840px`

### 7.2 Spacing Rhythm

Use an 8px-based rhythm.

Recommended major spacing:
- Section gap: `64px - 96px`
- Card padding: `20px - 32px`
- Component gap: `12px - 20px`
- Tight inline gap: `6px - 10px`

### 7.3 Corner Radius

- Small elements: `10px - 12px`
- Inputs and buttons: `14px - 18px`
- Cards: `20px - 24px`
- Hero panels: `28px - 32px`

---

## 8. Component Language

### 8.1 Buttons

Primary button:
- Solid teal background
- White text
- Medium or semibold weight
- Slight lift on hover

Secondary button:
- Translucent white or pale surface
- Thin border
- Text in primary teal

Ghost button:
- No hard fill
- Quiet neutral text
- Used for tertiary actions only

Button rules:
- Primary CTA should be unique in a local area
- Avoid having multiple equally bright buttons adjacent to each other
- Use shape and spacing to support hierarchy

### 8.2 Inputs

Input style:
- Soft surface background
- Thin border
- Strong focus ring in teal
- Comfortable inner padding

Prompt input should feel like a creative command surface, not a plain form field.

### 8.3 Chips and Tags

Use chips for:
- Example prompts
- App type
- Status
- Filters

Style:
- Soft surface background
- Rounded full shape or rounded pill
- Quiet border
- Active chip can use pale teal fill

### 8.4 Cards

App cards should emphasize:
- Cover or preview
- Title
- One-line summary
- Status
- Updated time

Cards should not feel like admin records.
They should feel like project tiles or creative works.

### 8.5 Navigation

Header should be:
- Light
- Sticky
- Clean
- Not visually heavier than page content

Navigation items:
- Use subtle hover states
- Strong active state, but not loud
- Primary creation action may sit on the right

---

## 9. Motion Language

### 9.1 Motion Personality

Motion should feel:
- Soft
- Intentional
- Helpful
- Calm

Not:
- Bouncy everywhere
- Hyperactive
- Decorative without meaning

### 9.2 Recommended Motion

- Hover lift: `translateY(-2px)` to `translateY(-4px)`
- Fade and rise on section reveal
- Smooth state transitions on hover/focus
- Gentle loading shimmer or pulse in workspace areas

### 9.3 Duration

- Quick hover: `150ms - 180ms`
- Standard transition: `220ms - 280ms`
- Page section reveal: `320ms - 420ms`

### 9.4 Prohibited Motion

- Infinite floating on too many elements
- Repetitive scaling animations on multiple CTAs
- Flashing gradients
- Heavy parallax for product surfaces

---

## 10. Page-Specific Design Rules

### 10.1 Home Page

The home page should contain four clear zones:

1. Hero
2. Prompt creation workbench
3. Featured showcase
4. Capability/trust strip

Hero requirements:
- One strong product promise
- One short supporting line
- One dominant prompt action
- One visual preview area or concept panel

Prompt area requirements:
- Large prompt field
- Example prompt chips
- Clear generation type selection
- One strong create action

Featured showcase requirements:
- More visual than textual
- Should feel like a curated gallery, not plain repeated cards

### 10.2 App List Page

The app list page should feel like a creator dashboard:
- Search and filter first
- Clear status system
- Continue creation should be the main action
- Preview and manage are secondary

### 10.3 App Detail Page

The detail page should feel like a project presentation page:
- Preview is first-class
- Metadata is supportive
- Actions are grouped by next step
- Avoid backend-style descriptions as the only layout

### 10.4 Chat Workspace Page

The chat page is the core product experience and should be treated as a studio workspace.

Must include clear hierarchy for:
- App identity
- Current stage
- Conversation
- Live preview
- Deployment readiness

On mobile:
- Use tabbed switching between conversation and preview
- Avoid cramped split-view layouts

---

## 11. Status System

Use consistent status labels across pages:

- `Draft`
- `Generating`
- `Ready to Preview`
- `Ready to Deploy`
- `Deployed`
- `Error`

Status rules:
- Status should appear as calm pills, not alarming badges
- Use color as support, not as the only differentiator
- Each status should map to a recommended next action

Recommended mapping:
- Draft -> Continue creation
- Generating -> Wait / View progress
- Ready to Preview -> Preview
- Ready to Deploy -> Deploy
- Deployed -> Open live site
- Error -> Fix and retry

---

## 12. Iconography and Imagery

### 12.1 Icons

Use consistent line icons as the primary icon style.

Rules:
- Stroke-based icons preferred
- Avoid mixing emoji, filled icons, and outline icons randomly
- Important product actions should use the same icon family

### 12.2 Images and Covers

App previews should be treated as product assets.

Rules:
- Prefer real preview thumbnails over generic placeholder icons
- If no preview is available, use a branded abstract cover
- Placeholder visuals should still feel premium and consistent

---

## 13. Accessibility and Usability

### 13.1 Readability

- Body text must maintain strong contrast
- Muted text should still remain readable
- Avoid tiny low-contrast captions

### 13.2 Interaction

- All interactive areas need clear hover and focus feedback
- Buttons and tags must have comfortable touch targets
- Major flows should be understandable without needing prior training

### 13.3 Consistency

- Similar actions should look similar across pages
- Similar content types should use the same card structure
- Page-level behavior should not change without strong reason

---

## 14. Implementation Guidance

This document should drive future updates to:

- `src/styles/variables.css`
- shared button, card, tag, and layout styles
- home page redesign
- app list redesign
- app detail redesign
- chat workspace redesign

Implementation strategy:

1. Update design tokens first
2. Unify global surface, button, and card styles
3. Redesign home page
4. Redesign chat workspace
5. Redesign app list and detail pages

---

## 15. Quick Checklist

Before shipping a new page or component, confirm:

- Does it look like part of `Creator Studio`?
- Is the next action obvious?
- Is the page calm rather than noisy?
- Are surfaces, spacing, and buttons consistent with the theme?
- Does the page feel like a creative product, not a generic admin screen?
- Is preview or output emphasized where it matters?

If the answer to any of the above is no, the design is not aligned yet.
