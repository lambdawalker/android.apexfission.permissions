import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://lambdawalker.github.io',
  base: '/android.apexfission.permissions',
  integrations: [
    starlight({
      title: 'Apexfission Permissions',
      description: 'Android runtime permissions, explained with Compose.',
      favicon: '/favicon.svg',
      customCss: ['./src/styles/brand.css'],
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/lambdawalker/android.apexfission.permissions' }],
      sidebar: [
        { label: 'Overview', slug: 'index' },
        { label: 'Start here', items: [
          { label: 'Install and request', slug: 'getting-started' },
          { label: 'Compose UI', slug: 'compose' },
          { label: 'Callbacks without library UI', slug: 'callbacks' },
          { label: 'Platform recipes', slug: 'recipes' },
        ] },
        { label: 'Explore', items: [
          { label: 'Runnable demos', slug: 'demos' },
          { label: 'Screen gallery', slug: 'gallery' },
          { label: 'Public API reference', slug: 'reference' },
          { label: 'Build, test, and release', slug: 'development' },
        ] },
      ],
    }),
  ],
});
