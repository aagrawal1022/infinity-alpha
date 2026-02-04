<!-- 
  ============================================================================
  SYNC IMPACT REPORT - Constitution Update
  ============================================================================
  
  CONSTITUTION VERSION: 1.0.0 (Initial Adoption)
  RATIFICATION DATE: 2026-02-04
  
  PRINCIPLES ADDED:
  • I. Clean Code (explicit requirement for readable, maintainable code)
  • II. Simple User Experience (UX simplicity mandate for frontend)
  • III. Responsive Design (cross-device design requirement)
  • IV. No Testing (Non-Negotiable Override - supersedes all other guidance)
  
  TECHNOLOGY STACK DEFINED:
  • Backend & MCP Server: Python 3.11+
  • Frontend: React 18+ with responsive CSS
  • Development Environment: Python virtual environment
  • Architecture: Clean separation (Data/Reasoning/Presentation layers)
  
  DEPENDENT TEMPLATES UPDATED:
  ✅ spec-template.md - Removed "Testing" language, changed "testable" to "verifiable"
  ✅ tasks-template.md - Removed all test task sections, added venv task, adjusted numbering
  ✅ plan-template.md - Removed testing framework field, removed tests/ directories
  
  NOTES:
  - All references to automated testing removed from templates
  - Manual verification language substituted throughout
  - Python virtual environment setup explicitly required
  - Technology stack is fixed; deviations require amendment
  - Constitution takes precedence over all other templates/guidance
  
  ============================================================================
-->

# Infinity Alpha Constitution

## Core Principles

### I. Clean Code
Every piece of code must be readable, maintainable, and self-documenting. Functions should be small and focused; variable names should be explicit; comments should explain the "why" not the "what". Code that is unclear or unnecessarily complex MUST be refactored. Simplicity is non-negotiable—local reasoning should be possible without reading the entire codebase.

### II. Simple User Experience
The frontend UI must prioritize simplicity and intuitiveness. Every screen should have a clear, single primary action. Information architecture should require minimal clicks to reach the most common workflows. Jargon should be minimized; users should understand the application without training. Feature requests that add complexity without proportional value must be rejected.

### III. Responsive Design
Frontend components MUST work seamlessly across desktop, tablet, and mobile viewports. No broken layouts, no unreadable text, no untappable controls on any device size. Media queries and flexible layouts are mandatory. User experience should degrade gracefully on constrained screens, not fail.

### IV. No Testing (Non-Negotiable Override)
This project explicitly does NOT include unit tests, integration tests, or end-to-end tests. This decision supersedes all other guidance, templates, or industry practices. Manual testing by developers and users is the only validation mechanism. Any template or process that mandates or assumes automated testing MUST be ignored or overridden.

## Technology Stack

The project uses a fixed technology baseline. Deviations require constitution amendment:

- **Backend & MCP Server**: Python 3.11+
- **Frontend**: React 18+ with responsive CSS (Flexbox/Grid)
- **Development Environment**: Python virtual environment (`python -m venv .venv`)
- **Architecture**: Clean separation of concerns—Data layer (MCP/Python backend), Reasoning layer (AI agents), Presentation layer (React frontend)

## Development Workflow

1. **Environment Setup**: Developers MUST initialize Python virtual environment (`python -m venv .venv`) before running any backend or MCP code. Virtual environment activation is a prerequisite for every development session.
2. **Code Review**: All code changes MUST be reviewed for adherence to Clean Code principle before merge. Reviews focus on readability, naming clarity, and necessity.
3. **Manual Validation**: Developers are responsible for manually testing their changes across target viewports (desktop, tablet, mobile for frontend). Spot-checks in production-like conditions are required.
4. **No Automated Testing Gates**: CI/CD pipelines MUST NOT block merges based on test results, as this project has no tests. Build checks (linting, compilation, syntax) are acceptable; test coverage gates are forbidden.

## Governance

The Constitution supersedes all other guidance documents and templates. Where a template (plan-template.md, spec-template.md, tasks-template.md) conflicts with these core principles, the Constitution takes precedence.

**Amendment Process**: Amendments require explicit documentation (rationale, affected principles), written approval, and update of this file plus dependent artifacts.

**Compliance Verification**: All Pull Requests must reference which principles they uphold. Code review checklists should verify Clean Code and Responsive Design adherence.

**Ratification & Amendments**:
- **Original Ratification Date**: 2026-02-04
- **Last Amended**: 2026-02-04 (initial adoption)
- **Version**: 1.0.0

---

**Version**: 1.0.0 | **Ratified**: 2026-02-04 | **Last Amended**: 2026-02-04
