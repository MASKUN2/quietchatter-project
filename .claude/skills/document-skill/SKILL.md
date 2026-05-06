---
name: documents skill
description: Use when reviewing or updating markdown documentation in any (sub)module.
---

# Rules
## File Rules
Each (sub)module root must contain exactly three markdown files.
- README.md: Everything needed to understand the service (tech stack, structure, API conventions, how to run, etc.)
- CLAUDE.md: AI task instructions only. Must begin with a directive to read README.md. No content duplication with README.md.
- GEMINI.md: Contains only the single line `MUST READ CLAUDE.md.`

## Writing Rules
No emphasis formatting (bold, italic) and no emojis.

## Content
Keep documentations consistent with the code.