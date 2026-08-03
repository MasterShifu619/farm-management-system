# Farm Management System — Learn-as-you-go Build Plan

## Context

This project is live-coding interview prep for a Monday 2026-08-03 technical interview with Gustavo Machado (NC State, Machado Lab / RABapp) for a Full Stack Developer role (Angular + Spring Boot). The interview format: 5-min codebase walkthrough, then 20 minutes live-implementing a feature/fix the interviewer picks on the spot. The user needs to **understand every part well enough to explain it**, not just have working code — so this is built in checkpointed phases, verifying and discussing each before moving to the next. The final phase is a dry run of the intended live feature (farm-to-farm movement traversal), practiced on a throwaway branch so `main` stays clean for the actual interview.

Toolchain decision: apt-installed Java 17 + apt maven (already have Java 17; just add maven). No SDKMAN — saves setup time, zero functional difference for this project. Project lives at `~/farm-management-system` (Linux home, not `/mnt/c`) per user's requirement to avoid WSL file-watch/reload issues.

## Phase 0 — Environment + Project Skeleton

- Install maven via apt (`sudo apt-get install -y maven`) — **needs sudo, user runs via `!` prefix or own terminal**
- Create `~/farm-management-system`, init git
- Backend: Spring Boot project via `start.spring.io` curl (or manually scaffold) with deps: Spring Web, Spring Security, Spring Data JPA, H2, Lombok, jjwt (io.jsonwebtoken: jjwt-api/impl/jackson). Use Maven wrapper (`./mvnw`).
- Frontend: `ng new frontend --standalone --routing --style=css` (Angular CLI, latest stable; install via npm if `ng` not present — check first)
- Verify: `./mvnw spring-boot:run` boots cleanly (default Whitelabel or actuator ok), `ng serve` boots default Angular page
- Checkpoint: walk through `pom.xml` dependencies, `application.properties`, Maven wrapper purpose, Angular project structure (`app.config.ts`, standalone bootstrap)

## Phase 1 — Domain Model, Repositories, Seed Data

- Entities (Lombok `@Entity`): `User`, `Farm`, `BiosecurityPlan`, `Movement` per the fields specified (role enum, state code, plan status enum + booleans/notes, movement fields)
- Spring Data JPA repositories for each
- `CommandLineRunner` seeding on startup: 3+ producers across 2 states, several farms, mixed plan statuses, 1 reviewer, 1 state official per state, movement records forming a small connected network. Print seeded username/password pairs to console.
- Verify: hit H2 console (`/h2-console`), confirm seeded rows exist and relationships (FKs) are correct
- Checkpoint: explain entity relationships (`@ManyToOne`/`@OneToMany`), why H2 in-memory + reseed-on-restart is fine for a demo, enum mapping strategy

## Phase 2 — Security Layer (JWT + Spring Security)

- `UserDetailsService` impl backed by `UserRepository`; BCrypt `PasswordEncoder` bean
- JWT utility class (jjwt) — sign/parse/validate, claims carry username + role
- `JwtAuthFilter` (`OncePerRequestFilter`) — validates token, populates `SecurityContextHolder`
- `SecurityConfig` — stateless session policy, permit `/api/auth/**`, secure everything else, register filter
- `AuthController` — `POST /api/auth/login` (verify BCrypt hash, issue JWT)
- Every security class gets clear comments (what + why), per user's requirement to read/explain every class
- Write `ARCHITECTURE.md`: step-by-step JWT flow from Angular login call → token issuance → filter validating a later request → controller enforcing a role rule
- Verify: `curl` login to get a token, `curl` a protected endpoint with/without the token (401 vs 200)
- Checkpoint: this is core interview material — user should be able to trace the whole request lifecycle unaided before moving on

## Phase 3 — REST Controllers + Role-Based Access Rules

- `FarmController`: PRODUCER — CRUD own farms only; STATE_OFFICIAL — read-only farms in their own state; enforce via querying repository filtered by authenticated principal (owner id / state), not just `@PreAuthorize` role checks alone
- `BiosecurityPlanController`: PRODUCER create/edit/submit (DRAFT→SUBMITTED) own farm's plans; REVIEWER view all SUBMITTED plans + approve (SUBMITTED→APPROVED) or send back to DRAFT; STATE_OFFICIAL read-only plans in their state
- `MovementController`: seed data already exists; add one controller method stub / clear `// TODO` marking where the "farms within N movements" traversal goes — **do not implement it**, this is the reserved live-coding feature
- Verify: manual `curl` tests per role proving cross-tenant/cross-state blocking actually works at the API level (not just hidden in UI)
- Checkpoint: explain how a role check ties back to JWT claims → `SecurityContext` → controller/repository filtering

## Phase 4 — Angular Frontend

- `AuthService` (login call, JWT storage in `localStorage`, simple decode for role/username)
- `HttpInterceptor` — attaches `Authorization: Bearer <token>` to outgoing requests
- Route guard — blocks protected routes without a valid token
- Farm list view + create/edit form
- Plan view with role-appropriate actions (producer: submit; reviewer: approve/reject)
- Keep visual design plain/functional — no styling polish
- Verify: manually log in as each of the 3 seeded roles in the browser, confirm each sees only what they should
- Checkpoint: explain interceptor + guard, and why backend enforcement (Phase 3) is still required even though the UI also gates by role

## Phase 5 — Polish + Repo Hygiene

- README: one-line description, what it does, stack, how to run backend (`./mvnw spring-boot:run`) and frontend (`ng serve`) locally
- `.gitignore`: `target/`, `node_modules/`, `dist/`, `.angular/`, IDE files
- Commit everything to `main` on git (already initialized in Phase 0)
- Full clean-boot verification: backend + frontend running simultaneously, walk the whole app in a browser end to end

## Phase 6 — Practice Round (Movement Traversal, on a throwaway branch)

Practiced once already on `feat/disease-outbreak` (branch name flexible — any throwaway branch off `main` works). Built in two stages on purpose: get a working, correct feature first, then add the visual so the "reveal" lands. Both stages recreated below in enough detail to redo from scratch.

**Do not merge this branch.** Once built and understood, switch back to `main`, which stays exactly at the clean TODO-stub state for the actual interview.

### Stage 1 — backend traversal + species filter + plain table

Trigger prompt used (this alone was enough — the algorithm spec below was already known from this doc, not restated in chat):
> "lets make a small frontend also to demo this"

Backend — `MovementController`, replacing the `/traversal` 501 stub:

- `GET /api/movements/traversal?farmId=X&hops=N&species=(optional)`
- **Not** a plain single-queue BFS — level-by-level, because chronology has to be enforced per level:
  - Track two maps: `farmId -> hopCount` and `farmId -> earliestExposureDate`.
  - Source farm starts at hop 0 with no date constraint (`LocalDate.MIN`).
  - At each level, only follow a farm's outgoing movements whose `movementDate` is **on/after** the date exposure arrived at that farm — a chain can't trace backwards in time.
  - If multiple edges reach the same farm within the same level, keep the **earliest** qualifying date (most permissive for continuing further hops).
  - Stop expanding once `hops` levels are done or the frontier is empty.
  - Exclude the source farm itself from the result.
- `species` optional query param: when present, only follow movements of that species. Added `MovementRepository.findBySourceFarmIdAndSpecies(Long, String)` and branch on whether the param is present. Omitted/blank = no filter, trace everything.
- Response DTO (`FarmDistanceResponse`): `farmId`, `farmName`, `stateCode`, `hops`, `earliestExposureDate`. Sorted by hops ascending.
- Error handling: 404 if `farmId` doesn't exist, 400 if `hops < 0`.
- Verify with curl against the 5-movement seed graph before touching the frontend: a few hop counts, a species filter that should exclude a branch (Cattle vs Swine legs split at Blue Ridge → Lowcountry), a dead-end farm (empty result), an unknown farm (404).

Frontend — new `/traversal` route + nav link, `MovementTraversalComponent`:

- Form: source farm dropdown, hop count number input, animal dropdown (default "All").
- **Farm and species dropdowns are built from `GET /api/movements` data, not `GET /api/farms`** — the farms endpoint is role-scoped and blocks REVIEWER entirely, but movements are visible to every role. Derive unique farms/species by scanning `sourceFarm`/`destinationFarm`/`species` across all movements.
- Results table: farm, state, hops away, earliest exposure date.
- No graph yet — just the working form + table.

### Stage 2 — the graph (the reveal)

Real trigger prompts, in order (a genuine exploratory question, then confirmation, then one follow-up):
> "how can I present this better in terms of a graph or something on the screen? Is it possible?"
> (recommended an SVG node-link diagram, small graph so no layout library needed)
> "Yes, lets go with the first option showing all the movement data in the db at the top and then option to find movement traversal"
> "Can the arrows also have the animals printed on it" (added after the first graph pass)

What got built, above the existing Stage 1 form:

- SVG node-link diagram: every farm from movement data as a circle, ring layout (evenly spaced around a circle by angle, radius ~160px, centered in a 420×420 viewBox).
- Every movement as a directed arrow between farm circles — trimmed back by node radius (+ extra gap) so the arrowhead marker lands cleanly on the circle's edge instead of hiding under it.
- Species name labeled directly on each arrow: positioned at the edge midpoint, offset perpendicular off the line (~10px) so it doesn't sit on top of the arrow, rotated to read along the arrow's direction, flipped upright when the arrow points right-to-left so it's never upside down.
- Color logic (sequential ramp, not arbitrary per-hop hues) — only applied after a traversal query runs:
  - Source farm: darkest step.
  - Reachable farms: progressively lighter with increasing hop distance.
  - Unreached farms: neutral gray.
  - Small legend underneath explaining the four states.
- When a species filter is selected: arrows not carrying that species dim to ~20% opacity — visible before even looking at the results table, reinforces that some diseases only trace through one animal.
- Hover tooltips via native SVG `<title>` elements on both nodes and edges (farm name/state; species/date) — no custom tooltip component, deliberate simplification for an internal demo tool.
- Before any query runs, the graph still renders the full unfiltered network so there's always something to look at.

### Also worth knowing when recreating

- `CLAUDE.md`/`PLAN.md` (this file) travel with every branch — the base algorithm spec doesn't need restating in chat, only genuine follow-up asks (chronology, species, visualization) do.
- The date-chronology fix in Stage 1 actually originated from a question, not an instruction: *"is the date being considered to calculate hops"* → gap explained → *"yes"* to fix it. Worth noticing that gap live rather than scripting it.
- Goal: this exact feature has been built once already before doing it live Monday, so the live version is muscle memory, not a first attempt.

## Verification (recurring, every phase)

- Backend: `./mvnw spring-boot:run` starts without errors, relevant `curl` calls return expected status/data
- Frontend: `ng serve` compiles without errors, manual browser check of the relevant view
- Before declaring any phase done, user should be able to explain the new pieces without looking at code
