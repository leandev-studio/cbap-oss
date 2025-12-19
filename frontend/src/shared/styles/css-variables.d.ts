/**
 * TypeScript declarations for CBAP CSS custom properties
 * 
 * This file provides type safety for CSS variables defined in global.css
 */

declare module '*.css' {
  const content: { [className: string]: string };
  export default content;
}

/**
 * CSS Custom Properties (Variables) available in the CBAP theme system
 * 
 * These variables are defined in global.css and can be used in any CSS file
 * or inline styles using var(--variable-name)
 */
export interface CbapCSSVariables {
  /* Base Colors */
  '--color-bg-primary': string;
  '--color-bg-secondary': string;
  '--color-bg-tertiary': string;
  '--color-surface': string;
  '--color-surface-elevated': string;

  /* Text Colors */
  '--color-text-primary': string;
  '--color-text-secondary': string;
  '--color-text-tertiary': string;
  '--color-text-inverse': string;
  '--color-text-disabled': string;

  /* Primary Brand Colors */
  '--color-primary': string;
  '--color-primary-light': string;
  '--color-primary-dark': string;
  '--color-primary-subtle': string;

  /* Semantic Colors */
  '--color-success': string;
  '--color-success-light': string;
  '--color-success-dark': string;
  '--color-success-subtle': string;

  '--color-warning': string;
  '--color-warning-light': string;
  '--color-warning-dark': string;
  '--color-warning-subtle': string;

  '--color-error': string;
  '--color-error-light': string;
  '--color-error-dark': string;
  '--color-error-subtle': string;

  '--color-info': string;
  '--color-info-light': string;
  '--color-info-dark': string;
  '--color-info-subtle': string;

  /* Neutral Colors */
  '--color-neutral': string;
  '--color-neutral-light': string;
  '--color-neutral-dark': string;

  /* Border & Divider Colors */
  '--color-border': string;
  '--color-border-light': string;
  '--color-border-dark': string;

  /* Interactive States */
  '--color-interactive-default-bg': string;
  '--color-interactive-default-text': string;
  '--color-interactive-default-border': string;

  '--color-interactive-hover-bg': string;
  '--color-interactive-hover-text': string;
  '--color-interactive-hover-border': string;

  '--color-interactive-active-bg': string;
  '--color-interactive-active-text': string;
  '--color-interactive-active-border': string;

  '--color-interactive-focus-bg': string;
  '--color-interactive-focus-text': string;
  '--color-interactive-focus-border': string;
  '--color-interactive-focus-ring-width': string;

  '--color-interactive-disabled-bg': string;
  '--color-interactive-disabled-text': string;
  '--color-interactive-disabled-border': string;

  /* Workflow States */
  '--color-workflow-draft': string;
  '--color-workflow-submitted': string;
  '--color-workflow-approved': string;
  '--color-workflow-rejected': string;
  '--color-workflow-closed': string;
  '--color-workflow-archived': string;

  /* Task Status */
  '--color-task-open': string;
  '--color-task-in-progress': string;
  '--color-task-done': string;
  '--color-task-cancelled': string;

  /* Document Status */
  '--color-document-draft': string;
  '--color-document-in-review': string;
  '--color-document-approved': string;
  '--color-document-published': string;
  '--color-document-retired': string;

  /* Shadows */
  '--shadow-sm': string;
  '--shadow-md': string;
  '--shadow-lg': string;
  '--shadow-xl': string;
}
