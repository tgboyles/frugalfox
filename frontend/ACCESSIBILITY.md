# Accessibility Testing

This project implements automated accessibility testing using ESLint and [eslint-plugin-jsx-a11y](https://github.com/jsx-eslint/eslint-plugin-jsx-a11y).

## What's Included

The accessibility linting configuration is integrated into the standard ESLint setup and runs automatically during:

- Development (via `pnpm lint:watch`)
- Build time (via `pnpm build`)
- CI/CD pipelines (via `pnpm lint`)

## Configuration

The accessibility plugin is configured in `eslint.config.js`:

```javascript
import jsxA11y from 'eslint-plugin-jsx-a11y'

export default defineConfig([
  {
    extends: [
      // ... other configs
      jsxA11y.flatConfigs.recommended,
    ],
  },
])
```

## Rules Enforced

The recommended configuration enforces WCAG 2.1 Level A & AA standards, including:

### Interactive Elements
- ✅ Click handlers must have keyboard event handlers
- ✅ Interactive elements must have proper roles
- ✅ Interactive elements must be keyboard accessible (tabIndex)

### Images
- ✅ Images must have alt text
- ✅ Alt text must not contain redundant words like "image", "photo", "picture"
- ✅ Decorative images should use empty alt=""

### ARIA
- ✅ ARIA roles must be valid
- ✅ ARIA attributes must be valid for the role
- ✅ Required ARIA attributes must be present

### Semantic HTML
- ✅ Headings must be in order (h1 -> h2 -> h3)
- ✅ Form controls must have labels
- ✅ iframe elements must have a title

### Keyboard Navigation
- ✅ Elements with onClick must also have onKeyDown/onKeyUp
- ✅ Interactive elements must be focusable
- ✅ Tab order must be logical

## Running Accessibility Checks

### During Development

```bash
# Run linting once
pnpm lint

# Run linting in watch mode
pnpm lint:watch
```

### Before Committing

```bash
# Run full type checking and linting
pnpm build
pnpm lint
```

## Examples of Violations and Fixes

### ❌ Missing Keyboard Handler

```tsx
// BAD: Click handler without keyboard handler
<div onClick={handleClick}>Click me</div>

// GOOD: Click handler with keyboard handler
<div
  role="button"
  tabIndex={0}
  onClick={handleClick}
  onKeyDown={(e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      handleClick()
    }
  }}
>
  Click me
</div>
```

### ❌ Redundant Alt Text

```tsx
// BAD: Alt text contains "photo"
<img src="sunset.jpg" alt="Photo of a sunset" />

// GOOD: Descriptive alt text without redundant words
<img src="sunset.jpg" alt="Sunset over mountain range" />
```

### ❌ Non-interactive Element with Event Handler

```tsx
// BAD: div with onClick but no role
<div onClick={handleClick}>Click me</div>

// GOOD: Proper button role and keyboard support
<div
  role="button"
  tabIndex={0}
  onClick={handleClick}
  onKeyDown={(e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      handleClick()
    }
  }}
>
  Click me
</div>

// BETTER: Use semantic HTML
<button onClick={handleClick}>Click me</button>
```

## Common Accessibility Patterns

### Interactive Card

```tsx
function ClickableCard({ onClick, children }) {
  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          onClick()
        }
      }}
      className="cursor-pointer"
    >
      {children}
    </div>
  )
}
```

### Form with Proper Labels

```tsx
function LoginForm() {
  return (
    <form>
      <label htmlFor="email">Email</label>
      <input id="email" type="email" name="email" required />
      
      <label htmlFor="password">Password</label>
      <input id="password" type="password" name="password" required />
      
      <button type="submit">Log In</button>
    </form>
  )
}
```

### Accessible Modal

```tsx
function Modal({ isOpen, onClose, children }) {
  return isOpen ? (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <h2 id="modal-title">Modal Title</h2>
      {children}
      <button onClick={onClose} aria-label="Close modal">×</button>
    </div>
  ) : null
}
```

## Testing for Accessibility

While ESLint catches many issues at build time, manual testing is still important:

1. **Keyboard Navigation**: Test that all interactive elements can be accessed with Tab/Shift+Tab and activated with Enter/Space
2. **Screen Reader**: Test with a screen reader (NVDA, JAWS, VoiceOver)
3. **Color Contrast**: Ensure text has sufficient contrast (use browser dev tools)
4. **Zoom**: Test at 200% zoom to ensure layout doesn't break
5. **Focus Indicators**: Verify visible focus indicators on interactive elements

## Resources

- [eslint-plugin-jsx-a11y Documentation](https://github.com/jsx-eslint/eslint-plugin-jsx-a11y)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [MDN Accessibility Guide](https://developer.mozilla.org/en-US/docs/Web/Accessibility)
- [WebAIM](https://webaim.org/)
- [a11y Project](https://www.a11yproject.com/)

## Disabling Rules (Use Sparingly)

If you encounter a false positive or have a valid reason to disable a rule:

```tsx
{/* eslint-disable-next-line jsx-a11y/click-events-have-key-events */}
<div onClick={handleClick}>...</div>
```

**Note**: Always document why you're disabling a rule and ensure it's truly necessary.
