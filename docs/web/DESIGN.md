# Design System Document

## 1. Overview & Creative North Star: "The Golden Precision"

This design system is built to bridge the gap between complex AI logic and human-centric simplicity. Our Creative North Star is **"The Golden Precision."** We reject the cluttered, "dashboard-heavy" aesthetic of traditional no-code tools in favor of an editorial, high-end experience that feels like a premium concierge service rather than a technical utility.

To achieve this, we move beyond the "standard" grid. We utilize **intentional asymmetry**—pairing large, breathable typography with compact, dense data nodes. We break the "template" look by layering surfaces like fine stationery, using depth and tonal shifts rather than rigid lines to guide the eye. Every interaction should feel intentional, reliable, and technologically superior.

---

## 2. Colors & Surface Philosophy

The palette is anchored by a sophisticated Gold and Deep Charcoal, creating a high-contrast environment that screams authority and precision.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or containment. 
Boundaries must be defined solely through background color shifts or tonal transitions. To separate a side-bar from a main feed, use `surface-container-low` against a `surface` background. The eye should perceive change through mass, not lines.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of materials. 
- **Base Layer:** `surface` (#f9f9f9)
- **Secondary Sections:** `surface-container` (#eeeeee)
- **Interactive Cards:** `surface-container-lowest` (#ffffff) to create a subtle "pop."
- **Focus Areas:** `surface-container-highest` (#e2e2e2) for modal backdrops or deep-inset elements.

### The "Glass & Gold" Rule
To elevate the AI experience, use **Glassmorphism** for floating elements (e.g., sticky headers or hovering AI chat bubbles). Use `surface` with a 70% opacity and a `20px` backdrop-blur. 
**Signature Texture:** Primary CTAs should not be flat. Use a subtle linear gradient from `primary` (#785900) to `primary_container` (#ffc107) at a 135-degree angle to provide a "metallic sheen" that evokes premium quality.

---

## 3. Typography: Editorial Authority

We use a dual-typeface system to balance technical precision with modern elegance.

*   **Display & Headlines:** **Manrope.** Its geometric yet warm proportions provide a "custom" feel. Use `display-lg` (3.5rem) with tight letter-spacing (-0.02em) for hero moments to create an editorial impact.
*   **Body & UI:** **Inter.** Chosen for its extreme legibility at small sizes. Use `body-md` (0.875rem) for the majority of user-generated content and AI outputs.

**Hierarchy as Identity:** 
High-contrast scaling is mandatory. Pair a `headline-lg` title with a `body-sm` description. This "Large-Small" juxtaposition creates a sense of professional curation, moving away from the "medium-everything" look of generic bootstrap templates.

---

## 4. Elevation & Depth: Tonal Layering

We convey hierarchy through light and shadow, mimicking physical depth.

*   **The Layering Principle:** Place a `surface-container-lowest` card (Pure White) on top of a `surface-container-low` section. This creates a natural "lift" without a single drop shadow.
*   **Ambient Shadows:** If a component must float (e.g., a dropdown or modal), use an ultra-diffused shadow:
    *   *Y: 12px, Blur: 32px, Color: `on-surface` (#1a1c1c) at 4% opacity.*
    *   Shadows must feel like ambient occlusion, not a dark glow.
*   **The "Ghost Border" Fallback:** If accessibility requires a stroke (e.g., in high-contrast mode), use `outline-variant` (#d4c5ab) at **15% opacity**. 100% opaque borders are strictly forbidden.

---

## 5. Components

### Buttons: The Tactile Command
- **Primary:** Gradient (`primary` to `primary_container`), `on-primary` text, `DEFAULT` (0.5rem) roundedness. 
- **Secondary:** `surface-container-lowest` background with a `Ghost Border`.
- **Tertiary:** No background. Bold `primary` text with an icon.
- **States:** On `hover`, increase the gradient intensity. On `pressed`, use a subtle inner shadow to simulate a physical "click."

### Cards & Lists: The Separation of Thought
- **Rule:** Forbid the use of divider lines. 
- **Implementation:** Use `3.5rem` (Spacing 10) of vertical whitespace between list items, or shift the background color of alternating items to `surface-container-low`.

### Input Fields: Precision Inputs
- **Style:** Use `surface-container-low` as the field background. No border.
- **Focus State:** Transition the background to `surface-container-lowest` and add a `2px` "Ghost Border" using the `primary` color at 40% opacity.

### Additional Signature Component: The "AI Pulse" Chip
- A specialized chip for AI-generated suggestions. 
- **Style:** `tertiary_container` (#00defd) background with 10% opacity, `tertiary` text, and a `full` (9999px) roundedness. It should feel distinct from standard UI elements to signal "Machine Intelligence."

---

## 6. Do’s and Don'ts

### Do
- **Do** use `2.75rem` (Spacing 8) as your minimum gutter for page margins. Whitespace is a luxury asset.
- **Do** use `manrope` for any text larger than 24px.
- **Do** use `primary_fixed_dim` (#fabd00) for star ratings or "Success" indicators to maintain the gold theme.

### Don't
- **Don't** use pure black (#000000). Use `on_surface` (#1a1c1c) for text to maintain tonal softness.
- **Don't** use standard 4px corners. Stick strictly to the **8px (DEFAULT)** or **12px (md)** scale to ensure the UI feels modern and approachable.
- **Don't** use icons from different libraries. Use a single, high-quality, 2pt weight line-art set to maintain "Technological Precision."