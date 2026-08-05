# Branding Assets

The repository uses the Nexora logo supplied by the project owner.

| Asset | Purpose |
|---|---|
| `NexoraAdminPanelUI/src/assets/nexora-logo-primary.png` | Imported React asset used on login, navigation, and fatal-error screens |
| `NexoraAdminPanelUI/public/favicon.png` | Square browser favicon derived from the “N” mark |

The primary file was alpha-cropped and resized to reduce its download size while preserving the original artwork, colors, proportions, and tagline. The favicon is a deterministic crop of the same supplied artwork; no generative redesign was applied.

Do not stretch, recolor, redraw, or place the logo on a background that makes the tagline unreadable. Import the primary asset through `BrandLogo` rather than adding absolute root URLs, because the application is hosted under a configurable secret base path.
