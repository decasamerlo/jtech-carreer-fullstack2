---
description: Investigate and plan work
subtask: true
---

<summary>
You MUST investigate $ARGUMENTS by gathering context.
You SHOULD ask clarifying questions when needed.
You MUST deliver a report with findings and task suggestions.
</summary>

<user_guidelines>
$ARGUMENTS
</user_guidelines>

<objective>
You MUST investigate the given topic thoroughly before proposing solutions.
</objective>

1. **Clarify** — Ask clarifying questions about scope, constraints, or desired outcomes if anything is unclear. Don't hesitate to ask.

2. **Context-gathering phase** — Dispatch parallel subagents to gather context from:
   - Load relevant agent skills
   - Search the codebase for related code, patterns, and conventions
   - Read documentation (README, SPECIFICATION, AGENTS.md, etc.)
   - Check app logs if available
   - Access the database (readonly) if relevant
   - Review similar features or past implementations

3. **Analysis** — Synthesize findings into a structured report covering:
   - What was found (with evidence)
   - Explanations and reasoning
   - Implementation options (with trade-offs)
   - Suggested task breakdown
   - Test plan recommendations
   - **Documentation impact** — Suggest which docs need updating (AGENTS.md, README.md, misc/docs/BACKLOG.md) and whether a new misc/docs/ file should be created
   - What to do next

4. **Task creation** — Present the report to the developer. Include the documentation suggestions from step 3 for their awareness. If they approve task creation, load the `writing-plans` skill and create a structured plan with actionable tasks.
