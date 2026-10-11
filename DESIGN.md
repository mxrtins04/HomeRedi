---
# gstack: design-md-format=spec
name: HomeRedi
description: A calm, practical operations desk for property readiness and turnover work.
colors:
  primary: "#1F4033"
  on-primary: "#FFFFFF"
  on-primary-dark: "#122019"
  canvas: "#F3F5F1"
  surface: "#FFFFFF"
  surface-soft: "#EDF1EC"
  text: "#1C2922"
  text-muted: "#58665E"
  line: "#DBE2DC"
  line-strong: "#C7D1C9"
  accent: "#C15029"
  accent-soft: "#FBECE5"
  success: "#247349"
  success-soft: "#E6F2E9"
  warning: "#98600B"
  warning-soft: "#FFF2D9"
  error: "#B33B34"
  error-soft: "#FCEAE7"
  offline: "#626E67"
  offline-soft: "#EDF0ED"
  canvas-dark: "#121A16"
  surface-dark: "#1B2620"
  surface-soft-dark: "#243129"
  text-dark: "#EDF3EE"
  text-muted-dark: "#AAB8AE"
  line-dark: "#35453A"
  line-strong-dark: "#506253"
  primary-dark: "#A7D4B6"
  primary-hover-dark: "#C1E5CC"
  accent-dark: "#F29A70"
  accent-soft-dark: "#40271E"
  success-dark: "#87D6A2"
  success-soft-dark: "#20392A"
  warning-dark: "#F0C16E"
  warning-soft-dark: "#3B301E"
  error-dark: "#F18F85"
  error-soft-dark: "#402522"
  offline-dark: "#BCC8BF"
  offline-soft-dark: "#2D3831"
typography:
  display:
    fontFamily: "Bricolage Grotesque"
    fontWeight: 600
    fontSize: "clamp(1.625rem, 3vw, 1.875rem)"
    letterSpacing: "-0.035em"
  body:
    fontFamily: "Instrument Sans"
    fontSize: 1rem
    lineHeight: 1.5
  label:
    fontFamily: "IBM Plex Mono"
    fontSize: 0.6875rem
    letterSpacing: 0.05em
  mono:
    fontFamily: "IBM Plex Mono"
    fontFeature: tnum
rounded:
  sm: 4px
  md: 6px
  lg: 8px
  xl: 10px
  phone: 26px
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  2xl: 32px
  3xl: 48px
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
  button-primary-dark:
    backgroundColor: "{colors.primary-dark}"
    textColor: "{colors.on-primary-dark}"
    rounded: "{rounded.md}"
  button-primary-hover:
    backgroundColor: "#285642"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    borderColor: "{colors.line-strong}"
    rounded: "{rounded.md}"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    borderColor: "{colors.line-strong}"
    rounded: "{rounded.md}"
  card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.line}"
    rounded: "{rounded.lg}"
  nav-link:
    textColor: "{colors.text-muted}"
    rounded: "{rounded.sm}"
  status-ready:
    backgroundColor: "{colors.success-soft}"
    textColor: "{colors.success}"
  status-at-risk:
    backgroundColor: "{colors.warning-soft}"
    textColor: "{colors.warning}"
  status-not-ready:
    backgroundColor: "{colors.error-soft}"
    textColor: "{colors.error}"
  status-offline:
    backgroundColor: "{colors.offline-soft}"
    textColor: "{colors.offline}"
---

# HomeRedi

## Overview

**Creative North Star:** A clear operations desk that makes property readiness, blockers, ownership, and arrival deadlines easy to scan.

**Product context:** The long-term product hypothesis is property reliability and operations software for Nigerian short-let and serviced-apartment operators. The current implementation slice is a caretaker's happy-path post-checkout condition inspection. It does not include cleaning, issue reporting, a readiness decision, or maintenance dispatch. The 10–50-unit segment is an initial targeting hypothesis, not a validated market boundary. See `HomeRedi_Current_State.md` for current product decisions and `reports/20261001_180000_nigeria_shortlet_operations.md` for dated research evidence.

**Mode per surface:** Caretaker post-checkout inspection — Operate. Future manager readiness workspace, turnover coordination, and cleaner task — Operate when those capabilities enter scope. Reference and status views — Read.

**Reference sites:** https://www.breezeway.io/vacation-rental, https://getproperly.com/vacation-rental-inspection-software, https://residly.ng/, https://stayops.com.ng/

**Key characteristics:**
- The future manager workspace should make arrival deadlines, unit condition, blockers, and next ownership easy to scan.
- The current slice centers on a caretaker inspecting a unit after checkout and checking its spaces one at a time.
- Readiness, cleaning, issue ownership, and photo evidence are later capabilities; do not present them as implemented Slice 1 behavior.
- Status colors carry consistent meaning across summaries, tasks, and alerts.

## Colors

**Strategy:** Restrained palette with dedicated state colors. Pine anchors the product; warm signal orange marks actionable links and emphasis. Green, amber, red, and slate keep readiness states distinct.

**Light or dark:** Light is the default for daytime portfolio work. Dark mode is available for low-light use. It uses its own surface and status tokens to preserve contrast and hierarchy instead of simply inverting the light palette. Use the corresponding `-dark` tokens for dark surfaces, text, and status colors.

## Typography

Bricolage Grotesque gives page titles and key property names a distinct, compact voice. Instrument Sans handles navigation, instructions, tables, and controls for comfortable repeated scanning. IBM Plex Mono is reserved for timestamps, arrival deadlines, compact counts, and uppercase operational labels. Load all three variable families from Google Fonts; retain sans-serif and monospace fallbacks. Keep display styling to headings and high-priority arrival information.

## Layout

Use a disciplined desktop workspace with a narrow persistent navigation rail and a flexible content area. At 850px, stack turnover and field-task columns and reduce the rail width. At 620px, turn navigation into a horizontal scroll row, keep table content horizontally scrollable, and convert turnover progress to a vertical sequence. Use dense but clearly separated rows for property queues; reserve larger gaps for page sections and handoff stages.

## Elevation & Depth

Use thin borders to define tables, cards, and nested app areas. Reserve a soft offset shadow for the main app frame and phone preview. Avoid glow effects and unnecessary stacked cards.

## Shapes

Use small radii for controls and status pills, medium radii for task panels, and a slightly larger radius for the main application frame. Keep phone framing distinct from in-app cards. Nested surfaces should use a smaller radius than their parent.

## Components

Primary buttons use the pine action color and a clear hover state. Secondary buttons use a surface fill and visible border; ghost actions remain visually quiet until hover. Provide visible keyboard focus, pressed, and disabled states in implementation. Status labels pair color with text and a compact shape marker so meaning does not rely on color alone. Inputs use a strong neutral border and an accent focus ring. Navigation highlights the active section with a soft surface tint and clear text weight.

## Do's and Don'ts

- Do put arrivals, readiness state, and next owner near the top of manager views.
- Do show the reason and timestamp when a turnover is blocked or returned.
- Do keep cleaner tasks readable on narrow screens and attach evidence to the specific job.
- Do use state color consistently and pair it with a written label.
- Don't present unsupported savings or efficiency claims as measured outcomes.
- Don't make every panel a floating card; use rules and whitespace for structure.
- Don't use color alone to communicate readiness.
- Don't add revenue management, vendor marketplace, or predictive-maintenance UI to the current inspection slice.

## Motion

- **Approach:** Minimal-functional.
- **Easing:** enter(ease-out), exit(ease-in), move(ease-in-out).
- **Duration:** micro(50–100ms), short(150–250ms), medium(250–400ms), long(400–700ms).
- **The one authored moment for the current slice:** A caretaker marks the inspection complete after passing every space; the UI confirms that the inspection was recorded. Do not imply that this makes the unit clean or guest-ready.

## Decisions Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-09-28 | Initial design system created | Based on the approved app screens, product brief, and visual research. |
