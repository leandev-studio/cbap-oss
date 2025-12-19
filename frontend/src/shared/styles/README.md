# CBAP Global Styles

This directory contains the global CSS styles for the CBAP application, implementing the color system defined in `COLOR_GUIDE.md`.

## Files

- **global.css**: Main stylesheet with CSS custom properties (variables) for both light and dark themes
- **css-variables.d.ts**: TypeScript declarations for CSS variables (for type safety)

## Usage

### CSS Variables

All colors are available as CSS custom properties. Use them in your CSS files or inline styles:

```css
.my-component {
  background-color: var(--color-bg-primary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}
```

### Utility Classes

The global CSS includes utility classes for common patterns:

```html
<div class="bg-primary text-secondary border">
  Content here
</div>
```

### Theme Switching

Use the theme utilities from `shared/utils/theme.ts`:

```typescript
import { setTheme, toggleTheme, getInitialTheme } from '@shared/utils/theme';

// Set theme
setTheme('dark');

// Toggle theme
const newTheme = toggleTheme();

// Get current theme
const current = getInitialTheme();
```

### Theme Detection

The theme is automatically initialized on app load and respects:
1. User's stored preference (localStorage)
2. System preference (prefers-color-scheme)
3. Defaults to light theme

## Color System

The color system includes:

- **Base Colors**: Backgrounds and surfaces
- **Text Colors**: Primary, secondary, tertiary, inverse, disabled
- **Primary Brand Colors**: Main brand color with variants
- **Semantic Colors**: Success, Warning, Error, Info (each with light, dark, subtle variants)
- **Neutral Colors**: For borders, dividers, icons
- **Interactive States**: Default, hover, active, focus, disabled
- **Status Colors**: Workflow states, task status, document status

All colors meet WCAG 2.1 AA contrast requirements for accessibility.

## Integration with Material UI

The global CSS works alongside Material UI's theme system. The MUI theme (defined in `shared/theme.ts`) uses the same color values, ensuring consistency across the application.
