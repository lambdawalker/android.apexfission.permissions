# Library website

This Astro Starlight project builds the GitHub Pages site. The repository root `docs/` remains the home for source documentation and Compose screenshot fixtures. The build copies the PNG fixtures into `public/screenshots/` without committing duplicates.

```bash
cd sites
npm ci
npm run dev
npm run build
```

The output is `sites/dist/`, with the project base path `/android.apexfission.permissions/`. [Library site](../.github/workflows/deploy-library-site.yml) builds on relevant pull requests and deploys on relevant `main` pushes (or manually from Actions). In repository **Settings → Pages → Build and deployment**, choose **GitHub Actions** as the source once; branch publishing cannot select `/sites` directly. The `github-pages` environment is separate from Maven Central.

Update site articles in `src/content/docs/` and the theme in `src/styles/brand.css`. Existing detailed guides are linked from the site to the repository, keeping their maintenance in `agents/` and `docs/`.
