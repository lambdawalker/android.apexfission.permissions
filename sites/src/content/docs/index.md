---
title: Apexfission Permissions
description: A Compose permission flow you can shape around your feature.
template: splash
hero:
  title: Permissions, with context.
  tagline: Explain access in your own voice, request a group with one action, and move forward when the permissions your feature requires are granted.
  actions:
    - text: Get started
      link: /android.apexfission.permissions/getting-started/
      icon: right-arrow
    - text: View the repository
      link: https://github.com/lambdawalker/android.apexfission.permissions
      icon: external
      variant: minimal
---

<p class="site-eyebrow">Android · Jetpack Compose · Runtime permissions</p>

<div class="site-grid">
  <a class="site-card" href="./compose/"><strong>One considered flow</strong><span>Use your own Compose pages and icons while the library handles request and recovery states.</span></a>
  <a class="site-card" href="./callbacks/"><strong>Your UI, your choice</strong><span>Use callback helpers or platform-aware recipes when a built-in screen is not the right fit.</span></a>
  <a class="site-card" href="./gallery/"><strong>See the states</strong><span>Browse real Compose screenshot fixtures for single and grouped access.</span></a>
</div>

## Built for an honest request

<div class="site-showcase">
  <div>
    <p class="site-eyebrow">A single action, clear expectations</p>
    <h3>Explain first. Ask when they choose.</h3>
    <p>The overview and each permission page can use host-supplied content. The icon strip stays in place while the carousel changes; the request starts only after a tap. Android may still show several system prompts and may grant only part of a batch.</p>
    <p><a href="./getting-started/">Add it to your app →</a></p>
  </div>
  <img src="/android.apexfission.permissions/screenshots/bundle-overview.png" alt="Compose permission overview with camera and microphone access" width="300" height="620" loading="lazy" />
</div>

## Capabilities at a glance

- **One or several permissions:** use the same `HandlePermissions` API and one request action.
- **Required and optional access:** only required grants block protected content; inspect optional grants in `PermissionGrants`.
- **Custom pages:** pass a full composable page for each permission or use localized generic defaults.
- **Recovery:** use a cautious Settings message and customize the action for your feature.
- **Timed reading:** opt in to an automatically advancing carousel with per-page timing and a segmented indicator.

The library handles ordinary Android runtime permissions. Background location, notifications, and the photo picker have separate [platform recipes](./callbacks/#platform-aware-recipes).
