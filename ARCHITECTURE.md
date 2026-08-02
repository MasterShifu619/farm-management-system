# JWT Auth Flow

## 1. Login

`POST /api/auth/login` with `{"username": "...", "password": "..."}`.

`AuthController` builds a `UsernamePasswordAuthenticationToken` from the raw credentials and hands it to Spring Security's `AuthenticationManager`. That manager delegates to a `DaoAuthenticationProvider`, which:

1. Calls `CustomUserDetailsService.loadUserByUsername()` — looks up the `User` row via `UserRepository`, wraps it as a Spring Security `UserDetails` (username, BCrypt hash, one `ROLE_<role>` authority derived from our `Role` enum).
2. Compares the submitted password against the stored BCrypt hash via the `PasswordEncoder` bean.
3. On success, returns an authenticated `Authentication` object carrying the user's authority (e.g. `ROLE_PRODUCER`). On failure, throws `BadCredentialsException`.

`AuthController` reads the authority off that `Authentication`, passes `(username, role)` to `JwtUtil.generateToken()`, which signs a JWT (HS384, key derived from `app.jwt.secret`) with `subject=username`, `claim("role", ...)`, and an expiry. The signed token string comes back in the response body — nothing is stored server-side (no session, no server-side token table).

## 2. Later authenticated requests

Client sends `Authorization: Bearer <token>` on every request.

`JwtAuthFilter` (a `OncePerRequestFilter`, registered via `SecurityConfig.addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`) runs early in the filter chain on every request:

1. Reads the header, strips `Bearer `.
2. `JwtUtil.isTokenValid()` — parses and verifies the signature/expiry. Bad signature, tampered payload, or expired token → invalid, filter does nothing and moves on.
3. If valid, pulls `username` + `role` straight out of the token's own claims (no DB round trip — the signature is the trust boundary) and builds a `UsernamePasswordAuthenticationToken(username, null, [role authority])`, then sets it on `SecurityContextHolder`.

## 3. Authorization

Later in the same chain, Spring Security's `AuthorizationFilter` checks the rule from `SecurityConfig`: `/api/auth/**` and `/h2-console/**` are `permitAll()`, everything else requires `authenticated()`. If step 2 populated the context, the request passes; if not, `Http403ForbiddenEntryPoint` rejects with 403.

Session policy is `STATELESS` — no `JSESSIONID`, no server-side session store. Every request re-proves identity via its own token.

## Gotcha worth knowing

`OncePerRequestFilter` skips itself on `ERROR`-dispatch (Spring's internal forward to `/error` when a route 404s). Without explicitly permitting `/error`, a 404 on a real endpoint gets masked as a 403, because the forwarded `/error` request has no re-populated `SecurityContext` and falls under `anyRequest().authenticated()`. `SecurityConfig` permits `/error` explicitly to avoid this.
